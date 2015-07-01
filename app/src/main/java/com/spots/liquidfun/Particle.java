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
import org.jbox2d.particle.ParticleGroup;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by fredrikjohansson on 15-06-18.
 */
public class Particle {

    public Vec2 position = new Vec2(100.0f,100.0f);
    public float size = Physics.PARTICLE_RADIUS;
    public float rotation = 0.0f;
    public Color3 color = new Color3(0, 0, 255);
    public float alpha = 1.0f;


    private final float[] positionData = {  -0.5f, -0.5f, 0.0f,
                                        0.5f, -0.5f, 0.0f,
                                        0.5f, 0.5f, 0.0f,
                                        -0.5f, 0.5f, 0.0f,
    };

    private final float[] textureCoordinateData = { 0.0f, 1.0f,
                                            1.0f, 1.0f,
                                            1.0f, 0.0f,
                                            0.0f, 0.0f,
    };

    //private int mTextureDataHandle = loadTexture(Renderer.context, R.drawable.white_point);

    private FloatBuffer mPositions;
    private FloatBuffer mCubeTextureCoordinates;

    private int modelHandle =               GLES20.glGetUniformLocation(Renderer.getShaderProg(), "ModelView");
    private int colorHandle =               GLES20.glGetUniformLocation(Renderer.getShaderProg(), "Color");
    private int mTextureUniformHandle =     GLES20.glGetUniformLocation(Renderer.getShaderProg(), "u_Texture");

    private int positionHandle =            GLES20.glGetAttribLocation(Renderer.getShaderProg(), "Position");
    private int mTextureCoordinateHandle =  GLES20.glGetAttribLocation(Renderer.getShaderProg(), "a_TexCoordinate");


    public Particle(float _size){

        size = _size;
        mPositions = ByteBuffer.allocateDirect(positionData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(positionData);
        mPositions.position(0);

        mCubeTextureCoordinates = ByteBuffer.allocateDirect(textureCoordinateData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(textureCoordinateData);
        mCubeTextureCoordinates.position(0);

        loadTexture(Renderer.context, R.drawable.white_point);
    }

    public int loadTexture(final Context context, final int resourceId)
    {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;	// No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    public void draw(GL10 unused) {

        // Construct mvp to be applied to every vertex
        float[] modelView = new float[16];

        Matrix.setIdentityM(modelView, 0);
        Matrix.translateM(modelView, 0, position.x, position.y, 1.0f);
        Matrix.rotateM(modelView, 0, rotation, 0, 0, 1.0f);
        Matrix.scaleM(modelView, 0, size*1.5f, size*1.5f, 1.0f);

        float[] colorf = color.toFloatArray();
        colorf[3] = alpha;


        // Uniforms  (Projection is made in Renderer)
        GLES20.glUniformMatrix4fv(modelHandle, 1, false, modelView, 0);     // ModelView
        GLES20.glUniform4fv(colorHandle, 1, colorf, 0);                     // Color
        GLES20.glUniform1i(mTextureUniformHandle, 0);                       // u_texture


        // Atribute pointers
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, mPositions);                         // Position
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, mCubeTextureCoordinates);  // a_texture

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, positionData.length / 3);


        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
    }
}
