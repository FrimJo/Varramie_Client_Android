package com.spots.varramie;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
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


    public OpenGLSurfaceView(Context context) {
        super(context);



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



        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);


        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // make sure we get key events
        setFocusable(true);
    }

    @Override
    public boolean performClick(){
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float height = (float) getHeight();
        final float x = event.getX()/height;
        final float y = event.getY()/height;
        final float size = event.getSize();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                queueEvent(new Runnable() {
                    @Override public void run() {
                        Client.INSTANCE.sendTouchAction(x, y, OpCodes.ACTION_DOWN, size);
                    }});
                return true;
            case MotionEvent.ACTION_UP:
                this.performClick();
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        Client.INSTANCE.sendTouchAction(x, y, OpCodes.ACTION_UP, size);
                    }
                });
                return true;
            case MotionEvent.ACTION_MOVE:
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        Client.INSTANCE.sendTouchAction(x, y, OpCodes.ACTION_MOVE, size);
                    }
                });
                return true;
            case MotionEvent.ACTION_OUTSIDE:
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        Client.INSTANCE.sendTouchAction(x, y, OpCodes.ACTION_UP, size);
                    }
                });
                return true;
            case MotionEvent.ACTION_CANCEL:
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        Client.INSTANCE.sendTouchAction(x, y, OpCodes.ACTION_UP, size);
                    }
                });
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }
}
