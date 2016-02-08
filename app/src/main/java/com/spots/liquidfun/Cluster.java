package com.spots.liquidfun;

import com.spots.varramie.TouchState;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;

/**
 * Created by fredrikjohansson on 15-06-18.
 */
public class Cluster {

    private final String id;
    private final ParticleGroup _group;

    private Vec2[] cords;
    private ParticleColor[] colors;
    private Vec2[] velocity;

    public Cluster(ParticleGroup grp, String _id, Color3f color){
        id = _id;
        _group = grp;

        updateParticles();

        for(int i = 0; i < cords.length; i++)
            colors[i].set(color);
    }


    /*
    This method is caled while the user is moving on the screen
    it uses the touch position and the size of the touch
    to calculate which particles to change velocity on. The velocity changed
    is calculated base on the distance between the touch position and each
    particle in the touch radius position.
     */
    private float min = 1.0f;

//    public void push2(Vec2 velocity_touch, Vec2 position_screen, float touch_size){
//        touch_size *= 2.0f;
//        //touch_size *= Math.sqrt(Math.pow(velocity_touch.x, 2.0) + Math.pow(velocity_touch.y, 2.0))+1;
//        touch_size *= 2.0f;
//        if(Physics.physicsWorld.isLocked())
//            return;
//        if(touch_size < min && min != 0)
//            min = touch_size;
//
//        // Some variable declarations where we convert
//        // the touch position in to the physic worlds coordinates.
//        float   group_radius_world = Renderer.screenToWorld(Physics.GROUP_RADIUS),
//                touch_radius_world = (touch_size - min + group_radius_world / 40.0f) / min * group_radius_world * 3.0f;
//
//        double  hyp_sq,
//                radius_sq = Math.pow( (double) touch_radius_world , 2.0);
//
//        Vec2    dif_rad,
//                center = _group.getCenter(),
//                dist = Renderer.screenToWorld(position_screen);
//
//        boolean move = false;
//
//
//        Vec2 d = Renderer.screenToWorld(position_screen).sub(center);
//        double D = Math.sqrt(Math.pow(d.x, 2.0) + Math.pow(d.y, 2.0));
//        double B = group_radius_world;// - touch_radius_world;
//
//        Vec2 selection_center;
//        if(D > B) {
//            move = true;
//            selection_center = d.mul( (float) (B/D) ).add( center ); // .mul( (float) (B/D) )
//        }else
//            selection_center = d.add( center );
//
//
//        // Move the touching particles if they are near the edge of the cluster
///*        Vec2 d = dist.sub(center);
//        double grp_touch_sq = Math.pow(group_radius_world - touch_radius_world,2.0);
//        double dx_sq = Math.pow(d.x,2.0);
//        double dy_sq = Math.pow(d.y,2.0);
//
//
//        if(grp_touch_sq <= dx_sq - dy_sq){
//            move = true;
//
//        }*/
//
//        // For each particle, calculates the distance between the
//        // touch position and the particle position.
//        for(int i = 0; i < cords.length; i++){
//            //dif_rad = center.sub(cords[i]);
//            dif_rad = selection_center.sub(cords[i]);
//            hyp_sq = Math.pow(dif_rad.x,2.0) + Math.pow(dif_rad.y,2.0);
//
//            // Uses above calculated difference to set the velocity of the
//            // particles within the radius of the touch size.
//            //if(hyp_sq < radius_sq) {
//                if(move){
//                    colors[i].set(Color3f.GREEN);
//
//                    /*try{
//                        Physics.moveGroupQ.put(new TouchPackage( cords[i],   ));
//                    }catch(InterruptedException e){
//
//                    }*/
//                    cords[i].set(cords[i].add(dist.sub(selection_center)));
//
//                }else{
//                    //colors[i].set(Color3f.RED);
//                    colors[i].set(Color3f.BLUE);
//                }
//            //}else{
//                colors[i].set(Color3f.BLUE);
//                //velocity[i].add(Renderer.screenToWorld(velocity_touch));
//            //}
//        }
//    }

    public void updateParticles(){
        cords = Physics.getParticles(_group);
        colors = Physics.getColors(_group);
        velocity = Physics.getVelocities(_group);
    }

    /*
     * This method is run from the Physics.class main thread.
     */
    public synchronized void move(final TouchState ts){
        updateParticles();
        Vec2 position_world = Renderer.screenToWorld(ts.getPositionScreen());
        Vec2 diff, center = _group.getCenter();

        for(int i = 0; i < cords.length; i++){
            diff = position_world.sub(center);
            cords[i].addLocal(diff);
        }

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public String getId(){
        return id;
    }
    public ParticleGroup getGroup(){
        return _group;
    }

}