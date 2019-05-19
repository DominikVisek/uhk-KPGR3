package uloha2.parallax;

import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import oglutils.*;
import transforms.*;

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

    private OGLBuffers buffers;
    private OGLTextRenderer textRenderer;
    private OGLRenderTarget renderTarget;
    private OGLTexture2D.Viewer textureViewer;
    private OGLTexture2D texture;

    private int shaderProgramViewer, locTime, locView, locProjection, locMode, locLightVP, locEyePosition, locLightPosition;
    private int shaderProgramLight, locLightView, locLightProj, locModeLight;

    private Mat4 projViewer, projLight;
    private float time = 0;
    private Camera camera, lightCamera;
    private int mx, my;
    private boolean withBorder = false;

    @Override
    public void init(GLAutoDrawable glDrawable) {
        // check whether shaders are supported
        GL2GL3 gl = glDrawable.getGL().getGL2GL3();
        OGLUtils.shaderCheck(gl);

        OGLUtils.printOGLparameters(gl);

        textRenderer = new OGLTextRenderer(gl, glDrawable.getSurfaceWidth(), glDrawable.getSurfaceHeight());

        gl.glPolygonMode(GL2GL3.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);// vyplnění přivrácených i odvrácených stran
        gl.glEnable(GL2GL3.GL_DEPTH_TEST); // zapnout z-test

        shaderProgramLight = ShaderUtils.loadProgram(gl, "/uloha2/parallax/light");
        shaderProgramViewer = ShaderUtils.loadProgram(gl, "/uloha2/parallax/start");

        //createBuffers(gl);
        buffers = GridFactory.generateGrid(gl, 100, 100);

        lightCamera = new Camera()
                .withPosition(new Vec3D(5, 5, 5))
                .addAzimuth(5 / 4. * Math.PI)//-3/4.
                .addZenith(-1 / 5. * Math.PI);

        camera = new Camera()
                .withPosition(new Vec3D(0, 0, 0))
                .addAzimuth(5 / 4. * Math.PI)//-3/4.
                .addZenith(-1 / 5. * Math.PI)
                .withFirstPerson(false)
                .withRadius(5);

        locTime = gl.glGetUniformLocation(shaderProgramViewer, "time");
        locMode = gl.glGetUniformLocation(shaderProgramViewer, "mode");
        locView = gl.glGetUniformLocation(shaderProgramViewer, "view");
        locProjection = gl.glGetUniformLocation(shaderProgramViewer, "projection");
        locLightVP = gl.glGetUniformLocation(shaderProgramViewer, "lightVP");
        locEyePosition = gl.glGetUniformLocation(shaderProgramViewer, "eyePosition");
        locLightPosition = gl.glGetUniformLocation(shaderProgramViewer, "lightPosition");

        locLightProj = gl.glGetUniformLocation(shaderProgramLight, "projLight");
        locLightView = gl.glGetUniformLocation(shaderProgramLight, "viewLight");
        locModeLight = gl.glGetUniformLocation(shaderProgramLight, "mode");

        texture = new OGLTexture2D(gl, "/textures/mosaic.jpg");
        textureViewer = new OGLTexture2D.Viewer(gl);

        renderTarget = new OGLRenderTarget(gl, 1024, 1024);
    }

    void createBuffers(GL2GL3 gl) {
        float[] vertexBufferData = {
                -1, -1, 0.7f, 0, 0,
                1, 0, 0, 0.7f, 0,
                0, 1, 0, 0, 0.7f
        };
        int[] indexBufferData = {0, 1, 2};

        // vertex binding description, concise version
        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2), // 2 floats
                new OGLBuffers.Attrib("inColor", 3) // 3 floats
        };
        buffers = new OGLBuffers(gl, vertexBufferData, attributes, indexBufferData);
    }

    @Override
    public void display(GLAutoDrawable glDrawable) {
        GL2GL3 gl = glDrawable.getGL().getGL2GL3();

        renderFromLight(gl);
        renderFromViewer(gl);

        String text = this.getClass().getName();
        textRenderer.drawStr2D(3, height - 20, text);
        textRenderer.drawStr2D(width - 90, 3, " (c) PGRF UHK");
    }

    private void renderFromLight(GL2GL3 gl) {
        gl.glUseProgram(shaderProgramLight);

        renderTarget.bind();

        gl.glClearColor(0.3f, 0.0f, 0.0f, 1.0f);
        gl.glClear(GL2GL3.GL_COLOR_BUFFER_BIT | GL2GL3.GL_DEPTH_BUFFER_BIT);

        gl.glUniformMatrix4fv(locLightView, 1, false, lightCamera.getViewMatrix().floatArray(), 0);
        gl.glUniformMatrix4fv(locLightProj, 1, false, projLight.floatArray(), 0);

        gl.glUniform1i(locModeLight, 0);
        buffers.draw(GL2GL3.GL_TRIANGLES, shaderProgramLight);
    }

    private void renderFromViewer(GL2GL3 gl) {
        gl.glUseProgram(shaderProgramViewer);

        gl.glBindFramebuffer(GL2GL3.GL_FRAMEBUFFER, 0);
        gl.glViewport(0, 0, width, height);

        gl.glClearColor(0.0f, 0.3f, 0.0f, 1.0f);
        gl.glClear(GL2GL3.GL_COLOR_BUFFER_BIT | GL2GL3.GL_DEPTH_BUFFER_BIT);

        time += 0.1;
        gl.glUniform1f(locTime, time);

        gl.glUniformMatrix4fv(locView, 1, false, camera.getViewMatrix().floatArray(), 0);
        gl.glUniformMatrix4fv(locProjection, 1, false, projViewer.floatArray(), 0);
        gl.glUniformMatrix4fv(locLightVP, 1, false, lightCamera.getViewMatrix().mul(projLight).floatArray(), 0);
        gl.glUniform3fv(locEyePosition, 1, ToFloatArray.convert(camera.getPosition()), 0);
        gl.glUniform3fv(locLightPosition, 1, ToFloatArray.convert(lightCamera.getPosition()), 0);

        texture.bind(shaderProgramViewer, "textureID", 0);
        //renderTarget.getColorTexture().bind(shaderProgram, "colorTexture", 0);
        renderTarget.getDepthTexture().bind(shaderProgramViewer, "depthTexture", 1);

        if (withBorder) {
            gl.glPolygonMode(GL2GL3.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
        } else {
            gl.glPolygonMode(GL2GL3.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
        }

        // renderuj stěnu
        gl.glUniform1i(locMode, 0);
        buffers.draw(GL2GL3.GL_TRIANGLES, shaderProgramViewer);

        // renderuj elipsoid
        gl.glUniform1i(locMode, 1);
        buffers.draw(GL2GL3.GL_TRIANGLES, shaderProgramViewer);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
        textRenderer.updateSize(width, height);

        double ratio = height / (double) width;
        projLight = new Mat4OrthoRH(5 / ratio, 5, 0.1, 20);
//        projViewer = new Mat4OrthoRH(5 / ratio, 5, 0.1, 20);
        projViewer = new Mat4PerspRH(Math.PI / 3, ratio, 1, 20.0);
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
        mx = e.getX();
        my = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        camera = camera.addAzimuth(Math.PI * (mx - e.getX()) / width);
        camera = camera.addZenith(Math.PI * (e.getY() - my) / width);
        mx = e.getX();
        my = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
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