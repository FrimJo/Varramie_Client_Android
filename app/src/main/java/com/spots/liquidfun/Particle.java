package com.spots.liquidfun;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.spots.varramie.R;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.particle.ParticleGroup;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by fredrikjohansson on 15-06-18.
 */
public class Particle {


    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] mModelMatrix = new float[16];







    //private FloatBuffer vertBuffer;
    public float size = Physics.PARTICLE_RADIUS;


    private FloatBuffer mCubeTextureCoordinates;  // Store our model data in a float buffer.
    private FloatBuffer mCubePositions;  // Store our model data in a float buffer.
    private FloatBuffer mCubeColors;
    private FloatBuffer mCubeNormals;

    private final int mBytesPerFloat = 4;
    private final int mPositionDataSize = 3;
    private final int mColorDataSize = 4;
    private final int mNormalDataSize = 3;
    private final int mTextureCoordinateDataSize = 2;

    // S, T (or X, Y)
    // Texture coordinate data.
    // Because images have a Y axis pointing downward (values increase as you move down the image) while
    // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
    // What's more is that the texture coordinates are the same for every face.
    private final float[] cubeTextureCoordinateData;
    private final float[] cubePositionData;
    private final float[] cubeColorData;
    private final float[] cubeNormalData;

    private Vec2 position;
    private float rotation;


    public Particle(float _size_world, Vec2 _position, float _rotation){

        // 1 point, 3 coords, 3 elements, 'over 9000!' problems
        cubePositionData = new float[18];
        cubeTextureCoordinateData = new float[12];
        cubeColorData = new float[24];
        cubeNormalData = new float[18];
        position = _position;
        size = _size_world;
        rotation = _rotation;

        refreshVertices();
    }

    private void refreshVertices() {

        // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle,
        // if the points are counter-clockwise we are looking at the "front". If not we are looking at
        // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
        // usually represent the backside of an object and aren't visible anyways.
        cubePositionData[0] = -1.0f;
        cubePositionData[1] = 1.0f;
        cubePositionData[2] = 1.0f;

        cubePositionData[3] = -1.0f;
        cubePositionData[4] = -1.0f;
        cubePositionData[5] = 1.0f;

        cubePositionData[6] = 1.0f;
        cubePositionData[7] = 1.0f;
        cubePositionData[8] = 1.0f;

        cubePositionData[9] = -1.0f;
        cubePositionData[10] = -1.0f;
        cubePositionData[11] = 1.0f;

        cubePositionData[12] = 1.0f;
        cubePositionData[13] = -1.0f;
        cubePositionData[14] = 1.0f;

        cubePositionData[15] = 1.0f;
        cubePositionData[16] = 1.0f;
        cubePositionData[17] = 1.0f;


        // Front face
        cubeTextureCoordinateData[0] = 0.0f;
        cubeTextureCoordinateData[1] = 0.0f;

        cubeTextureCoordinateData[2] = 0.0f;
        cubeTextureCoordinateData[3] = 1.0f;

        cubeTextureCoordinateData[4] = 1.0f;
        cubeTextureCoordinateData[5] = 0.0f;

        cubeTextureCoordinateData[6] = 0.0f;
        cubeTextureCoordinateData[7] = 1.0f;

        cubeTextureCoordinateData[8] = 1.0f;
        cubeTextureCoordinateData[9] = 1.0f;

        cubeTextureCoordinateData[10] = 1.0f;
        cubeTextureCoordinateData[11] = 0.0f;

        // Front face
        cubeColorData[0] = 0.0f;
        cubeColorData[1] = 0.0f;
        cubeColorData[2] = 1.0f;
        cubeColorData[3] = 1.0f;

        cubeColorData[4] = 0.0f;
        cubeColorData[5] = 0.0f;
        cubeColorData[6] = 1.0f;
        cubeColorData[7] = 1.0f;

        cubeColorData[8] = 0.0f;
        cubeColorData[9] = 0.0f;
        cubeColorData[10] = 1.0f;
        cubeColorData[11] = 1.0f;

        cubeColorData[12] = 0.0f;
        cubeColorData[13] = 0.0f;
        cubeColorData[14] = 1.0f;
        cubeColorData[15] = 1.0f;

        cubeColorData[16] = 0.0f;
        cubeColorData[17] = 0.0f;
        cubeColorData[18] = 1.0f;
        cubeColorData[19] = 1.0f;

        cubeColorData[20] = 0.0f;
        cubeColorData[21] = 0.0f;
        cubeColorData[22] = 1.0f;
        cubeColorData[23] = 1.0f;

        // Front face
        cubeNormalData[0] = 0.0f;
        cubeNormalData[1] = 0.0f;
        cubeNormalData[2] = 1.0f;

        cubeNormalData[3] = 0.0f;
        cubeNormalData[4] = 0.0f;
        cubeNormalData[5] = 1.0f;

        cubeNormalData[6] = 0.0f;
        cubeNormalData[7] = 0.0f;
        cubeNormalData[8] = 1.0f;

        cubeNormalData[9] = 0.0f;
        cubeNormalData[10] = 0.0f;
        cubeNormalData[11] = 1.0f;

        cubeNormalData[12] = 0.0f;
        cubeNormalData[13] = 0.0f;
        cubeNormalData[14] = 1.0f;

        cubeNormalData[15] = 0.0f;
        cubeNormalData[16] = 0.0f;
        cubeNormalData[17] = 1.0f;


        // Initialize the buffers.
        mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions.put(cubePositionData).position(0);

        mCubeColors = ByteBuffer.allocateDirect(cubeColorData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeColors.put(cubeColorData).position(0);

        mCubeNormals = ByteBuffer.allocateDirect(cubeNormalData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeNormals.put(cubeNormalData).position(0);

        mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);
    }

    public void draw(GL10 unused) {

        // Draw some cubes.
        /*Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, position.x, position.y, 1.0f);
        Matrix.rotateM(mModelMatrix, 0, rotation, 0.0f, 0.0f, 1.0f);*/
        //Matrix.scaleM(mModelMatrix, 0, size, size, 0.0f);

        /*Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 4.0f, 0.0f, -7.0f);
        Matrix.rotateM(mModelMatrix, 0, 0.0f, 1.0f, 0.0f, 0.0f);*/

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -4.0f, -7.0f);

        // Pass in the position information
        mCubePositions.position(0);
        GLES20.glVertexAttribPointer(Renderer.mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                0, mCubePositions);

        GLES20.glEnableVertexAttribArray(Renderer.mPositionHandle);

        // Pass in the color information
        mCubeColors.position(0);
        GLES20.glVertexAttribPointer(Renderer.mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                0, mCubeColors);

        GLES20.glEnableVertexAttribArray(Renderer.mColorHandle);

        // Pass in the normal information
        mCubeNormals.position(0);
        GLES20.glVertexAttribPointer(Renderer.mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false,
                0, mCubeNormals);

        GLES20.glEnableVertexAttribArray(Renderer.mNormalHandle);

        // Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(Renderer.mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinates);

        GLES20.glEnableVertexAttribArray(Renderer.mTextureCoordinateHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(Renderer.mMVPMatrix, 0, Renderer.mViewMatrix, 0, mModelMatrix, 0);

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(Renderer.mMVMatrixHandle, 1, false, Renderer.mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(Renderer.mMVPMatrix, 0, Renderer.mProjectionMatrix, 0, Renderer.mMVPMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(Renderer.mMVPMatrixHandle, 1, false, Renderer.mMVPMatrix, 0);

        // Pass in the light position in eye space.
        GLES20.glUniform3f(Renderer.mLightPosHandle, Renderer.mLightPosInEyeSpace[0], Renderer.mLightPosInEyeSpace[1], Renderer.mLightPosInEyeSpace[2]);

        // Draw the particle.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

    }

}
