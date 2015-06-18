package com.spots.varramie;

import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by fredrikjohansson on 15-06-12.
 */
public class Circle {


    private final int COORDS_PER_VERTEX = 3;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex


    private FloatBuffer vertexBuffer;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private float[] mTranslationMatrix = new float[16];
    private float[] mScaleMatrix = new float[16];


    private float[] _coords;
    private float _glColor[];
    private volatile float _dx = 0.0f;
    private volatile float _dy = 0.0f;
    private volatile float _pressure = 0.0f;
    private int vertexCount = 0;
    //private LinkedBlockingDeque<TouchState> touchStates = new LinkedBlockingDeque<>();

    public Circle(float cx, float cy, float r, int num_segments, float[] color){
        _coords = createCircle(cx, cy, r, num_segments);
        _glColor = color;
        vertexCount = _coords.length / COORDS_PER_VERTEX;
        //touchStates.add(new TouchState(OpCodes.ACTION_UP, 0.0f, 0.0f, 0.32f));
        setupOpenGL();
    }

    public Circle(float[] coords, float[] color){
        _coords = coords;
        _glColor = color;
        vertexCount = _coords.length / COORDS_PER_VERTEX;
        //touchStates.add(new TouchState(OpCodes.ACTION_UP, 0.0f, 0.0f, 0.32f));
        setupOpenGL();
    }

    public float getX(){
        return _dx;
    }

    public float getY(){
        return _dy;
    }

    public float getPressure(){
        return _pressure;
    }

    public static float[] createCircle(float cx, float cy, float r, int num_segments){
        float[] coords = new float[num_segments * 3];

        double theta = 2.0f * Math.PI / (double) num_segments;
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

    public void set(final float x, final float y, final float pressure){
        //TouchState t = new TouchState(OpCodes.ACTION_UP, x, y, pressure);
        //touchStates.add(t);
        _dx = x;
        _dy = y;
        _pressure = (float) Math.pow(pressure/0.29,4.0);
    }

//    public float getX(){
//        return _dx;
//    }

    /*public float getY(){
        return _dy;
    }*/

    public void setupOpenGL(){
        // Setup vertex array buffer. Vertices in float. A float has 4 bytes
        ByteBuffer bb = ByteBuffer.allocateDirect(_coords.length * 4);
        bb.order(ByteOrder.nativeOrder());  // use the device hardware's native byte order
        vertexBuffer = bb.asFloatBuffer();  // create a floating point buffer from the ByteBuffer
        vertexBuffer.put(_coords);          // add the coordinates to the FloatBuffer
        vertexBuffer.position(0);           // set the buffer to read the first coordinate
    }

    public void draw(final float[] mvpMatrix, final float alpha, int mProgram) {
        /*TouchState t;
        float pressure, x, y;
        if(touchStates.size() > 1)
            t = touchStates.poll();
        else
            t = touchStates.peek();*/

        float pressure = _pressure;
        float x = _dx;
        float y = _dy;


        _glColor[3] = alpha;



        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");    // get handle to vertex shader's vPosition member
        GLES20.glEnableVertexAttribArray(mPositionHandle);                      // Enable a handle to the circle vertices
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer); // Prepare the triangle coordinate data

        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");     // get handle to fragment shader's vColor member
        GLES20.glUniform4fv(mColorHandle, 1, _glColor , 0);                 // Set color for drawing the circle

        Matrix.setIdentityM(mTranslationMatrix, 0);
        Matrix.translateM(mTranslationMatrix, 0, x, y, 0.0f);
        Matrix.multiplyMM(mTranslationMatrix, 0, mvpMatrix, 0, mTranslationMatrix, 0);


        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.scaleM(mScaleMatrix, 0, pressure, pressure, 0.0f);
        Matrix.multiplyMM(mScaleMatrix, 0, mTranslationMatrix, 0, mScaleMatrix, 0);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix"); // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mScaleMatrix, 0);

        // Set the line width of the circle
        //GLES20.glEnable(GL10.GL_LINE_SMOOTH);
        //GLES20.glLineWidth(8.0f); // 1 -> 8

        //GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);   // Draw the circle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);   // Draw the circle
        GLES20.glDisableVertexAttribArray(mPositionHandle);         // Disable vertex array

    }
}
