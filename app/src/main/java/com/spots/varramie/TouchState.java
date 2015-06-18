package com.spots.varramie;

/**
 * Created by fredrikjohansson on 15-06-08.
 */
public class TouchState {
    private byte _state;
    private float _x;
    private float _y;
    private float _pressure;

    public TouchState(final byte startState, final float x, final float y, final float pressure){
        _state = startState;
        _x = x;
        _y = y;
        _pressure = pressure;
    }

    public TouchState(TouchState touchState){
        _state = touchState.getState();
        _x = touchState.getX();
        _y = touchState.getY();
        _pressure = touchState.getPressure();
    }

    public byte getState(){
        return _state;
    }

    public void setState(final float x, final float y, final byte state, final float pressure){
        _state = state;
        _x = x;
        _y = y;
        _pressure = pressure;
    }

    public float getX(){
        return _x;
    }

    public float getY(){
        return _y;
    }

    public float getPressure() { return _pressure; }

    @Override
    public boolean equals(Object o){
        if(o == null)
            return false;

        if(o.getClass() != this.getClass())
            return false;

        TouchState ts = (TouchState) o;

        if(ts._x != _x || ts._y != _y || ts._state != _state)
            return false;

        return true;
    }

    public static TouchState cloneState(TouchState touchState){
        return new TouchState(touchState);
    }
}
