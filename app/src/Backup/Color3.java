package com.spots.liquidfun;

/**
 * Created by fredrikjohansson on 15-06-17.
 */

import org.jbox2d.common.Vec3;

public class Color3 {
    public int _r;
    public int _g;
    public int _b;

    public Color3() {
        _r = _g = _b = 0;
    }
    public Color3(int r, int g, int b) {
    _r = r;
    _g = g;
    _b = b;
    }
    public Color3(float r, float g, float b) {
    _r = (int)(r * 255.0f);
    _g = (int)(g * 255.0f);
    _b = (int)(b * 255.0f);
    }

    // Makes components GL-compatible, returns them in a vector
    public Vec3 toFloat() {
        return new Vec3((float)_r / 255.0f, (float)_g / 255.0f, (float)_b / 255.0f);
    }

    public float[] toFloatArray() {
        return new float[]{ (float)_r / 255.0f, (float)_g / 255.0f, (float)_b / 255.0f, 1.0f };
    }
}