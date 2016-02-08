package com.spots.varramie;

import org.jbox2d.common.Vec2;

/**
 * Created by fredrikjohansson on 15-08-03.
 */
public class ParticleMovementPackage {

    public int index;
    public Vec2 position;
    public Vec2 center;

    public ParticleMovementPackage(int _index, Vec2 _position, Vec2 _center){
        index = _index;
        position = _position;
        center = _center;
    }
}
