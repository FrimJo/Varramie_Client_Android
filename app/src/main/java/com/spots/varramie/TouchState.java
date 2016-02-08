package com.spots.varramie;

import android.graphics.Color;

import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;

import java.util.Random;

/**
 * Created by fredrikjohansson on 15-06-08.
 */
public class TouchState {
    private byte _state;
    private final Vec2 _position_screen;
    private float _pressure;
    private final Vec2 _velocity;
    private final String _id;
    private final Color3f _color;

    private static Random rand = new Random(System.currentTimeMillis());

    public TouchState(final byte startState, Vec2 position_screen, final float pressure, final Vec2 velocity, final String id){
        _state = startState;
        _position_screen = position_screen;
        _pressure = pressure;
        _velocity = velocity;
        _id = id;
        _color = calcColor();
    }

    public TouchState(TouchState touchState){
        _state = touchState._state;
        _position_screen = touchState._position_screen;
        _pressure = touchState._pressure;
        _velocity = touchState._velocity;
        _id = touchState._id;
        _color = calcColor();
    }

    public Color3f calcColor(){
        float[] hsv = new float[3];
        Color.RGBToHSV(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256), hsv);
        hsv[1] = 0.75f;
        hsv[2] = 1.0f;
        int argb = Color.HSVToColor(hsv);
        int red = Color.red(argb);
        int green = Color.green(argb);
        int blue = Color.blue(argb);
        return new Color3f(red/255.0f, green/255.0f, blue/255.0f);
    }

    public void setState(final byte state){
        _state = state;
    }
    public void setPositionScreen(final Vec2 position_screen){
        _position_screen.x = position_screen.x;
        _position_screen.y = position_screen.y;
    }

    public byte getState(){
        return _state;
    }
    public Vec2 getPositionScreen() { return _position_screen; }
    public float getPressure() { return _pressure; }
    public Vec2 getVelocity() { return _velocity; }
    //public String getId() { return _id; }
    public Color3f getColor() { return _color; }

    @Override
    public boolean equals(Object o){
        if(o == null)
            return false;

        if(o.getClass() != this.getClass())
            return false;

        TouchState ts = (TouchState) o;

        if(ts._position_screen.x != _position_screen.x || ts._position_screen.y != _position_screen.y || ts._state != _state || ts._velocity.x != ts._velocity.x || ts._velocity.y != ts._velocity.y || ts._state != _state)
            return false;

        return true;
    }
}