package com.spots.liquidfun;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.spots.varramie.R;

import org.jbox2d.common.Vec2;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by fredrikjohansson on 15-06-18.
 */
public class Cluster {

    private final int id;
    private ParticleGroup _group = null;

    public Color3 color = new Color3(0, 0, 255);

    protected Vec2 world_position = new Vec2(0.0f, 0.0f);
    protected float rotation = 0.0f;

    //private Particle[] particles;

    private Vec2[] cords;
    private Vec2[] controllCords;
    private Vec2[] controllVelocity;
    private ParticleColor[] colors;
    private Vec2[] velocity;
    private Particle[] particles;


    public Cluster(ParticleGroup grp, int _id){


        id = _id;
        ClusterManager.allClusters.put(_id, this);
        _group = grp;
        cords = Physics.getParticles(grp);
        colors = Physics.getColors(grp);
        velocity = Physics.getVelocities(grp);
        particles = new Particle[cords.length];

        for(int i = 0; i < particles.length; i++){
            particles[i] = new Particle(Physics.PARTICLE_RADIUS, cords[i], _group.getAngle());
        }


        setVertices();



        //particles = new Particle[grp.getParticleCount()];
        /*Vec2 center = _group.getCenter();
        Vec2 pos;
        float dx, dy, alpha;
        double val, radius;

        for(int i = 0; i < particles.length; i++){
            pos = Renderer.worldToScreen(cords[i]);
            dx = Renderer.screenToWorld(pos.x) - center.x;
            dy = Renderer.screenToWorld(pos.y) - center.y;
            val = Math.pow(dx,2.0) + Math.pow(dy,2.0);
            radius = Math.pow(Renderer.screenToWorld(Physics.GROUP_RADIUS), 2.0);
            alpha = (float) (1.0 - val/radius);


            particles[i] = new Particle(Physics.PARTICLE_RADIUS);
            if(val > radius-Renderer.screenToWorld(Physics.PARTICLE_RADIUS)*3.0)
                particles[i].color = new Color3(0, 0, 0);
            else
                particles[i].color = new Color3(colors[i].r & 0xFF, colors[i].g & 0xFF, colors[i].b & 0xFF);
            particles[i].position = pos;
            particles[i].rotation = _group.getAngle() * 57.2957795786f;
            particles[i].alpha = alpha;

        }*/

    }
/*
    public void draw(GL10 unused) {

        if (particles.length != cords.length) return;


        // Update local data from physics engine
        position = Renderer.worldToScreen(_group.getPosition());
        rotation = _group.getAngle() * 57.2957795786f;
        float particle_radius = Renderer.worldToScreen(Physics.physicsWorld.getParticleRadius());

        for(int i = 0; i < particles.length; i++){
            Vec2 com_screen_pos = Renderer.worldToScreen(cords[i]);

            particles[i].position = com_screen_pos;
            particles[i].rotation = rotation;
            particles[i].size = particle_radius;
            particles[i].draw(unused);
        }


    }*/

    public void remote(Vec2 position_screen, float size, Vec2 _velocity){

        if(cords.length != velocity.length) return;

        Vec2 position_world = Renderer.screenToWorld(position_screen);
        Vec2 dif = position_world.subLocal(_group.getCenter()).mul(1.0f / -10.0f);

        for(int i = 0; i < controllCords.length; i++){
            controllCords[i].subLocal(dif);
            velocity[i].set(_velocity);
        }
    }

    public void push(Vec2 position_screen, float size){
        Vec2 position_world = Renderer.screenToWorld(position_screen);
        //Vec2 dif = position_world.subLocal(_group.getCenter()).mul(1.0f / -10.0f);

        if(controllVelocity.length != controllCords.length) return;

        for(int i = 0; i < controllVelocity.length; i++){
            Vec2 position = controllCords[i];
            Vec2 dif = position.sub(position_world).mulLocal(-3.0f);
            controllVelocity[i].set(dif);
        }
    }

    public void release(Vec2 _velocity, float size){

        for(int i = 0; i < velocity.length; i++)
            velocity[i].set(_velocity);
    }

    public void destroyPhysicsGroup() {
        ClusterManager.allClusters.remove(id);
        Physics.destroyGroup(_group);
        _group = null;
    }

    public int getId(){
        return id;
    }


    /**
     * The drawing of the cluster below
     *
     *
     * */
    private float[] vertices;

    private FloatBuffer vertBuffer;

