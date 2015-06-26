package com.spots.varramie;

import org.jbox2d.common.Vec2;

/**
 * Created by fredrikjohansson on 15-06-08.
 */
public class TouchState {
    private byte _state;
    private final Vec2 _position_norm; // Normalized position x and y divided by the hight of the screen
    private float _pressure;
    private final Vec2 _velocity;

    public TouchState(final byte startState, Vec2 position_norm, final float pressure, final Vec2 velocity){
        _state = startState;
        _position_norm = position_norm;
        _pressure = pressure;
        _velocity = velocity;
    }

    public TouchState(TouchState touchState){
        _state = touchState._state;
        _position_norm = touchState._position_norm;
        _pressure = touchState._pressure;
        _velocity = touchState._velocity;
    }

    public byte getState(){
        return _state;
    }

    public void setState(final byte state){
        _state = state;
    }

    public Vec2 getPositionNorm() { return _position_norm; }
    public float getPressure() { return _pressure; }
    public Vec2 getVelocity() { return _velocity; }

    @Override
    public boolean equals(Object o){
        if(o == null)
            return false;

        if(o.getClass() != this.getClass())
            return false;

        TouchState ts = (TouchState) o;

        if(ts._position_norm.x != _position_norm.x || ts._position_norm.y != _position_norm.y || ts._state != _state || ts._velocity.x != ts._velocity.x || ts._velocity.y != ts._velocity.y || ts._state != _state)
            return false;

        return true;
    }
}
