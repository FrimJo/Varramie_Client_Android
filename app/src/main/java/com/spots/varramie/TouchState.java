package com.spots.varramie;

import android.view.MotionEvent;

/**
 * Created by fredrikjohansson on 15-06-08.
 */
public class TouchState {
    private int _state;
    private int _x;
    private int _y;

    public TouchState(int startState, int x, int y){
        _state = startState;
        _x = x;
        _y = y;
    }

    public TouchState(TouchState touchState){
        _state = touchState.getState();
        _x = touchState.getX();
        _y = touchState.getY();
    }

    public int getState(){
        return _state;
    }
    public void setState(int state){
        _state = state;
    }
    public void setState(int x, int y, int state){
        _state = state;
        _x = x;
        _y = y;
    }

    public int getX(){
        return _x;
    }

    public int getY(){
        return _y;
    }

    public static TouchState cloneState(TouchState touchState){
        return new TouchState(touchState);
    }
}
