package com.spots.varramie;


import java.util.ArrayList;

import android.graphics.Color;
import android.graphics.Point;
import android.util.SparseArray;

public class Spot {

	private static SparseArray<Spot> allSpots = new SparseArray<Spot>();
	private static Spot mySpot = new Spot(-1, true);
	private static boolean defaultHiden = false;
	
	private int color = ColorManager.getRealRandomColor();
	private ArrayList<Point> pointsList = new ArrayList<Point>();
	private boolean active = false;
	private boolean hidden = defaultHiden;
	private final RemoveThread removeThread = new RemoveThread(this);
	//private final Point currentPoint = new Point();
	private final int id;
	
	private int index = 0;
	
	public Spot(final int id, boolean isMySpot){
		this.id = id;
		if(!isMySpot)
			allSpots.put(id, this);
		this.removeThread.setRunning(true);
		this.removeThread.start();
	}
	
	public int getColor(){
		return this.color;
	}
	
	public int getDynamicColor(int index){
		int red = Color.red(this.color);
		int green = Color.green(this.color);
		int blue = Color.blue(this.color);
		
		
		return Color.argb(255/(index+1), red, green, blue);
	}
	
	public synchronized void activate(Point p){
		this.removeThread.interrupt();
		this.active = true;
		update(p);
	}
	
	public synchronized void update(Point p){
		if(isActive()){
			this.pointsList.add(p);
			
//			this.currentPoint.x = p.x;
//			this.currentPoint.y = p.y;
		}
	}
	
	public synchronized void deactivate(Point p){
		update(p);
		this.active = false;
	}
	
	public synchronized boolean isActive(){
//		if(this.hidden)
//			return false;
//		else if(!this.active)
//			return false;
//		return true;
		
		return (!this.hidden && this.active);
	}
	
	public synchronized Point getPoint(){

		Point p = null;
		try{
			p = this.pointsList.get(this.index);
			this.index++;
		}catch(IndexOutOfBoundsException e){
			this.index = 0;
		}
		return p;	
	}
	
	public synchronized int sizeOfPointList(){
		return this.pointsList.size();
	}
	
	public synchronized void removePoint(){
		if(!this.pointsList.isEmpty())
			this.pointsList.remove(0);
	}
	
	public synchronized void destroy(){
		allSpots.remove(this.id);
		
	    boolean retry = true;
	    this.removeThread.setRunning(false);
	    while (retry) {
	      try {
	        this.removeThread.join();
	        retry = false;
	      } catch (InterruptedException e) {
	      }
	    }
	}
	
	public synchronized int getId(){
		return this.id;
	}
	
	public synchronized static Spot getSpotAt(int index){
		return allSpots.valueAt(index);
	}
	
	public synchronized static Spot getSpot(int key){
		return allSpots.get(key);
	}

	public synchronized static void putSpot(int key, Spot value){
		allSpots.put(key, value);
	}
	
	public synchronized static int sizeOfSpotsList(){
		return allSpots.size();
	}
	
	public synchronized static boolean isListEmpty(){
		return allSpots.size() == 0;
	}
	
	public synchronized static Point getMySpotPoint(){
		return mySpot.getPoint();
	}
	
	public synchronized static void activateMySpot(Point p){
		mySpot.activate(p);
	}
	
	public synchronized static void updateMySpot(Point p){
		mySpot.update(p);
	}
	public synchronized static void deactivateMySpot(Point p){
		mySpot.deactivate(p);
	}
	
	public synchronized static int getMySpotDynamicColor(int index){
		int red = Color.red(mySpot.color);
		int green = Color.green(mySpot.color);
		int blue = Color.blue(mySpot.color);
		
		
		return Color.argb(255/(index+1), red, green, blue);
	}
	
	public synchronized static boolean isMySpotActive(){
		return mySpot.isActive();
	}
	
	public synchronized static void hideAllSpots(){
		for(int i = 0; i < sizeOfSpotsList(); i++){
			allSpots.valueAt(i).hidden = true;
		}
	}
	
	public synchronized static void showAllSpots(){
		for(int i = 0; i < sizeOfSpotsList(); i++){
			allSpots.valueAt(i).hidden = false;
		}
	}
	
	public synchronized static void setDefaultHidden(boolean value){
		defaultHiden = value;
	}
	
	@Override
	public synchronized boolean equals(Object o){
		if(o == null)
			return false;
		
		if(o.getClass() != this.getClass())
			return false;
		
		Spot s = (Spot) o;
		
		if(s.getId() != this.id)
			return false;
		
		return true;
	}
	
	private class RemoveThread extends Thread{
		private boolean run = false;
		private Spot spot;	
		
		public RemoveThread(Spot spot){
			this.spot = spot;
		}
		
		public void run() {
			while (run) {
				int size = this.spot.sizeOfPointList();
				try {
					sleep(10);
				} catch (InterruptedException e) {

				}
				if(size > 1)
					this.spot.removePoint();
		    }
		}
		
		public void setRunning(boolean status) { 
			run = status;
		}
	}
}
