package lvl1basic.p01start.p07utils;

import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import oglutils.OGLBuffers;
import oglutils.OGLTextRenderer;
import oglutils.OGLUtils;
import oglutils.ShaderUtils;
import transforms.Camera;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Vec3D;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

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
public class Renderer implements GLEventListener, MouseListener,
		MouseMotionListener, KeyListener {

	int width, height;

	OGLBuffers buffers;
	OGLTextRenderer textRenderer;

	private int shaderProgram, locTime, locView, locProj;

	float time = 0;
	private Mat4 proj;
	private Camera camera;
	private int mx, my;

	@Override
	public void init(GLAutoDrawable glDrawable) {
		// check whether shaders are supported
		GL2GL3 gl = glDrawable.getGL().getGL2GL3();
		OGLUtils.shaderCheck(gl);
		
		OGLUtils.printOGLparameters(gl);
		
		textRenderer = new OGLTextRenderer(gl, glDrawable.getSurfaceWidth(), glDrawable.getSurfaceHeight());
		
		// shader files are in /shaders/ directory
		// shaders directory must be set as a source directory of the project
		// e.g. in Eclipse via main menu Project/Properties/Java Build Path/Source
		
		shaderProgram = ShaderUtils.loadProgram(gl, "/lvl1basic/p01start/p07utils/start.vert",
				"/lvl1basic/p01start/p07utils/start.frag",
				null,null,null,null); 
		
		//shorter version of loading shader program
		//shaderProgram = ShaderUtils.loadProgram(gl, "/lvl1basic/p01start/p04utils/start");

		//createBuffers(gl);
		buffers = GridFactory.generateGrid(gl, 3, 3);

		/*camera = new Camera(new Vec3D(5, 5, 5),
				0, 0, 0, true);
*/

		camera = new Camera()
				.withPosition(new Vec3D(0, 0, 0))
				.addAzimuth(5 / 4. * Math.PI)//-3/4.
				.addZenith(-1 / 5. * Math.PI)
				.withFirstPerson(false)
				.withRadius(5);

		locTime = gl.glGetUniformLocation(shaderProgram, "time");
		locProj = gl.glGetUniformLocation(shaderProgram, "proj");
		locView = gl.glGetUniformLocation(shaderProgram, "view");
	}

	void createBuffers(GL2GL3 gl) {
		float[] vertexBufferData = {
			-1, -1, 	0.7f, 0, 0, 
			 1,  0,		0, 0.7f, 0,
			 0,  1,		0, 0, 0.7f 
		};
		int[] indexBufferData = { 0, 1, 2 };

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
		
		gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		gl.glClear(GL2GL3.GL_COLOR_BUFFER_BIT | GL2GL3.GL_DEPTH_BUFFER_BIT);
		
		// set the current shader to be used, could have been done only once (in
		// init) in this sample (only one shader used)
		gl.glUseProgram(shaderProgram); 
		time += 0.1;
		gl.glUniform1f(locTime, time); // correct shader must be set before this
		gl.glUniformMatrix4fv(locView,
				1,
				false,
				camera.getViewMatrix().floatArray(),
				0
		);
		gl.glUniformMatrix4fv(locProj,
				1,
				false,
				proj.floatArray(),
				0
		);
		
		// bind and draw
		buffers.draw(GL2GL3.GL_TRIANGLES, shaderProgram);
		
		String text = new String(this.getClass().getName());
		textRenderer.drawStr2D(3, height - 20, text);
		textRenderer.drawStr2D(width - 90, 3, " (c) PGRF UHK");

	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		this.width = width;
		this.height = height;
		proj = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.01, 1000.0);
		textRenderer.updateSize(width, height);
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
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void dispose(GLAutoDrawable glDrawable) {
		GL2GL3 gl = glDrawable.getGL().getGL2GL3();
		gl.glDeleteProgram(shaderProgram);
	}

}