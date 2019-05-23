package uloha2.parallax;

import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import oglutils.*;
import transforms.Camera;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Vec3D;
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
public class Renderer implements GLEventListener, MouseListener, MouseMotionListener, KeyListener
{
    private boolean withBorder = false;
    private int width, height;
    private float time;
    private Camera camera;
    private int mx, my;
    private OGLTextRenderer textRenderer;

    private OGLBuffers buffers;
    private OGLTexture diffTex, normTex, heightTex;

    private int shaderGrid, locMatGrid, locTimeGrid, locEyeGrid,
            locLightGrid, locLightDirGrid, locLightCutoffGrid, locLightDistGrid;

    private Mat4 proj;

    private String text;
    private Robot robot; // pro zjištění barvy
    private Color color; // barva do textového výpisu

    @Override
    public void init(GLAutoDrawable drawable) {
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

        GL2GL3 gl = drawable.getGL().getGL2GL3();
        textRenderer = new OGLTextRenderer(gl, drawable.getSurfaceWidth(), drawable.getSurfaceHeight());

        System.out.println("Init GL is " + gl.getClass().getName());
        System.out.println("OpenGL version " + gl.glGetString(GL2GL3.GL_VERSION));
        System.out.println("OpenGL vendor " + gl.glGetString(GL2GL3.GL_VENDOR));
        System.out
                .println("OpenGL renderer " + gl.glGetString(GL2GL3.GL_RENDERER));
        System.out.println("OpenGL extension "
                + gl.glGetString(GL2GL3.GL_EXTENSIONS));

        shaderGrid = ShaderUtils.loadProgram(gl, "/uloha2/parallax/start");
        createBuffers(gl);
        // create buffers vertex a index
        buffers = GridFactory.generateGrid(gl, 50, 50, "inParamPos");

        // vytvoření kamery (uživatel)
        camera = new Camera()
                .withPosition(new Vec3D(0, 0, 0))
                .addAzimuth(5 / 4. * Math.PI)//-3/4.
                .addZenith(-1 / 5. * Math.PI)
                .withFirstPerson(false)
                .withRadius(5);

        createTexture(gl);

        locMatGrid = gl.glGetUniformLocation(shaderGrid, "mat");
        locLightGrid = gl.glGetUniformLocation(shaderGrid, "lightPos");
        locEyeGrid = gl.glGetUniformLocation(shaderGrid, "eyePos");
        locTimeGrid = gl.glGetUniformLocation(shaderGrid, "time");
        locLightDirGrid = gl.glGetUniformLocation(shaderGrid, "lightDir");
        locLightCutoffGrid = gl.glGetUniformLocation(shaderGrid, "lightCutoff");
        locLightDistGrid = gl.glGetUniformLocation(shaderGrid, "lightDist");

        gl.glEnable(GL2GL3.GL_DEPTH_TEST);

    }

    void createBuffers(GL2GL3 gl) {
        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 3),
                new OGLBuffers.Attrib("inNormal", 3)
        };
    }

    void createTexture(GL2GL3 gl) {
        String texTmp = "/textures/bricks";
        diffTex = new OGLTexture2D(gl, texTmp + ".jpg");
        normTex = new OGLTexture2D(gl, texTmp + "n.png");
        heightTex = new OGLTexture2D(gl, texTmp + "h.png");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {
        time += 0.01;

        GL2GL3 gl = drawable.getGL().getGL2GL3();

        gl.glPolygonMode(GL2GL3.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);

        gl.glBindFramebuffer(GL2GL3.GL_FRAMEBUFFER, 0);
        gl.glViewport(0, 0, width, height);

        gl.glClearColor(0.2f, 0.1f, 0.2f, 1.0f);
        gl.glClear(GL2GL3.GL_COLOR_BUFFER_BIT | GL2GL3.GL_DEPTH_BUFFER_BIT);

        gl.glUseProgram(shaderGrid);
        gl.glUniformMatrix4fv(locMatGrid, 1, false,
                ToFloatArray.convert(camera.getViewMatrix().mul(proj)), 0);
        gl.glUniform3fv(locEyeGrid, 1, ToFloatArray.convert(camera.getEye()), 0);
        gl.glUniform1f(locTimeGrid, time);

        gl.glUniform3fv(locLightGrid, 1, ToFloatArray.convert(
                new Vec3D(5, 5, 1.5)
        ), 0);
        gl.glUniform3fv(locLightDirGrid, 1, ToFloatArray.convert(
                new Vec3D(-5, -5, 0)
        ), 0);

        gl.glUniform1f(locLightDistGrid, 20);
        gl.glUniform1f(locLightCutoffGrid, 0.8f);

        if (withBorder) {
            gl.glPolygonMode(GL2GL3.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
        } else {
            gl.glPolygonMode(GL2GL3.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
        }

        diffTex.bind(shaderGrid, "diffTex", 0);
        normTex.bind(shaderGrid, "normTex", 1);
        heightTex.bind(shaderGrid, "heightTex", 2);

        buffers.draw(GL2GL3.GL_TRIANGLES, shaderGrid);
        textRenderer.drawStr2D(3, height - 20, text);
        textRenderer.drawStr2D(width - 90, 3, " (c) PGRF UHK");
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
        proj = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.01, 1000.0);
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