    private List<Integer> outlineIndexL;
    private float[] outline_vertices;

    private void setVertices(){

        float dx, dy;
        Vec2 pos, center = _group.getCenter();
        double hyp, radius = Math.pow(Renderer.screenToWorld(Physics.GROUP_RADIUS) - Renderer.screenToWorld(Physics.PARTICLE_RADIUS)*1.5f, 2.0),
        radius_controll = Math.pow(Renderer.screenToWorld(Physics.GROUP_RADIUS / 2.0f), 2.0);


        List<Vec2> controllVectorL = new LinkedList<>();
        List<Vec2> controllVelocityL = new LinkedList<>();

        // Goes through all the particle coordinates in the group
        // and saves the indexes of the ones in the outer line.
        outlineIndexL = new LinkedList<>();
        for(int i = 0; i < cords.length; i++){
            pos = cords[i];
            dx = pos.x - center.x;
            dy = pos.y - center.y;
            hyp = Math.pow(dx,2.0) + Math.pow(dy,2.0);

            //if(hyp > radius){
            if(hyp < radius_controll){
                controllVectorL.add(cords[i]);
                controllVelocityL.add(velocity[i]);
            }else{
                outlineIndexL.add(i);
            }

        }
        controllCords = new Vec2[controllVectorL.size()];
        controllVectorL.toArray(controllCords);

        controllVelocity = new Vec2[controllVelocityL.size()];
        controllVelocityL.toArray(controllVelocity);


        // Creates the vertices array that will contain all outline vertices
        // and the center vertex of the group
        outline_vertices = new float[(outlineIndexL.size() /*+ 1*/) * 3];

        // Refresh!
        refreshVertices();

    }

    private void refreshVertices() {
        Vec2 center = Renderer.worldToScreen(_group.getCenter());

        // Populates the vertices array with all outline vertices
        // and the center vertex of the group using the indexes of
        // the out line index list
        /*outline_vertices[0] = center.x;
        outline_vertices[1] = center.y;
        outline_vertices[2] = 0.0f;*/
        int c = 0; //c=3
        float length = (outline_vertices.length)/2.0f;
        //float val = length % 3.0f;
        length += (length % 3.0f);
        int dist = Math.round(length);

        for(int i = 0; i < outlineIndexL.size(); ){
            Vec2 coord = Renderer.worldToScreen(cords[outlineIndexL.get(i++).intValue()]);
            outline_vertices[c] =   coord.x;
            outline_vertices[c+1] =   coord.y;
            outline_vertices[c+2] =   0.0f;

            if(i >= outlineIndexL.size()) break;

            coord = Renderer.worldToScreen(cords[outlineIndexL.get(i++).intValue()]);
            outline_vertices[dist + c] =   coord.x;
            outline_vertices[dist + c+1] =   coord.y;
            outline_vertices[dist + c+2] =   0.0f;
            c+=3;
        }

        // Update!
        updateVertices(outline_vertices);
    }

    public void updateVertices(float[] _vertices) {

        vertices = _vertices;

        // Allocate a new byte buffer to move the vertices into a FloatBuffer
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertBuffer = byteBuffer.asFloatBuffer();
        vertBuffer.put(vertices);
        vertBuffer.position(0);
    }

    private int[] textures = new int[1];


    public void draw(GL10 unused) {



        for(Particle p : particles){
            p.draw(unused);
        }

/*

        // Update local data from physics engine
        refreshVertices();

        // Construct mvp to be applied to every vertex
        float[] modelView = new float[16];



        Matrix.setIdentityM(modelView, 0);
        //Matrix.translateM(modelView, 0, world_position.x, world_position.y, 1.0f);
        //Matrix.rotateM(modelView, 0, rotation, 0, 0, 1.0f);

        // Load our matrix and color into our shader
        GLES20.glUniformMatrix4fv(modelHandle, 1, false, modelView, 0);
        //float[] colorf = color.toFloatArray();
        //colorf[3] = 1.0f;
        GLES20.glUniform4fv(colorHandle, 1, color.toFloatArray(), 0);

        outlineIndexL.size();

        GLES20.glUniform1f(pointHandle, Physics.PARTICLE_RADIUS);

        // Set up pointers, and draw using our vertBuffer
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);
        //GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertices.length / 3);
        //GLES20.glDisable(GLES20.GL_TEXTURE_2D);
        GLES20.glDisableVertexAttribArray(positionHandle);*/
    }
}
