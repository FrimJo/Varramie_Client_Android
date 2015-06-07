package com.spots.varramie;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SpotSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
	private SurfaceHolder sh;
	private final Paint paintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
	private SpotThread thread;
	private Context ctx;
	
	
	public SpotSurfaceView(Context context) {
		super(context);
		sh = getHolder();
		sh.addCallback(this);
		paintCircle.setStyle(Style.STROKE);
		paintCircle.setStrokeWidth(2);
		paintText.setColor(Color.WHITE);
		paintText.setTextSize(24.0f);
		ctx = context;
		
	    setFocusable(true); // make sure we get key events
	}
	
	public SpotThread getThread() {
	    return thread;
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		thread = new SpotThread(new Handler());
	    thread.setRunning(true);
	    thread.start();
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		thread.setSurfaceSize(width, height);
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	    boolean retry = true;	    
	    this.thread.setRunning(false);
	    while (retry) {
	      try {
	        this.thread.join();
	        retry = false;
	      } catch (InterruptedException e) {
	    	  // Catch the exceptions but do nothing.
	      }
	    }
	    
	}
	
	@Override
	public boolean performClick(){
		return super.performClick();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		int action = event.getAction();
		
		int x = Math.round(event.getX());
		int y = Math.round(event.getY());
		
		if (action == MotionEvent.ACTION_UP)
			this.performClick();
			
		
		
		try{
			Client.INSTANCE.sendTouchAction(x, y, action);
		}catch(IOException e){
			Client.INSTANCE.println("IOException in SpotSurfaceView");
		}

		return true;
	}
	
	private class SpotThread extends Thread{
		  
		  private boolean run = false;
		    
		  public SpotThread(Handler handler) {
		  }
		  
		  public void doStart() {
		  }
		  
		  public void run() {
		    while (run) {
		      Canvas c = null;
		      try {
		        c = sh.lockCanvas(null);
		        synchronized (sh) {
		          doDraw(c);
		        }
		      }catch(NullPointerException e){
		    	  // Catch the exception and do nothing. Temporary solution.
		      } finally {
		        if (c != null) {
		          sh.unlockCanvasAndPost(c);
		        }
		      }
		    }
		  }
		    
		  public void setRunning(boolean b) { 
		    run = b;
		  }
		  
		  public void setSurfaceSize(int width, int height) {
		    synchronized (sh) {
		      doStart();
		    }
		  }
		  
		  private void doDraw(Canvas canvas) {
			canvas.save();
			canvas.drawColor(Color.WHITE);
			
			for(int i = 0; i < Spot.sizeOfSpotsList(); i++){
				Spot s = Spot.getSpotAt(i);
				if(s.isActive()){
					Point p = s.getPoint();
					for(int counter = 0; p != null; counter++){
						paintCircle.setColor(s.getDynamicColor(counter));
						canvas.drawCircle(p.x, p.y, 30, paintCircle);	
						p = s.getPoint();
					}
				}
			}
			if(Spot.isMySpotActive()){
				Point p = Spot.getMySpotPoint();
				for(int counter = 0; p != null; counter++){
					paintCircle.setColor(Spot.getMySpotDynamicColor(counter));
					canvas.drawCircle(p.x, p.y, 30, paintCircle);	
					p = Spot.getMySpotPoint();
				}
			}
			canvas.restore();
			
		  }

		}
	
}
