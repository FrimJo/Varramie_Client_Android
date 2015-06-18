package com.spots.liquidfun;

import org.jbox2d.common.Vec2;

public class BoxObject extends BaseObject {

    private float width;
    private float height;

    public BoxObject(float _width, float _height) {
        super(); // Just assigns an ID

        // 4 points, 3 coords, 12 elements, 9000 problems
        vertices = new float[12];

        this.width = _width;
        this.height = _height;

        refreshVertices();
    }

    private void refreshVertices() {

        // Modify our own vertex array, and pass it to setVertices
        // We'll define our box centered around the origin
        // The z cord could potentially be used to specify a layer to render on. Food for thought.
        vertices[0] = -width;
        vertices[1] = -height;
        vertices[2] = 0;

        vertices[3] = -width;
        vertices[4] = height;
        vertices[5] = 0;

        vertices[6] = width;
        vertices[7] = -height;
        vertices[8] = 0;

        vertices[9] = width;
        vertices[10] = height;
        vertices[11] = 0;

        // Update!
        setVertices(vertices);
    }

    // Rebuild our vertices on modification
    public void setWidth(float _width) {
        this.width = _width;
        refreshVertices();
    }

    public void setHeight(float _height) {
        this.height = _height;
        refreshVertices();
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

}
