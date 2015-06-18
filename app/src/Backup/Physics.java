package com.spots.liquidfun;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

import java.util.Vector;

/**
 * Created by fredrikjohansson on 15-06-17.
 */
public class Physics {

    // Defined public, should we need to modify them from elsewhere
    public static int _velIterations = 6;
    public static int _posIterations = 2; // 6

    // We need to keep track of how many bodies exist, so we can stop the threa
    // when none are present, and start it up again when necessary
    private static int _bodyCount = 0;

    // This is private, since we need to set it in the physics world, so directly
    // modifying it from outside the calss would bypass that. Why not set it
    // in the world directly? The world is in another thread :) It might also
    // stop and start back up again, so we need to have it saved.
    private static  Vec2 _gravity = new Vec2(0, 0);

    // Our queues. Wonderful? I concur.
    private static final Vector<Body> _bodyDestroyQ = new Vector<>();
    private static final Vector<BodyQueueDef> _bodyCreateQ = new Vector<>();

    // Threads!
    private static PhysicsThread _pThread = null;

    public static void requestBodyCreation(final BodyQueueDef bq){

        // Ship it to our queue
        _bodyCreateQ.add(bq);

        if(_bodyCount == 0){

            // If the thread already exists, then wait for it to finish running before re-casting
            // Technically one could just restart the thread, but meh
            if(_pThread != null)
                while (_pThread.isRunning()) { }

            _pThread = new PhysicsThread();
            _pThread.start();
        }

        // Take not of the new body
        _bodyCount++;
    }

    public static void destroyBody(Body body){
        _bodyDestroyQ.add(body);
    }

    public static void setGravity(Vec2 gravity){
        _gravity = gravity;
        if(_pThread != null)
            _pThread.setGravity(gravity);
    }

    public static Vec2 getGravity(){
        return _gravity;
    }

    // Thread definition, this is where the physics magic happens
    private static class PhysicsThread extends Thread {

        // Setting this to true exits the internal update loop, and ends the thread.
        public boolean _stop = false;

        // We need to know of the thread is still running or not, just on vase we try to create it
        // after telling it to stop, but before it can finish.
        private boolean _running = false;

        // The world itself
        private World _physicsWorld = null;

        public boolean isRunning(){ return isRunning(); }

        public void setGravity(Vec2 grav){
            if(_physicsWorld != null)
                _physicsWorld.setGravity(grav);
        }

        public Vec2 getGravity(){
            if(_physicsWorld != null)
                return _physicsWorld.getGravity();
            else
                return null;
        }

        @Override
        public void run(){

            _running = true;

            // Create world with saved gravity
            _physicsWorld = new World(_gravity);
            _physicsWorld.setAllowSleep(true);

            // Step!
            while(!_stop){

                // Record the start time, so we know how long it took to sim everything
                long startTime = System.currentTimeMillis();

                if(_bodyDestroyQ.size() > 0)
                    synchronized (_bodyDestroyQ){
                        for(Body body : _bodyDestroyQ){
                            _physicsWorld.destroyBody(body);
                            _bodyCount--;
                        }
                        _bodyDestroyQ.clear();
                    }

                if(_bodyCreateQ.size() > 0)
                    synchronized (_bodyCreateQ){

                        // Handle creations
                        for(BodyQueueDef bq : _bodyCreateQ)
                            Renderer._actors.get(bq.getActorID()).onBodyCreation(_physicsWorld.createBody(bq.getBd()));
                    }

                // Step for 1/60th of a secound
                _physicsWorld.step(0.016666666f, _velIterations, _posIterations);

                if(_bodyCount == 0)
                    _stop = true;

                // Figure out how long it took
                long simTime = System.currentTimeMillis() - startTime;

                //Sleep for the excess, 16 for 60fps
                if(simTime < 16)
                    try{
                        Thread.sleep(16 - simTime);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
            }

            _running = false;
        }
    }

}
