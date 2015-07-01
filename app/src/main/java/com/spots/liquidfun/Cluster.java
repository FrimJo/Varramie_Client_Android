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

    private final String id;
    private ParticleGroup _group = null;

    public Color3 color = new Color3(0, 0, 255);

    protected float rotation = 0.0f;

    private Vec2[] cords;
    private Vec2[] controllCords;
    private Vec2[] controllVelocity;
    private ParticleColor[] colors;
    private Vec2[] velocity;
    private Particle[] particles;


    public Cluster(ParticleGroup grp, String _id){
        id = _id;
        ClusterManager.allClusters.put(_id, this);
        _group = grp;
        cords = Physics.getParticles(grp);
        colors = Physics.getColors(grp);
        velocity = Physics.getVelocities(grp);
        particles = new Particle[cords.length];

        for(int i = 0; i < particles.length; i++){
            particles[i] = new Particle(Physics.PARTICLE_RADIUS);
        }

        setController();

    }

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

        Vec2 position = _group.getCenter();
        Vec2 dif = position.sub(position_world).mulLocal(-3.0f);

        for(int i = 0; i < controllVelocity.length; i++){
            controllVelocity[i].addLocal(dif);
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

    public String getId(){
        return id;
    }

    private void setController(){

        float dx, dy;
        Vec2 pos, center = _group.getCenter();
        double hyp, radius_controll = Math.pow(Renderer.screenToWorld(Physics.GROUP_RADIUS / 1.5f), 2.0);

        List<Vec2> controllVectorL = new LinkedList<>();
        List<Vec2> controllVelocityL = new LinkedList<>();

        //outlineCordsL.add(center);

        for(int i = 0; i < cords.length; i++){
            pos = cords[i];
            dx = pos.x - center.x;
            dy = pos.y - center.y;
            hyp = Math.pow(dx,2.0) + Math.pow(dy,2.0);

            if(hyp < radius_controll){
                controllVectorL.add(cords[i]);
                controllVelocityL.add(velocity[i]);
            }

        }
        controllCords = new Vec2[controllVectorL.size()];
        controllVectorL.toArray(controllCords);

        controllVelocity = new Vec2[controllVelocityL.size()];
        controllVelocityL.toArray(controllVelocity);

    }

    public void draw(GL10 unused) {

        // Update local data from physics engine
        rotation = _group.getAngle() * 57.2957795786f;
        float particle_radius = Renderer.worldToScreen(Physics.physicsWorld.getParticleRadius());

        for(int i = 0; i < particles.length; i++){
            Vec2 com_screen_pos = Renderer.worldToScreen(cords[i]);

            particles[i].position = com_screen_pos;
            particles[i].rotation = rotation;
            particles[i].size = particle_radius;
            particles[i].draw(unused);
        }
    }

}