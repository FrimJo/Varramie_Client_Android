package com.spots.varramie;

import org.jbox2d.common.Vec2;

/**
 * Created by fredrikjohansson on 15-08-05.
 */
public class ThreadContainer{

    public Thread thread;
    public String id;
    public Vec2 postistion_screen;

    public ThreadContainer(Thread _thread, String _id, Vec2 _position_screen){
        thread = _thread;
        id = _id;
        postistion_screen = _position_screen;
    }
}
