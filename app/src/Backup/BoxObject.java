package com.spots.liquidfun;

/**
 * Created by fredrikjohansson on 15-06-17.
 */
public class BoxObject extends BaseObject {

    private float _width;
    private float _height;

    public BoxObject(final float width, final float height){
        super(); //Just assigns an ID

        // 4 points, 3 coords, 12 elements, over 9000 problems
        _vertices = new float[12];

        _width = width;
        _height = height;

        refreshVertices();
    }

    private void refreshVertices(){

        // Modify our own vertex array, and pass it to setVertices
        // We'll define our box centered around the origin
        // The z cord could potentially be used to specify a layer to render on.
        // Food for thought.
        _vertices[0] = _width * -0.5f;
        _vertices[1] = _height * -0.5f;
        _vertices[2] = 1.0f;

        _vertices[3] = _width * -0.5f;
        _vertices[4] = _height * 0.5f;
        _vertices[5] = 1.0f;

        _vertices[6] = _width * 0.5f;
        _vertices[7] = _height * -0.5f;
        _vertices[8] = 1.0f;

        _vertices[9] = _width * 0.5f;
        _vertices[10] = _height * 0.5f;
        _vertices[11] = 1.0f;

        // Update!
        setVertices(_vertices);
    }

    public void setWidth(final float width){ _width = width; }
    public void setHeight(final float height){ _height = height; }

    public float getWidth() { return _width; }
    public float getHeight() { return _height; }

}
