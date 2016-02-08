package com.spots.liquidfun;

import com.spots.varramie.ThreadContainer;

import org.jbox2d.particle.ParticleGroupDef;

/**
 * Created by fredrikjohansson on 15-08-05.
 */
public class ComparableParticleGroupDef extends ParticleGroupDef {
    public ComparableParticleGroupDef(){
        super();
    }

    @Override
    public boolean equals(Object o) {
        if(o.getClass() != ComparableParticleGroupDef.class)
            return false;

        ComparableParticleGroupDef def = (ComparableParticleGroupDef) o;

        ThreadContainer local_tc = (ThreadContainer) super.userData;
        ThreadContainer tc = (ThreadContainer) def.userData;

        if(local_tc == null || tc == null)
            return false;

        if(!local_tc.id.equals(tc.id))
            return false;

        return true;
    }
}
