package uloha1;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import oglutils.*;
import transforms.*;

import java.awt.*;
import java.awt.event.*;

/**
 * GLSL sample:<br/>
 * Read and compile shader from files "/shader/glsl01/start.*" using ShaderUtils
 * class in oglutils package (older GLSL syntax can be seen in
 * "/shader/glsl01/startForOlderGLSL")<br/>
 * Manage (create, bind, draw) vertex and index buffers using OGLBuffers class
 * in oglutils package<br/>
 * Requires JOGL 2.3.0 or newer
 *
 * @author PGRF FIM UHK
 * @version 2.0
 * @since 2015-09-05
 */

public class Renderer implements GLEventListener, MouseListener, MouseMotionListener, KeyListener {

    private int width, height;
    private boolean withBorder = false; // přepinač signalizující zobrazení hran

    private OGLBuffers buffers;
    private OGLTextRenderer textRenderer;
    private OGLRenderTarget renderTarget;
    private OGLTexture2D.Viewer textureViewer;
    private OGLTexture2D texture;

    private int shaderProgramViewer, locTimeViewer, locView, locProjection, locMode, locLightVP, locEyePosition, locLightPosition;
    private int shaderProgramLight, locTimeLight, locLightView, locLightProj, locModeLight;

    private Mat4 projViewer, projLight;
    private float time = 0;
    private Camera camera, lightCamera;
    private int mx, my;
    private String text;
    private Robot robot; // pro zjištění barvy
    private Color color; // barva do textového výpisu

