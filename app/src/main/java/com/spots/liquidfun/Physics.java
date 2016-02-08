package com.spots.liquidfun;

import com.spots.varramie.Client;
import com.spots.varramie.CollisionPackage;
import com.spots.varramie.OpCodes;
import com.spots.varramie.TouchState;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.TimeStep;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleContact;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleGroupType;
import org.jbox2d.particle.ParticleType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Physics {

    // Defined public, should we need to modify them from elsewhere
    public static int velIterations = 8; // 6
    public static int posIterations = 3; // 6

    public static float GROUP_RADIUS;
    public static float PARTICLE_RADIUS;

    // Threads!
    private static PhysicsThread pThread = null;

    // The world itself
    public static World physicsWorld = null;


    public static void start(){
        if (pThread == null) {
            pThread = new PhysicsThread();
            pThread.start();
        }
    }

    public static void clearClients(){
        if(pThread == null)
            return;
        pThread.clearClients();
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
        private ConcurrentHashMap<String, Cluster> clustersM = new ConcurrentHashMap();

        private void clearClients(){
            clustersM.clear();
        }


        private Cluster createCluster(final TouchState ts, final String id){
            /* Variable declaration */
            ParticleGroup grp;
            ParticleGroupDef def;

            /* Creates the definition of the group */
            CircleShape partShape = new CircleShape();
            partShape.setRadius(Renderer.screenToWorld(Physics.GROUP_RADIUS));  // Sets the size of the cluster
            def = new ParticleGroupDef();
            def.color = new ParticleColor(new Color3f(0.0f, 0.0f, 1.0f));
            def.flags = ParticleType.b2_springParticle | ParticleType.b2_destructionListener;
            def.groupFlags = ParticleGroupType.b2_solidParticleGroup;
            def.position.set(ts.getPositionScreen()); // Converts the screen coordinates to world coordinates
            def.shape = partShape;
            def.strength = 1.0f;
            def.destroyAutomatically = false;

            /* Create the Particle Group out of the Group Definition. */
            grp = physicsWorld.createParticleGroup(def);
            grp.setUserData(id);

            /* Creates and adds the cluster to the Cluster HashMap */
            Cluster c = new Cluster(grp, id, ts.getColor());
            clustersM.put(id, c);

            return c;
        }


        @Override
        public void run() {

            // Create world with saved gravity
            physicsWorld = new World(new Vec2(0.0f, 0.0f)); //physicsWorld = new World(new Vec2(0, -10));
            physicsWorld.setAllowSleep(true);
            physicsWorld.setParticleDamping(0.15f); // 0.35
            physicsWorld.setParticleDensity(0.2f);
            physicsWorld.setParticleRadius(Renderer.screenToWorld(PARTICLE_RADIUS));

            // Step!
            while (!stop) {

                // Record the start time, so we know how long it took to sim everything
                long startTime = System.currentTimeMillis();

                /* Loops-through all the touch states in the Hash Map and
                 * checks whether the ID behind each touch state exists in the
                 * cluster Hash Map. If not add it, else set its new position,
                 * and then move the physic particles to the right place in the
                 * physics world. */

                for(Map.Entry<String, TouchState> entry : Client.INSTANCE.getTouchMapValues()){
                    TouchState ts = entry.getValue();
                    switch (ts.getState()) {
                        case OpCodes.ACTION_DOWN:
                        case OpCodes.ACTION_MOVE:
                            Cluster c;
                            if (clustersM.containsKey(entry.getKey()))
                                c = clustersM.get(entry.getKey());
                            else
                                c = createCluster(ts, entry.getKey());
                            c.move(ts);
                            break;
                        case OpCodes.ACTION_UP:
                            if (clustersM.containsKey(entry.getKey())){
                                physicsWorld.destroyParticlesInGroup(clustersM.remove(entry.getKey()).getGroup(), true);

                            }
                            break;
                        default:
                            break;
                    }
                }



                /* This part handles the collision detection */

                if(physicsWorld.getParticleContactCount() > 0){


                    ParticleContact[] particleContacts = physicsWorld.getParticleContacts();
                    ParticleGroup[] groups = physicsWorld.getParticleGroupList();

                    for(int i = 0; i < particleContacts.length; i++){
                        if(particleContacts[i].flags == 0 )
                            break;


                        int indexA = particleContacts[i].indexA;
                        int indexB = particleContacts[i].indexB;

                        if(indexA == -1 || indexB == -1 )
                            break;

                        String idA = ( (String) groups[indexA].getUserData() );
                        String idB = ( (String) groups[indexB].getUserData() );

                        if(!idA.equals(idB)){
                            Vec2 position = physicsWorld.getParticlePositionBuffer()[particleContacts[i].indexA];
                            try{
                                Client.INSTANCE.addPackage(new CollisionPackage(position, idA, idB));
                            }catch(InterruptedException e){
                                Client.INSTANCE.println("InterruptedException in collitionhandling: ");
                                e.printStackTrace();
                            }
                            Client.INSTANCE.onColide();
                            break;
                        }

                        /*
                         * Resets the flag to hinder old collision to be re detected
                         */
                        particleContacts[i].flags = 0;

                    }
                }

                /* Adds some friction to the clusters so they don't spin for ever. */
                Vec2[] velocities = physicsWorld.getParticleVelocityBuffer();
                for(int i = 0; i < physicsWorld.getParticleCount(); i++) {
                    velocities[i].x += (velocities[i].x < 0.0f) ? 0.01f : - 0.01f;
                    velocities[i].y += (velocities[i].y < 0.0f) ? 0.01f : - 0.01f;
                }

                TimeStep step = new TimeStep();
                step.dt = 0.016666666f; // add a zero
                step.velocityIterations = velIterations;
                step.positionIterations = posIterations;


                // Perform step, calculate elapsed time and divide by 1000 to get it
                // in seconds
                try{
                    //mParticleSystem.solve(step);
                    //mParticleSystem.updateContacts(false);
                    physicsWorld.step(step.dt, step.velocityIterations, step.positionIterations);


                    //physicsWorld.step(step.dt, step.velocityIterations, step.positionIterations);

                }catch(NullPointerException e){

                }

                // Wake up Renderer
                synchronized (Renderer.lock){
                    Renderer.lock.notify();
                }

                long simTime = System.currentTimeMillis() - startTime;

                if (simTime < 16) {
                    try {
                        Thread.sleep(16 - simTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}