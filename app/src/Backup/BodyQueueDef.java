package com.spots.liquidfun;

import org.jbox2d.dynamics.BodyDef;

/**
 * Created by fredrikjohansson on 15-06-17.
 */
public class BodyQueueDef {
    private int _actorID;
    private BodyDef _bd;

    public BodyQueueDef(final int actorID, final BodyDef bd) {
        _bd = bd;
        _actorID = actorID;
    }

    public int getActorID() { return _actorID; }
    public BodyDef getBd() { return _bd; }
}
