package com.spots.liquidfun;

import android.graphics.Color;

import com.spots.varramie.Client;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.TimeStep;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleContact;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleGroupType;
import org.jbox2d.particle.ParticleSystem;
import org.jbox2d.particle.ParticleType;

import java.util.Vector;

public class Physics {

    // Defined public, should we need to modify them from elsewhere
    public static int velIterations = 8; // 6
    public static int posIterations = 3; // 6
    public static float PARTICLE_RADIUS = 15.0f;
    public static float GROUP_RADIUS = 150.0f;


    // This is private, since we need to set it in the physics world, so directly
    // modifying it from outside the class would bypass that. Why not set it
    // in the world directly? The world is in another thread :) It might also
    // stop and start back up again, so we need to have it saved.
    private static Vec2 gravity = new Vec2(0, 0);

    // Threads!
    public static PhysicsThread pThread = null;

    // The world itself
    public static World physicsWorld = null;


    // Our queues. Wonderful? I concur.
    private static final Vector<Body> bodyDestroyQ = new Vector<Body>();
    private static final Vector<BodyQueueDef> bodyCreateQ = new Vector<BodyQueueDef>();

    private static final Vector<ParticleGroup> groupDestroyQ = new Vector<>();
    private static final Vector<GroupQueueDef> groupCreateQ = new Vector<>();

    // We need to keep track of how many bodies exist, so we can stop the thread
    // when none are present, and start it up again when necessary
    private static int bodyCount = 0;
    private static int groupCount = 0;

    public static void requestBodyCreation(BodyQueueDef bq) {

        // Ship it to our queue
        bodyCreateQ.add(bq);

        if (pThread == null) {
            pThread = new PhysicsThread();
            pThread.start();
        }

        // Take note of the new body
        bodyCount++;
    }

    public static void requestGroupCreation(GroupQueueDef gq) {

        // Ship it to our queue
        groupCreateQ.add(gq);

        if (pThread == null) {
            pThread = new PhysicsThread();
            pThread.start();
        }

        // Take note of the new body
        groupCount++;
    }

    public static void destroyBody(Body body) {
        bodyDestroyQ.add(body);
    }

    public static void destroyGroup(ParticleGroup group) {
        groupDestroyQ.add(group);
    }

    public static Vec2 getGravity() {
        return gravity;
    }

    public static Vec2[] getParticles(ParticleGroup group){

        int length = group.getParticleCount();
        Vec2[] src = physicsWorld.getParticlePositionBuffer();
        int srcPos = group.getBufferIndex();
        Vec2[] dst = new Vec2[length];
        int dstPos = 0;

        System.arraycopy(src, srcPos, dst, dstPos, length);

        return dst;
    }

    public static Vec2[] getVelocities(ParticleGroup group){

        int length = group.getParticleCount();
        Vec2[] src = physicsWorld.getParticleVelocityBuffer();
        int srcPos = group.getBufferIndex();
        Vec2[] dst = new Vec2[length];
        int dstPos = 0;



        System.arraycopy(src, srcPos, dst, dstPos, length);

        return dst;
    }

    public static ParticleColor[] getColors(ParticleGroup group){

        int length = group.getParticleCount();
        ParticleColor[] src = physicsWorld.getParticleColorBuffer();
        int srcPos = group.getBufferIndex();
        ParticleColor[] dst = new ParticleColor[length];
        int dstPos = 0;



        System.arraycopy(src, srcPos, dst, dstPos, length);

        return dst;
    }

    // Thread definition, this is where the physics magic happens
    private static class PhysicsThread extends Thread {

        // Setting this to true exits the internal update loop, and ends the thread
        public boolean stop = false;

        // We need to know if the thread is still running or not, just in case we try to create it
        // after telling it to stop, but before it can finish.
        private boolean running = false;

        public boolean isRunning() {
            return running;
        }

        public Vec2 getGravity() {
            if (physicsWorld != null) {
                return physicsWorld.getGravity();
            } else {
                return null;
            }
        }

        @Override
        public void run() {

            // Create world with saved gravity
            physicsWorld = new World(new Vec2(0.0f, 0.0f)); //physicsWorld = new World(new Vec2(0, -10));
            physicsWorld.setAllowSleep(true);
            physicsWorld.setParticleGravityScale(1.0f);
            physicsWorld.setParticleDensity(1.7f);
            physicsWorld.setParticleDamping(1.5f);
            physicsWorld.setParticleRadius(Renderer.screenToWorld(PARTICLE_RADIUS));



            //physicsWorld.setParticleMaxCount(MAX_PARTICLES);

            running = true;

            // Step!
            while (!stop) {

                // Record the start time, so we know how long it took to sim everything
                long startTime = System.currentTimeMillis();

                if (bodyDestroyQ.size() > 0) {
                    synchronized (bodyDestroyQ) {

                        for (Body body : bodyDestroyQ) {
                            physicsWorld.destroyBody(body);
                            bodyCount--;
                        }

                        bodyDestroyQ.clear();
                    }
                }

                if (groupDestroyQ.size() > 0) {
                    synchronized (groupDestroyQ) {

                        for (ParticleGroup group : groupDestroyQ) {
                            physicsWorld.destroyParticlesInGroup(group, false);
                            groupCount--;
                        }

                        groupDestroyQ.clear();
                    }
                }

                if (bodyCreateQ.size() > 0) {
                    synchronized (bodyCreateQ) {

                        // Handle creations
                        for (BodyQueueDef bq : bodyCreateQ) {
                            Renderer.actors.get(bq.getActorID()).onBodyCreation(physicsWorld.createBody(bq.getBd()));
                        }
                        bodyCreateQ.clear();
                    }
                }

                if (groupCreateQ.size() > 0) {
                    synchronized (groupCreateQ) {

                        // Handle creations
                        for (GroupQueueDef gq : groupCreateQ) {
                            ParticleGroup grp = physicsWorld.createParticleGroup(gq.getGd());
                            grp.setUserData(gq.getGroupID());
                            Renderer.groupQ.add(grp);
                        }

                        groupCreateQ.clear();
                    }
                }

                TimeStep step = new TimeStep();
                step.dt = 0.016666666f;
                step.velocityIterations = velIterations;
                step.positionIterations = posIterations;

                // Perform step, calculate elapsed time and divide by 1000 to get it
                // in seconds
                physicsWorld.step(step.dt, step.velocityIterations, step.positionIterations);

                /*if (bodyCount == 0 && groupCount == 0) {
                    stop = true;
                }*/

                long simTime = System.currentTimeMillis() - startTime;

                if (simTime < 16) {
                    try {
                        Thread.sleep(16 - simTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            running = false;
        }
    }
}