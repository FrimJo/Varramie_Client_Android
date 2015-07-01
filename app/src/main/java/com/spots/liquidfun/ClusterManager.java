package com.spots.liquidfun;

import android.util.SparseArray;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleGroupType;
import org.jbox2d.particle.ParticleType;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fredrikjohansson on 15-06-23.
 */
public class ClusterManager {

    public static Cluster myCluster;
    public static String myClusterId = "NOTHING";
    public static boolean myClusterIsCreating = false;
    public static final ConcurrentHashMap<String, Cluster> allClusters = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Boolean> allClustersIsCreating = new ConcurrentHashMap<>();

    public static void createNewCluster(final String id){

        // Create the body
        CircleShape partShape = new CircleShape();
        partShape.setRadius(Renderer.screenToWorld(Physics.GROUP_RADIUS));  // Sets the size of the cluster

        ParticleGroupDef gd = new ParticleGroupDef();
        gd.color = new ParticleColor(new Color3f(0.0f, 0.0f, 1.0f));
        gd.flags = ParticleType.b2_elasticParticle;
        gd.groupFlags = ParticleGroupType.b2_solidParticleGroup;
        Vec2 position_screen = new Vec2(150.0f, 150.0f);
        gd.position.set(Renderer.screenToWorld(position_screen)); // Converts the screen coordinates to world coordinates
        gd.shape = partShape;
        Physics.requestGroupCreation(new GroupQueueDef(id, gd));
    }

    public static void createNewCluster(final String id, Vec2 position_screen){
        // Create the body
        CircleShape partShape = new CircleShape();
        partShape.setRadius(Renderer.screenToWorld(Physics.GROUP_RADIUS));  // Sets the size of the cluster
        //Vec2 screen_position = Renderer.worldToScreen(position);

        ParticleGroupDef gd = new ParticleGroupDef();
        gd.color = new ParticleColor(new Color3f(0.0f, 0.0f, 1.0f));
        gd.flags = ParticleType.b2_elasticParticle;
        gd.groupFlags = ParticleGroupType.b2_solidParticleGroup;
        gd.position.set(Renderer.screenToWorld(position_screen)); // Converts the screen coordinates to world coordinates
        gd.shape = partShape;
        Physics.requestGroupCreation(new GroupQueueDef(id, gd));
    }

}
