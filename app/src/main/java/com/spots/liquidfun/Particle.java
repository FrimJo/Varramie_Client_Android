package com.spots.liquidfun;

import android.opengl.GLES20;
import android.opengl.Matrix;

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


    private float[] vertices;
    private FloatBuffer vertBuffer;
    public Vec2 position = new Vec2(0.0f,0.0f);
    public float size = Physics.PARTICLE_RADIUS;
    public float rotation = 0.0f;
    public Color3 color = new Color3(0, 0, 255);
    public float alpha = 1.0f;

    private int positionHandle = GLES20.glGetAttribLocation(Renderer.getShaderProg(), "Position");
    private int colorHandle = GLES20.glGetUniformLocation(Renderer.getShaderProg(), "Color");
    private int modelHandle = GLES20.glGetUniformLocation(Renderer.getShaderProg(), "ModelView");
    private int pointHandle = GLES20.glGetUniformLocation(Renderer.getShaderProg(), "PointSize");

    public Particle(float size){

        // 1 point, 3 coords, 3 elements, 'over 9000!' problems
        vertices = new float[3];
        this.size = size;
        refreshVertices();
    }

    private void refreshVertices() {

        // Modify our own vertex array, and pass it to setVertices
        vertices[0] = 0.0f;
        vertices[1] = 0.0f;
        vertices[2] = 0.0f;

        // Update!
        setVertices(vertices);
    }


    public void setVertices(float[] _vertices) {


        vertices = _vertices;

        // Allocate a new byte buffer to move the vertices into a FloatBuffer
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertBuffer = byteBuffer.asFloatBuffer();
        vertBuffer.put(vertices);
        vertBuffer.position(0);  // Index out of bounds?
    }

    public void draw(GL10 unused) {

        // Construct mvp to be applied to every vertex
        float[] modelView = new float[16];

        // Equivalent of gl.glLoadIdentity()
        Matrix.setIdentityM(modelView, 0);

        // gl.glTranslatef()
        Matrix.translateM(modelView, 0, position.x, position.y, 1.0f);

        // gl.glRotatef()
        Matrix.rotateM(modelView, 0, rotation, 0, 0, 1.0f);

        // Load our matrix and color into our shader
        GLES20.glUniformMatrix4fv(modelHandle, 1, false, modelView, 0);
        float[] colorf = color.toFloatArray();
        colorf[3] = alpha;
        GLES20.glUniform4fv(colorHandle, 1, colorf, 0);
        GLES20.glUniform1f(pointHandle, size);

        // Set up pointers, and draw using our vertBuffer as before

        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertices.length / 3);
        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}
