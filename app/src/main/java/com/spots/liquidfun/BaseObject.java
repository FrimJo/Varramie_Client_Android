package com.spots.liquidfun;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleSystem;

import javax.microedition.khronos.opengles.GL10;


public class BaseObject {

    public Color3 color = new Color3(255, 255, 255);
    public boolean visible = true;

    private int id;
    protected Body body = null;

    protected Vec2 position = new Vec2(0.0f, 0.0f);
    protected float rotation = 0.0f;
    protected FloatBuffer vertBuffer;
    protected float[] vertices;

    // Saved for when body is recreated on a vert refresh
    protected float friction;
    protected float density;
    protected float restitution;

    protected int positionHandle = GLES20.glGetAttribLocation(Renderer.getShaderProg(), "Position");
    protected int colorHandle = GLES20.glGetUniformLocation(Renderer.getShaderProg(), "Color");
    protected int modelHandle = GLES20.glGetUniformLocation(Renderer.getShaderProg(), "ModelView");

    public BaseObject() {

        //this.id = Renderer.getNextId();
        Renderer.actors.add(this);
    }

    public void setVertices(float[] _vertices) {


        this.vertices = _vertices;

        // Allocate a new byte buffer to move the vertices into a FloatBuffer
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertBuffer = byteBuffer.asFloatBuffer();
        vertBuffer.put(vertices);
        vertBuffer.position(0);

        if (body != null) {
            destroyPhysicsBody();
            createPhysicsBody(density, friction, restitution);
        }
    }

    public void draw(GL10 unused) {

        if (!visible) {
            return;
        }

        // Update local data from physics engine, if applicable
        if (body != null) {
            position = Renderer.worldToScreen(body.getPosition());
            rotation = body.getAngle() * 57.2957795786f;
        }

        // Construct mvp to be applied to every vertex
        float[] modelView = new float[16];

        Matrix.setIdentityM(modelView, 0);
        Matrix.translateM(modelView, 0, position.x, position.y, 1.0f);
        Matrix.rotateM(modelView, 0, rotation, 0, 0, 1.0f);

        // Load our matrix and color into our shader
        GLES20.glUniformMatrix4fv(modelHandle, 1, false, modelView, 0);
        GLES20.glUniform4fv(colorHandle, 1, color.toFloatArray(), 0);

        // Set up pointers, and draw using our vertBuffer
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertices.length / 3);
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    public void createPhysicsBody(float _density, float _friction, float _restitution) {

        if (body != null) {
            return;
        }

        // Save values
        friction = _friction;
        density = _density;
        restitution = _restitution;

        // Create the body
        BodyDef bd = new BodyDef();

        if (density > 0) {
            bd.type = BodyType.DYNAMIC;
        } else {
            bd.type = BodyType.STATIC;
        }

        bd.position = Renderer.screenToWorld(position);

        // Add to physics world body creation queue, will be finalized when possible
        Physics.requestBodyCreation(new BodyQueueDef(id, bd));
    }

    public void destroyPhysicsBody() {

        if (body == null) {
            return;
        }

        Physics.destroyBody(body);
        body = null;
    }

    public void onBodyCreation(Body _body) {

        // Threads ftw
        body = _body;

        // Body has been created, make fixture and finalize it
        // Physics world waits for completion before continuing

        // Create fixture from vertices
        PolygonShape shape = new PolygonShape();

        Vec2[] verts = new Vec2[vertices.length / 3];

        int vertIndex = 0;
        for (int i = 0; i < vertices.length; i += 3) {
            verts[vertIndex] = new Vec2(vertices[i] / Renderer.getPPM(), vertices[i + 1] / Renderer.getPPM());
            vertIndex++;
        }

        shape.set(verts, verts.length);

        // Attach fixture
        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = density;
        fd.friction = friction;
        fd.restitution = restitution;

        body.createFixture(fd);
    }

    // Modify the actor or the body
    public void setPosition(Vec2 position) {
        if (body == null) {
            this.position = position;
        } else {
            body.setTransform(Renderer.screenToWorld(position), body.getAngle());
        }
    }

    // Modify the actor or the body
    public void setRotation(float rotation) {
        if (body == null) {
            this.rotation = rotation;
        } else {
            body.setTransform(body.getPosition(), rotation * 0.0174532925f); // Convert to radians
        }
    }

    // Get from the physics body if avaliable
    public Vec2 getPosition() {
        if (body == null) {
            return position;
        } else {
            return Renderer.worldToScreen(body.getPosition());
        }
    }

    public float getRotation() {
        if (body == null) {
            return rotation;
        } else {
            return body.getAngle() * 57.2957795786f;
        }
    }

    public int getId() {
        return id;
    }

}
