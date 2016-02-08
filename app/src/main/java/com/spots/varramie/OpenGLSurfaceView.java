package com.spots.varramie;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.facebook.appevents.AppEventsLogger;
import com.spots.liquidfun.ClusterManager;
import com.spots.liquidfun.Physics;

import org.jbox2d.common.Vec2;

/**
 * Created by fredrikjohansson on 15-06-11.
 */
public class OpenGLSurfaceView extends GLSurfaceView {

    private final com.spots.liquidfun.Renderer mRenderer;


    public OpenGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new com.spots.liquidfun.Renderer(context);
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
        mRenderer = new com.spots.liquidfun.Renderer(context);
        setRenderer(mRenderer);


        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // make sure we get key events
        setFocusable(true);
    }

    @Override
    public void onResume(){
        super.onResume();
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this.getContext());
    }

    @Override
    public void onPause(){
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this.getContext());
    }

    @Override
    public boolean performClick(){
        return super.performClick();
    }

    private Vec2 touch =  new Vec2(0.0f,0.0f);
    private Vec2 event_touch = new Vec2(0.0f,0.0f);
    private Vec2 velocity = new Vec2(0.0f, 0.0f);
    private long startTime = System.currentTimeMillis();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        long deltaTime = System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();
        event_touch.set(event.getX(), event.getY());

        if(touch.x != event_touch.x || touch.y != event_touch.y){
            Vec2 deltaPosition = event_touch.sub(touch);
            velocity.set(deltaPosition.mulLocal(1000.0f / deltaTime));
            touch.set(event_touch);
        }
        final float size = event.getSize();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Client.INSTANCE.sendTouchAction(event_touch, OpCodes.ACTION_DOWN, size, velocity);
                return true;
            case MotionEvent.ACTION_UP:
                this.performClick();
                Client.INSTANCE.sendTouchAction(event_touch, OpCodes.ACTION_UP, size, velocity);
                return true;
            case MotionEvent.ACTION_MOVE:
                Client.INSTANCE.sendTouchAction(event_touch, OpCodes.ACTION_MOVE, size, velocity);
                return true;
            case MotionEvent.ACTION_OUTSIDE:
                Client.INSTANCE.sendTouchAction(event_touch, OpCodes.ACTION_UP, size, velocity);
                return true;
            case MotionEvent.ACTION_CANCEL:
                Client.INSTANCE.sendTouchAction(event_touch, OpCodes.ACTION_UP, size, velocity);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }
}