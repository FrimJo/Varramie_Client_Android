package com.spots.varramie;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.spots.depricated.Spot;

public class SpotSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
	private SurfaceHolder sh;
	private final Paint paintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
	private SpotThread thread;
	
	
	public SpotSurfaceView(Context context) {
		super(context);
		sh = getHolder();
		sh.addCallback(this);
		paintCircle.setStyle(Style.STROKE);
		paintCircle.setStrokeWidth(2);
		paintText.setColor(Color.WHITE);
		paintText.setTextSize(24.0f);
		
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
		
		byte action;
		switch (event.getAction()){
			case MotionEvent.ACTION_DOWN:
				action = OpCodes.ACTION_DOWN;
				break;
			case MotionEvent.ACTION_UP:
				action = OpCodes.ACTION_UP;
				this.performClick();
				break;
			case MotionEvent.ACTION_MOVE:
				action = OpCodes.ACTION_MOVE;
				break;
			default:
				action = OpCodes.ACTION_UP;
				break;
		}
		
		short x = (short) event.getX();
		short y = (short) event.getY();

		Client.INSTANCE.sendTouchAction(x, y, action);


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
		      Canvas c = sh.lockCanvas(null);
				synchronized (sh) {
				  doDraw(c);
				}

				if (c != null) {
				  sh.unlockCanvasAndPost(c);
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
			
			/*for(int i = 0; i < Spot.sizeOfSpotsList(); i++){
				Spot s = Spot.getSpotAt(i);
				if(s.isActive()){
					int counter = 0;
					for(Point p : s){
						float d = (counter/9.0f);
						paintCircle.setColor(s.getDynamicColor( (int) (255/(1.0f+d*2)) ));
						canvas.drawCircle(p.x, p.y, 50.0f/(1.0f+d), paintCircle);
						counter++;
					}

				}
			}*/

			Spot mySpot = Spot.getMyspot();
			if(mySpot.isActive()){
				int counter = 0;
				for(Point p : mySpot){
					float d = (counter/9.0f);
					paintCircle.setColor(mySpot.getDynamicColor( (int) (255/(1.0f+d*2)) ));
					canvas.drawCircle(p.x, p.y, 50.0f/(1.0f+d), paintCircle);
					counter++;
				}
				//Point p = Spot.getMySpotPoint();
				//for(int counter = 0; p != null; counter++){
					//paintCircle.setColor(Spot.getMySpotDynamicColor(0/*counter*/));
					//canvas.drawCircle(p.x, p.y, 30, paintCircle);
				//	p = Spot.getMySpotPoint();
				//}
			}
			canvas.restore();
			
		  }

		}
	
}
