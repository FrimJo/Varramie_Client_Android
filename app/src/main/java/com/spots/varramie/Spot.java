package com.spots.varramie;


import android.graphics.Color;
import android.graphics.Point;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.SparseArray;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.microedition.khronos.opengles.GL10;

public class Spot implements Iterable<Point>{

	// Open GL stuff
	private static float[] mTranslationMatrix = new float[16];
	private static final double PI = Math.PI;
	private FloatBuffer vertexBuffer;
	static final int COORDS_PER_VERTEX = 3;




	private final String vertexShaderCode =
			// This matrix member variable provides a hook to manipulate
			// the coordinates of the objects that use this vertex shader
			"uniform mat4 uMVPMatrix;" +
					"attribute vec4 vPosition;" +
					"void main() {" +
					// the matrix must be included as a modifier of gl_Position
					// Note that the uMVPMatrix factor *must be first* in order
					// for the matrix multiplication product to be correct.
					"  gl_Position = uMVPMatrix * vPosition;" +
					"}";

	// Use to access and set the view transformation
	private int mMVPMatrixHandle;

	private final String fragmentShaderCode =
			"precision mediump float;" +
					"uniform vec4 vColor;" +
					"void main() {" +
					"  gl_FragColor = vColor;" +
					"}";

	private final int mProgram = GLES20.glCreateProgram();
	private int mPositionHandle;
	private int mColorHandle;




	private static boolean defaultHiden = false;
	private static Spot mySpot;
	private static int mySpotId;

	private static final float CIRCLE_RADIUS = 80.0f;
	private static final int ARRAY_SIZE = 256;
	//private static final SparseArray<Spot> allSpots = new SparseArray<>();
	private static final Spot[] allSpots = new Spot[ARRAY_SIZE];


	private float[] circle = MakeCircle(0.0f, 0.0f, CIRCLE_RADIUS, 30);
	//private final CircleArray<PointFloat> pointArray = new CircleArray<>(10, PointFloat.class);
	private boolean active = false;
	private boolean hidden = defaultHiden;
	private float glColor[] = ColorManager.getRealRandomColor();
	private int id;
	//private float glColor[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

	private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
	private final int vertexCount = circle.length / COORDS_PER_VERTEX;

