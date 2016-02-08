package com.spots.liquidfun;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.spots.varramie.R;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by fredrikjohansson on 15-06-18.
 */
public class Particle {

    private final Vec2 position;
    private final Vec2 velocity;
    private final ParticleColor color;

    public float size = Physics.PARTICLE_RADIUS;
    public float rotation = 0.0f;

/*
    private final float[] positionData = {  0.0f, 0.0f, 0.0f };
    private final float[] textureCoordinateData = { 0.0f, 0.0f };
*/

    private final float[] positionData = {  0.0f, 0.0f, 0.0f };
/*
    private final float[] positionData = {  -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            0.5f, 0.5f, 0.0f,
            -0.5f, 0.5f, 0.0f,
    };
*/
    //private int mTextureDataHandle = loadTexture(Renderer.context, R.drawable.white_point);

    private FloatBuffer mPositions;


    private int modelHandle =               GLES20.glGetUniformLocation(Renderer.getShaderProg(), "ModelView");
    private int colorHandle =               GLES20.glGetUniformLocation(Renderer.getShaderProg(), "Color");

    private int positionHandle =            GLES20.glGetAttribLocation(Renderer.getShaderProg(), "Position");
    //private int mPointSizeHandle =          GLES20.glGetAttribLocation(Renderer.getShaderProg(), "a_Size");


    public Particle(Vec2 _position, Vec2 _velocity, ParticleColor _color, float _size){

        size = _size;
        position = _position;
        velocity = _velocity;
        color = _color;
        mPositions = ByteBuffer.allocateDirect(positionData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(positionData);
        mPositions.position(0);


    }



    public void draw(GL10 unused) {

        // Construct mvp to be applied to every vertex
        float[] modelView = new float[16];

        Matrix.setIdentityM(modelView, 0);
        Vec2 position_screen = Renderer.worldToScreen(position);
        Matrix.translateM(modelView, 0, position_screen.x, position_screen.y, 1.0f);
        Matrix.rotateM(modelView, 0, rotation, 0, 0, 1.0f);
        Matrix.scaleM(modelView, 0, size*1.5f, size*1.5f, 1.0f);

        // Uniforms  (Projection is made in Renderer)
        GLES20.glUniformMatrix4fv(modelHandle, 1, false, modelView, 0);     // ModelView
        float[] lColor = new float[] { (color.r & 0xFF)/255.0f, (color.g & 0xFF)/255.0f, (color.b & 0xFF)/255.0f, (color.a & 0xFF)/255.0f };
        GLES20.glUniform4fv(colorHandle, 1, lColor, 0);                     // Color


        // Atribute pointers
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, mPositions);                         // Position

        //GLES20.glVertexAttribPointer(mPointSizeHandle, 1, GLES20.GL_FLOAT, false, 0, 20);                               // a_size

        GLES20.glEnableVertexAttribArray(positionHandle);

        //GLES20.glEnableVertexAttribArray(mPointSizeHandle);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, positionData.length / 3);
        //GLES20.glDrawArrays(GLES20.GL_POINTS, 0, positionData.length / 3);


        GLES20.glDisableVertexAttribArray(positionHandle);

        //GLES20.glDisableVertexAttribArray(mPointSizeHandle);
    }
}
