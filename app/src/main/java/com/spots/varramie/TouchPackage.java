package com.spots.varramie;

import org.jbox2d.common.Vec2;
import org.jbox2d.particle.ParticleGroup;

/**
 * Created by fredrikjohansson on 15-07-09.
 *
 * This is a small class representing a
 * package that contains touch information
 * which is beeing sent from the Client Class
 * to the Physics Class and stored in a linked
 * blocking queue for the physics engine to
 * pick up and apply to the physics world.
 */

public class TouchPackage {


    public final Vec2 cord;
    public final Vec2 position;

    /**
     * The constructor for this small package.
     */
    public TouchPackage(final Vec2 _cord, final Vec2 _position){
        cord = _cord;
        position = _position;
    }
}
