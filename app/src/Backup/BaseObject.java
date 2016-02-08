package com.spots.liquidfun;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.spots.varramie.Client;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by fredrikjohansson on 15-06-17.
 */
public class BaseObject {

    public Color3   _color = new Color3(255.0f, 255.0f, 255.0f);
    public boolean  _visible = true;

    private int             _id;
    private Body            _body = null;
    private float           _friction;
    private float           _density;
    private float           _restitution;

    // Handlers
    private final int       _positionHandle = GLES20.glGetAttribLocation(Renderer.getShaderProg(), "Position");
    private final int       _colorHandle = GLES20.glGetAttribLocation(Renderer.getShaderProg(), "Color");
    private final int       _modelHandle = GLES20.glGetAttribLocation(Renderer.getShaderProg(), "ModelView");

    protected Vec2          _position = new Vec2(0.0f, 0.0f);
    protected float         _rotation = 0.0f;
    protected FloatBuffer   _vertBuffer;
    protected float[]       _vertices;

    public BaseObject() {
        _id = Renderer.getNextId();
        Renderer._actors.add(this);
    }

    public void draw(){
        if(!_visible) return;

        // Update local data from physical engine, if applicable
        if(_body != null){
            _position = Renderer.worldToScreen(_body.getPosition());
            _rotation = _body.getAngle() * 57.2957795786f;
        }

        // Construct modelView to be applied to every vertex
        float[] modelView = new float[16];

        // Equivalent of gl.glLoadIdentity()
        Matrix.setIdentityM(modelView, 0);

        // Move to where our object is positioned.
        Matrix.translateM(modelView, 0, _position.x, _position.y, 1.0f);

        // Set the angle on each axis, 0 on x and y, our angle on z
        Matrix.rotateM(modelView, 0, _rotation, 0.0f, 0.0f, 1.0f);

        // Load our matrix and color into shader
        GLES20.glUniformMatrix4fv(_modelHandle, 1, false, modelView, 0);
        GLES20.glUniform4fv(_colorHandle, 1, _color.toFloatArray(), 0);

        // Set up pointers, and draw using our vertBuffer as before
        GLES20.glVertexAttribPointer(_positionHandle, 3, GLES20.GL_FLOAT, false, 0, _vertBuffer);
        GLES20.glEnableVertexAttribArray(_positionHandle);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, _vertices.length / 3);
        GLES20.glDisableVertexAttribArray(_positionHandle);

        if(this.getId() == 3)
            Client.INSTANCE.println("x: " + _position.x + ", y: " + _position.y);

    }

    // Modify the actor or the body
    public void setPosition(final Vec2 position){
        if(_body == null)
            _position = position;
        else
            _body.setTransform(Renderer.screenToWorld(position), _body.getAngle());
    }

    // Modify the actor or the body
    public void setRoation(final float rotation){
        if(_body == null)
            _rotation = rotation;
        else
            _body.setTransform(_body.getPosition(), rotation * 0.0174532925f); // Convert to radians
    }

    // Get from the physics body if avaible
    public Vec2 getPosition(){
        if(_body == null)
            return _position;
        else
            return Renderer.worldToScreen(_body.getPosition());
    }
    public float getRotation(){
        if(_body == null)
            return _rotation;
        else
            return _body.getAngle() * 57.2957795786f;
    }

    public int getId(){ return _id; }

    public void setVertices(final float[] vertices){
        _vertices = vertices;

        // Allocate a new byte buffer to move the vertices into a FloatBuffer
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        _vertBuffer = byteBuffer.asFloatBuffer();
        _vertBuffer.put(_vertices);
        _vertBuffer.position(0);

        if(_body != null) {
            destroyPhysicsBody();
            createPhysicsBody(_density, _friction, _restitution);
        }
    }

    public void createPhysicsBody(final float density, final float friciton, final float restitution){

        // Cowardly refuse to continue if the body already exists
        if(_body != null)
            return;

        // Save values
        _friction = friciton;
        _density = density;
        _restitution = restitution;

        // Create the body
        BodyDef bd = new BodyDef();

        if(density > 0)
            bd.type = BodyType.DYNAMIC;
        else
            bd.type = BodyType.STATIC;

        // Oh jeez
        bd.position = Renderer.screenToWorld(_position);

        // Add to physics world body creation queue, will be finalized when possible
        Physics.requestBodyCreation(new BodyQueueDef(_id, bd));

    }

    public void destroyPhysicsBody(){
        if(_body == null) return;
        Physics.destroyBody(_body);
        _body = null;
    }

    public void onBodyCreation(final Body body){

        _body = body;

        // Body has been created, make fixture and finalize it
        // Physics world waits for completion before continuing

        // Create fixture from vertices
        PolygonShape shape = new PolygonShape();
        Vec2[] verts = new Vec2[_vertices.length / 3];

        int vertIndex = 0;
        for(int i = 0; i < _vertices.length; i += 3){
            verts[vertIndex] = new Vec2(_vertices[i] / Renderer.getPPM(), _vertices[i + 1] / Renderer.getPPM());
            vertIndex++;
        }

        shape.set(verts, verts.length);

        // Attach fixture
        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = _density;
        fd.friction = _friction;
        fd.restitution = _restitution;

        body.createFixture(fd);
    }
}