    @Override
    public void init(GLAutoDrawable glDrawable) {
        //inicializace barvy pro získání do textového výpisu
        color = new Color(0, 0, 0);
        try {
            // získání instance robota pro zjištění barvy
            this.robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        // inicializace textu pro vpis údajů pro editování
        this.text = new String("");

        // check whether shaders are supported
        GL2GL3 gl = glDrawable.getGL().getGL2GL3();
        OGLUtils.shaderCheck(gl);

        OGLUtils.printOGLparameters(gl);

        textRenderer = new OGLTextRenderer(gl, glDrawable.getSurfaceWidth(), glDrawable.getSurfaceHeight());

        gl.glPolygonMode(GL2GL3.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);// vyplnění přivrácených i odvrácených stran
        gl.glEnable(GL2GL3.GL_DEPTH_TEST); // zapnout z-test

        // načtení souborů (shaders)
        shaderProgramLight = ShaderUtils.loadProgram(gl, "/uloha1/light");
        shaderProgramViewer = ShaderUtils.loadProgram(gl, "/uloha1/start");

        // create buffers vertex a index
        buffers = GridFactory.generateGrid(gl, 100, 100);

        // vytvoření kamery (světlo)
        lightCamera = new Camera()
                .withPosition(new Vec3D(0, 0, 0))
                .addAzimuth(5 / 4. * Math.PI)//-3/4.
                .addZenith(-1 / 5. * Math.PI)
                .withFirstPerson(false)
                .withRadius(5);

        // vytvoření kamery (uživatel)
        camera = new Camera()
                .withPosition(new Vec3D(0, 0, 0))
                .addAzimuth(5 / 4. * Math.PI)//-3/4.
                .addZenith(-1 / 5. * Math.PI)
                .withFirstPerson(false)
                .withRadius(5);

        locTimeViewer = gl.glGetUniformLocation(shaderProgramViewer, "time");
        locMode = gl.glGetUniformLocation(shaderProgramViewer, "mode");
        locView = gl.glGetUniformLocation(shaderProgramViewer, "view");
        locProjection = gl.glGetUniformLocation(shaderProgramViewer, "projection");
        locLightVP = gl.glGetUniformLocation(shaderProgramViewer, "lightVP");
        locEyePosition = gl.glGetUniformLocation(shaderProgramViewer, "eyePosition");
        locLightPosition = gl.glGetUniformLocation(shaderProgramViewer, "lightPosition");

        locLightProj = gl.glGetUniformLocation(shaderProgramLight, "projLight");
        locLightView = gl.glGetUniformLocation(shaderProgramLight, "viewLight");
        locModeLight = gl.glGetUniformLocation(shaderProgramLight, "mode");
        locTimeLight = gl.glGetUniformLocation(shaderProgramLight, "time");

        texture = new OGLTexture2D(gl, "/textures/mosaic.jpg");
        textureViewer = new OGLTexture2D.Viewer(gl);

        renderTarget = new OGLRenderTarget(gl, 1024, 1024);
    }

    @Override
    public void display(GLAutoDrawable glDrawable) {
        GL2GL3 gl = glDrawable.getGL().getGL2GL3();

        // posun světla
        time += 0.1;
        lightCamera = lightCamera.addAzimuth(0.01);

        renderFromLight(gl);
        renderFromViewer(gl);

        textureViewer.view(texture, -1, -1, 0.5);
        textureViewer.view(renderTarget.getColorTexture(), -1, -0.5, 0.5);
        textureViewer.view(renderTarget.getDepthTexture(), -1, 0, 0.5);

        textRenderer.drawStr2D(3, height - 20, text);
        textRenderer.drawStr2D(width - 90, 3, " (c) PGRF UHK");

        // gl pro ctverec
        final GL2 glColorRectangle = glDrawable.getGL().getGL2();

        // čtverec pro zobrazení barvy
        glColorRectangle.glBegin(GL2.GL_POLYGON);

        glColorRectangle.glColor4f((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, (float) color.getAlpha() / 255);
        glColorRectangle.glVertex2f(0.5f, 0.5f);
        glColorRectangle.glVertex2f(1.0f, 0.5f);
        glColorRectangle.glVertex2f(1.0f, 1.0f);
        glColorRectangle.glVertex2f(0.5f, 1.0f);

        glColorRectangle.glEnd();
    }

    private void renderFromLight(GL2GL3 gl) {
        gl.glUseProgram(shaderProgramLight);

        renderTarget.bind();

        gl.glClearColor(0.3f, 0.0f, 0.0f, 1.0f);
        gl.glClear(GL2GL3.GL_COLOR_BUFFER_BIT | GL2GL3.GL_DEPTH_BUFFER_BIT);

        gl.glUniform1f(locTimeLight, time);

        gl.glUniformMatrix4fv(locLightView, 1, false, lightCamera.getViewMatrix().floatArray(), 0);
        gl.glUniformMatrix4fv(locLightProj, 1, false, projLight.floatArray(), 0);

        // renderuj stěnu + ZAKOMENTOVANÝ GL_TRIANGLE_STRIP
        gl.glUniform1i(locModeLight, 0);
        buffers.draw(GL2GL3.GL_TRIANGLES, shaderProgramLight);
        //buffers.draw(GL2GL3.GL_TRIANGLE_STRIP, shaderProgramLight);

        // renderuj modifiedSphere + ZAKOMENTOVANÝ GL_TRIANGLE_STRIP
        gl.glUniform1i(locModeLight, 1);
        buffers.draw(GL2GL3.GL_TRIANGLES, shaderProgramLight);
        // buffers.draw(GL2GL3.GL_TRIANGLE_STRIP, shaderProgramLight);

        // renderuj parsur + ZAKOMENTOVANÝ GL_TRIANGLE_STRIP
        gl.glUniform1i(locModeLight, 2);
        buffers.draw(GL2GL3.GL_TRIANGLES, shaderProgramLight);
        // buffers.draw(GL2GL3.GL_TRIANGLE_STRIP, shaderProgramLight);

    }

    private void renderFromViewer(GL2GL3 gl) {
        gl.glUseProgram(shaderProgramViewer);

        gl.glBindFramebuffer(GL2GL3.GL_FRAMEBUFFER, 0);
        gl.glViewport(0, 0, width, height);

        gl.glClearColor(0.0f, 0.3f, 0.0f, 1.0f);
        gl.glClear(GL2GL3.GL_COLOR_BUFFER_BIT | GL2GL3.GL_DEPTH_BUFFER_BIT);

        gl.glUniform1f(locTimeViewer, time);

        // zobrazená obrysů trojuhelníku na základě hodnoty withBorder -> klávesa B
        if (withBorder) {
            gl.glPolygonMode(GL2GL3.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
        } else {
            gl.glPolygonMode(GL2GL3.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
        }

        gl.glUniformMatrix4fv(locView, 1, false, camera.getViewMatrix().floatArray(), 0);
        gl.glUniformMatrix4fv(locProjection, 1, false, projViewer.floatArray(), 0);
        gl.glUniformMatrix4fv(locLightVP, 1, false, lightCamera.getViewMatrix().mul(projLight).floatArray(), 0);
        gl.glUniform3fv(locEyePosition, 1, ToFloatArray.convert(camera.getPosition()), 0);
        gl.glUniform3fv(locLightPosition, 1, ToFloatArray.convert(lightCamera.getPosition()), 0);

        texture.bind(shaderProgramViewer, "textureID", 0);
        renderTarget.getColorTexture().bind(shaderProgramViewer, "colorTexture", 1);
        renderTarget.getDepthTexture().bind(shaderProgramViewer, "depthTexture", 2);

        // renderuj stěnu + ZAKOMENTOVANÝ GL_TRIANGLE_STRIP
        gl.glUniform1i(locMode, 0);
        buffers.draw(GL2GL3.GL_TRIANGLES, shaderProgramViewer);
        // buffers.draw(GL2GL3.GL_TRIANGLE_STRIP, shaderProgramViewer);

        // renderuj modifiedSphere + ZAKOMENTOVANÝ GL_TRIANGLE_STRIP
        gl.glUniform1i(locMode, 1);
        buffers.draw(GL2GL3.GL_TRIANGLES, shaderProgramViewer);
        // buffers.draw(GL2GL3.GL_TRIANGLE_STRIP, shaderProgramViewer);

        // renderuj parsur + ZAKOMENTOVANÝ GL_TRIANGLE_STRIP
        gl.glUniform1i(locMode, 2);
        buffers.draw(GL2GL3.GL_TRIANGLES, shaderProgramViewer);
        // buffers.draw(GL2GL3.GL_TRIANGLE_STRIP, shaderProgramViewer);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
        textRenderer.updateSize(width, height);

        double ratio = height / (double) width;
        // Ortogonální projekce
        projLight = new Mat4OrthoRH(5 / ratio, 5, 0.1, 20);
        // projViewer = new Mat4OrthoRH(5 / ratio, 5, 0.1, 20);

        // Perspektivní projekce
        projViewer = new Mat4PerspRH(Math.PI / 3, ratio, 1, 20.0);
        // projLight = new Mat4PerspRH(Math.PI / 3, ratio, 1, 20.0);
    }

    @Override
    public void dispose(GLAutoDrawable glDrawable) {
        GL2GL3 gl = glDrawable.getGL().getGL2GL3();
        gl.glDeleteProgram(shaderProgramViewer);
        gl.glDeleteProgram(shaderProgramLight);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // uložení ouřadnic kliknutí -> ovládání kamery myší -> mouseDragged
        mx = e.getX();
        my = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // ovládání kamery myší
        camera = camera.addAzimuth(Math.PI * (mx - e.getX()) / width);
        camera = camera.addZenith(Math.PI * (e.getY() - my) / width);
        mx = e.getX();
        my = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Vypisování informací o pixelech(barva, pozice x, pozice y, hloubka barvy, RGB)
        color = robot.getPixelColor(e.getX(), e.getY());
        String colorText = "Red: " + color.getRed() +
                "Green: " + color.getGreen() +
                "Blue: " + color.getBlue() +
                "Alpha: " + color.getAlpha();

        this.text = "X:" + String.valueOf(e.getX()) + ", Y: " + String.valueOf(e.getY()) + ", barva: " + colorText;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // ovládání kamery klávesnicí
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                camera = camera.forward(1);
                break;
            case KeyEvent.VK_D:
                camera = camera.right(1);
                break;
            case KeyEvent.VK_S:
                camera = camera.backward(1);
                break;
            case KeyEvent.VK_A:
                camera = camera.left(1);
                break;
            case KeyEvent.VK_CONTROL:
                camera = camera.down(1);
                break;
            case KeyEvent.VK_SHIFT:
                camera = camera.up(1);
                break;
            case KeyEvent.VK_SPACE:
                camera = camera.withFirstPerson(!camera.getFirstPerson());
                break;
            case KeyEvent.VK_R:
                camera = camera.mulRadius(0.9f);
                break;
            case KeyEvent.VK_F:
                camera = camera.mulRadius(1.1f);
                break;
            case KeyEvent.VK_B:
                withBorder = !withBorder;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

}