	public Spot() {
		this.id = '\0';
		this.hidden = false;
		ByteBuffer bb = ByteBuffer.allocateDirect(
		// (number of coordinate values * 4 bytes per float)
			circle.length * 4);

		// use the device hardware's native byte order
		bb.order(ByteOrder.nativeOrder());

		// create a floating point buffer from the ByteBuffer
		vertexBuffer = bb.asFloatBuffer();
		// add the coordinates to the FloatBuffer
		vertexBuffer.put(circle);
		// set the buffer to read the first coordinate
		vertexBuffer.position(0);

		int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
				vertexShaderCode);
		int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
				fragmentShaderCode);

		// create empty OpenGL ES Program
		//mProgram = GLES20.glCreateProgram();

		// add the vertex shader to program
		GLES20.glAttachShader(mProgram, vertexShader);

		// add the fragment shader to program
		GLES20.glAttachShader(mProgram, fragmentShader);

		// creates OpenGL ES program executables
		GLES20.glLinkProgram(mProgram);

	}

	public Spot(final int id){
		this.id = id;
		this.hidden = false;


		ByteBuffer bb = ByteBuffer.allocateDirect(
				// (number of coordinate values * 4 bytes per float)
				circle.length * 4);

		// use the device hardware's native byte order
		bb.order(ByteOrder.nativeOrder());

		// create a floating point buffer from the ByteBuffer
		vertexBuffer = bb.asFloatBuffer();
		// add the coordinates to the FloatBuffer
		vertexBuffer.put(circle);
		// set the buffer to read the first coordinate
		vertexBuffer.position(0);

		int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
				vertexShaderCode);
		int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
				fragmentShaderCode);

		// create empty OpenGL ES Program
		//mProgram = GLES20.glCreateProgram();

		// add the vertex shader to program
		GLES20.glAttachShader(mProgram, vertexShader);

		// add the fragment shader to program
		GLES20.glAttachShader(mProgram, fragmentShader);

		// creates OpenGL ES program executables
		GLES20.glLinkProgram(mProgram);
		allSpots[id] = this;
		//allSpots.put(id, this);
	}

	public synchronized void activate(final float x, final float y){
		update(x, y);
	}
	
	public synchronized void update(final float x, final float y){
		this.active = true;
		this._dx = x;
		this._dy = y;
		//this.pointArray.add(p);
	}
	
	public synchronized void deactivate(final float x, final float y){
		this.active = false;
	}
	
	public synchronized boolean isActive(){
		return (!this.hidden && (this.active ));
	}

	public synchronized void destroy(){
		allSpots[this.id] = null;
		//allSpots.remove(this.id);
	}
	
	public synchronized int getId(){
		return this.id;
	}

	public synchronized static Spot getSpotAt(int index){
		return allSpots[index];
		//return allSpots.valueAt(index);
	}
	
	//public synchronized static Spot getSpot(int key){
		//return allSpots.get(key);
	//}
	
	public synchronized static int sizeOfSpotsList(){
		return allSpots.length;

		//return allSpots.size();
	}

	public synchronized static Spot getMySpot(){
		return mySpot;
	}

	public synchronized  static void setMySpot(Spot s){
		mySpot = s;
	}
	public synchronized static int getMySpotId(){
		return mySpotId;
	}

	public synchronized static void setMySpotId(final int id){
		mySpotId = id;
	}
	
	public synchronized static void activateMySpot(final float x, final float y){
		mySpot.activate(x, y);
	}
	
	public synchronized static void updateMySpot(final float x, final float y){
		mySpot.update(x, y);
	}
	public synchronized static void deactivateMySpot(final float x, final float y){
		mySpot.deactivate(x, y);
	}

	public synchronized static void setIsHidden(boolean value){
		for(int i = 0; i < allSpots.length; i++){
			if(allSpots[i] != null)
				allSpots[i].hidden = value;
		}
		/*for(int i = 0; i < allSpots.size(); i++){
			allSpots.valueAt(i).hidden = value;
		}*/
	}

	public synchronized static void setDefaultHidden(boolean value){
		defaultHiden = value;
	}
	
	@Override
	public synchronized boolean equals(Object o){
		if(o == null)
			return false;
		
		if(o.getClass() != this.getClass())
			return false;
		
		Spot s = (Spot) o;
		
		if(s.getId() != this.id)
			return false;
		
		return true;
	}

	@Override
	public Iterator iterator() {
		//return this.pointArray.iterator();
		return null;
	}

	public float _dx = 0.0f;
	public float _dy = 0.0f;



	public void draw(float[] mvpMatrix) {

		// GL_LINE_SMOOTH (hint: enable blending).


		// Add program to OpenGL ES environment
		GLES20.glUseProgram(mProgram);

		// get handle to vertex shader's vPosition member
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false,
				vertexStride, vertexBuffer);

		// get handle to fragment shader's vColor member
		mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

		// Set color for drawing the circle
		GLES20.glUniform4fv(mColorHandle, 1, glColor, 0);

		Matrix.setIdentityM(mTranslationMatrix, 0);
		Matrix.translateM(mTranslationMatrix, 0, _dx, _dy, 0.0f);
		//Matrix.multiplyMM(mvpMatrix, 0, mOrtho, 0, mvpMatrix, 0);
		Matrix.multiplyMM(mTranslationMatrix, 0, mvpMatrix, 0, mTranslationMatrix, 0);

		// Pass the projection and view transformation to the shader
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mTranslationMatrix, 0);

		// Set the line width of the circle
		GLES20.glEnable(GL10.GL_LINE_SMOOTH);
		GLES20.glLineWidth(8.0f); // 1 -> 8


		// Draw the circle
		GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, vertexCount);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);

	}



	public static float[] MakeCircle(float cx, float cy, float r, int num_segments)
	{
		float[] coords = new float[num_segments * 3];


		double theta = 2.0f * PI / (double) num_segments;
		double c = Math.cos(theta);//precalculate the sine and cosine
		double s = Math.sin(theta);
		double t;

		double x = r;//we start at angle = 0
		double y = 0;


		for(int ii = 0; ii < num_segments; ii++)
		{
			int index = ii*3;
			coords[index] = (float) x + cx;
			coords[index + 1] = (float) y + cy;
			coords[index + 2] = 0.0f;

			//apply the rotation matrix
			t = x;
			x = c * x - s * y;
			y = s * t + c * y;
		}
		return coords;
	}

}
