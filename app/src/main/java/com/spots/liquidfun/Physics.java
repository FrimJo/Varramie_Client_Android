package com.spots.liquidfun;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

import java.util.Vector;

public class Physics {

  // Defined public, should we need to modify them from elsewhere
  public static int velIterations = 6;
  public static int posIterations = 6;

  // This is private, since we need to set it in the physics world, so directly
  // modifying it from outside the class would bypass that. Why not set it
  // in the world directly? The world is in another thread :) It might also
  // stop and start back up again, so we need to have it saved.
  private static Vec2 gravity = new Vec2(0, 0);

  // Threads!
  private static PhysicsThread pThread = null;

  // Our queues. Wonderful? I concur.
  private static final Vector<Body> bodyDestroyQ = new Vector<Body>();
  private static final Vector<BodyQueueDef> bodyCreateQ = new Vector<BodyQueueDef>();

  // We need to keep track of how many bodies exist, so we can stop the thread
  // when none are present, and start it up again when necessary
  private static int bodyCount = 0;

  public static void requestBodyCreation(BodyQueueDef bq) {

    // Ship it to our queue
    bodyCreateQ.add(bq);

    if(bodyCount == 0) {

      // If the thread already exists, then wait for it to finish running before re-creating
      // Technically one could just restart the thread, but recreating is simpler
      if(pThread != null) {
        while(pThread.isRunning()) { }
      }

      pThread = new PhysicsThread();
      pThread.start();
    }

    // Take note of the new body
    bodyCount++;
  }

  public static void destroyBody(Body body) {
    bodyDestroyQ.add(body);
  }

  public static void setGravity(Vec2 grav) {
    if(pThread != null) {
      pThread.setGravity(grav);
    }
    gravity = grav;
  }

  public static Vec2 getGravity() {
    return gravity;
  }

  // Thread definition, this is where the physics magic happens
  private static class PhysicsThread extends Thread {

    // Setting this to true exits the internal update loop, and ends the thread
    public boolean stop = false;

    // We need to know if the thread is still running or not, just in case we try to create it
    // after telling it to stop, but before it can finish.
    private boolean running = false;

    // The world itself
    private World physicsWorld = null;

    public boolean isRunning() { return running; }

    public void setGravity(Vec2 grav) {
      if(physicsWorld != null) {
        physicsWorld.setGravity(grav);
      }
    }

    public Vec2 getGravity() {
      if(physicsWorld != null) {
        return physicsWorld.getGravity();
      } else {
        return null;
      }
    }

    @Override
    public void run() {

      running = true;

      // Create world with saved gravity
      physicsWorld = new World(new Vec2(0, -0)); //physicsWorld = new World(new Vec2(0, -10));
      physicsWorld.setAllowSleep(true);

      // Step!
      while(!stop) {

        // Record the start time, so we know how long it took to sim everything
        long startTime = System.currentTimeMillis();

        if(bodyDestroyQ.size() > 0) {
          synchronized (bodyDestroyQ) {

            for(Body body : bodyDestroyQ) {
              physicsWorld.destroyBody(body);
              bodyCount--;
            }

            bodyDestroyQ.clear();
          }
        }

        if(bodyCreateQ.size() > 0) {
          synchronized (bodyCreateQ) {

            // Handle creations
            for (BodyQueueDef bq : bodyCreateQ) {
              Renderer.actors.get(bq.getActorID()).onBodyCreation(physicsWorld.createBody(bq.getBd()));
            }

            bodyCreateQ.clear();
          }
        }

        // Perform step, calculate elapsed time and divide by 1000 to get it
        // in seconds
        physicsWorld.step(0.016666666f, velIterations, posIterations);

        if(bodyCount == 0) { stop = true; }

        long simTime = System.currentTimeMillis() - startTime;

        if(simTime < 16) {
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