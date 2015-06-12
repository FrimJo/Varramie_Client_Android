package com.spots.varramie;

import android.graphics.Point;
import android.opengl.EGLConfig;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by fredrikjohansson on 15-06-11.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Spot mySpot;
    private Spot testSpot;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(255.0f, 255.0f, 255.0f, 1.0f);
        for(int i = 0; i < 256; i++){
            new Spot(i);
        }
        mySpot = new Spot();
        Spot.setMySpot(mySpot);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Matrix.orthoM(mMVPMatrix, 0, 0, width, height, 0, -1, 1);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        for(int i = 0; i < Spot.sizeOfSpotsList(); i++){
            Spot s = Spot.getSpotAt(i);
            if(s.isActive())
                s.draw(mMVPMatrix);
        }

        if(mySpot != null && mySpot.isActive())
            mySpot.draw(mMVPMatrix);

    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}