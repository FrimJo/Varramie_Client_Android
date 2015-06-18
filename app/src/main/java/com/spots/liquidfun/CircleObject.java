package com.spots.liquidfun;

import org.jbox2d.common.Vec2;

/**
 * Created by fredrikjohansson on 15-06-17.
 */
public class CircleObject extends BaseObject{

    private float _r;
    private int _num_segments;

    public CircleObject(final float r, final int num_segments){
        _r = r;
        _num_segments = num_segments;
        vertices = new float[num_segments * 3];

        refreshVertices();
    }

    private void refreshVertices() {

        // Modify our own vertex array, and pass it to setVertices
        // We'll define our box centered around the origin
        // The z cord could potentially be used to specify a layer to render on. Food for thought.

        double theta = 2.0f * Math.PI / (double) _num_segments;
        double c = Math.cos(theta);//precalculate the sine and cosine
        double s = Math.sin(theta);
        double t;

        double x = _r;//we start at angle = 0
        double y = 0;


        for(int ii = 0; ii < _num_segments; ii++)
        {
            int index = ii*3;
            vertices[index] = (float) x;
            vertices[index + 1] = (float) y;
            vertices[index + 2] = 0.0f;

            //apply the rotation matrix
            t = x;
            x = c * x - s * y;
            y = s * t + c * y;
        }

        // Update!
        setVertices(vertices);
    }

}

