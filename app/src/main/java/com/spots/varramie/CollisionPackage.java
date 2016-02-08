package com.spots.varramie;

import org.jbox2d.common.Vec2;

/**
 * Created by fredrikjohansson on 15-07-02.
 */
public class CollisionPackage {

    public final String idA;
    public final String idB;
    public final Vec2 position;

    public CollisionPackage(final Vec2 _position, final String _idA, final String _idB){
        position = _position;
        idA = _idA;
        idB = _idB;
    }
}
