package com.spots.varramie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

/**
 * Created by fredrikjohansson on 15-06-11.
 */
public class OpenGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;
    private final Context _context;
    private Spot _mySpot;


    public OpenGLSurfaceView(Context context) {
        super(context);
        _context = context;



        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        // make sure we get key events
        setFocusable(true);
    }

    public OpenGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        _context = context;



        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        // make sure we get key events
        setFocusable(true);
    }

    @Override
    public boolean performClick(){
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        byte action;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                action = OpCodes.ACTION_DOWN;
                Client.INSTANCE.sendTouchAction(x, y, action);
                break;
            case MotionEvent.ACTION_UP:
                action = OpCodes.ACTION_UP;
                this.performClick();
                Client.INSTANCE.sendTouchAction(x, y, action);
                break;
            case MotionEvent.ACTION_MOVE:
                action = OpCodes.ACTION_MOVE;
                Client.INSTANCE.sendTouchAction(x, y, action);
                break;
            default:
                break;
        }
        //invalidate();

        return true;
    }
}
