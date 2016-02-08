package com.spots.liquidfun;

import android.util.SparseArray;

import com.spots.varramie.OpCodes;

import org.jbox2d.callbacks.ParticleQueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleGroupType;
import org.jbox2d.particle.ParticleSystem;
import org.jbox2d.particle.ParticleType;

import java.util.Collection;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fredrikjohansson on 15-06-23.
 */
public class ClusterManager {

    //public Cluster myCluster;
    public static String myClusterId;
    //public volatile static ConcurrentHashMap<String, Cluster> allClusters = new ConcurrentHashMap();



    /*private int getGroupFromParticle(final int i){
        ParticleGroup[] groups = Physics.physicsWorld.getParticleGroupBuffer();
        int preIndex = 0;
        int grpIndex = groups[0].getBufferIndex();
        for(int index = 1; grpIndex <= i; index++){
            preIndex = grpIndex;
            grpIndex = groups[index].getBufferIndex();
        }
        return preIndex;
    }*/

    /*public static void createNewCluster(final String id, Vec2 position_screen){
        // Create the body
        CircleShape partShape = new CircleShape();
        partShape.setRadius(Renderer.screenToWorld(Physics.GROUP_RADIUS));  // Sets the size of the cluster
        //Vec2 screen_position = Renderer.worldToScreen(position);

        ComparableParticleGroupDef gd = new ComparableParticleGroupDef();
        gd.color = new ParticleColor(new Color3f(0.0f, 0.0f, 1.0f));
        gd.flags = ParticleType.b2_springParticle;
        gd.groupFlags = ParticleGroupType.b2_solidParticleGroup;
        gd.position.set(position_screen); // Converts the screen coordinates to world coordinates
        gd.shape = partShape;
        gd.strength = 1.0f;

        Physics.createCluster(gd, id, position_screen);
    }*/

}
