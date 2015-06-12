package com.spots.varramie;

/**
 * Created by fredrikjohansson on 15-06-11.
 */
public class PointFloat {

    private float _x, _y;

    public PointFloat(final float x, final float y){
        _x = x;
        _y = y;
    }

    public float getX(){
        return _x;
    }

    public void setX(final float x){
        _x = x;
    }

    public float getY(){
        return _y;
    }

    public void setY(final float y){
        _y = y;
    }

    public void setXY(final float x, final float y){
        _x = x;
        _y = y;
    }

    public void setXYfromPoint(final PointFloat p){
        _x = p._x;
        _y = p._y;
    }
}
