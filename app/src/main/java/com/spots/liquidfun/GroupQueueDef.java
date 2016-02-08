package com.spots.liquidfun;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleGroupType;
import org.jbox2d.particle.ParticleType;

import java.security.acl.Group;

/**
 * Created by fredrikjohansson on 15-06-18.
 */
public class GroupQueueDef {

    private String groupID;
    private ParticleGroupDef gd;
    private ParticleGroup grp = null;

    public GroupQueueDef(String _groupID, ParticleGroupDef _gd) {
        groupID = _groupID;
        gd = _gd;
    }

    public String getGroupID() { return groupID; }
    public ParticleGroupDef getGd() { return gd; }

}
