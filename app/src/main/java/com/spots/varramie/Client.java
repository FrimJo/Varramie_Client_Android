package com.spots.varramie;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.graphics.Point;
import android.net.ParseException;
import android.util.Log;
import android.view.MotionEvent;


/**
 * The main class of ChattApp 2.0 Client side. It uses the
 * Singleton design pattern. Controls all the connection
 * from the server to different connected clients.
 * @author Fredrik Johansson
 * @author Mattias Edin 
 */
public enum Client {
	INSTANCE;

	private IGUI					gui;
	private final TouchState		touchState = new TouchState(MotionEvent.ACTION_UP, 0, 0);
	
	/**
	 * The constructor for class Client. Being a singleton implies that
	 * the constructor is empty because object wont and can't be
	 * generated. This class is initialized from main in the
	 * init method and used through Client.INSTANCE.[method].
	 */
	Client(){ }
	
	/**
	 * This is the initialize method, it uses a GUI and
	 * setup the connection towards the name server (DNS).
	 * @param gui The GUI to be used with the client, needs to implement GUI_Interface_Client.java.
	 */
	public void init(final IGUI gui){
		this.gui = gui;
		println("Initiating the client . . .");
	}

	public void sendTouchAction(final int x, final int y, int action) throws IOException {
		synchronized (this.touchState){
			this.touchState.setState(x, y, action);
		}

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				Spot.activateMySpot(new Point(x, y));
				//connectedServer.interruptSenderThread();
				break;
			case MotionEvent.ACTION_MOVE:
				Spot.updateMySpot(new Point(x,y));

				break;
			case MotionEvent.ACTION_UP:
				Spot.deactivateMySpot(new Point(x,y));
				break;

			default:
				break;
		}
	}

	public void receiveTouch(int x, int y, int id, int action){
		Spot spot = Spot.getSpot(id);
		if(spot == null){
			spot = new Spot(id);
		}

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				spot.activate(new Point(x,y));
				break;
			case MotionEvent.ACTION_MOVE:
				spot.update(new Point(x,y));
				break;
			case MotionEvent.ACTION_UP:
				spot.deactivate(new Point(x,y));
				break;

			default:
				break;
		}

		if(Spot.isMySpotActive()){
			Point p = Spot.getMySpotPoint();
			if( (p.x > x-20 && p.x < x+20) && (p.y > y-20 && p.y < y+20 ) ){
				this.gui.onColide();
				println("Touch!");	
			}
		}
	}

	/**
	 * Signals the client to shut down and disconnect from all servers.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public void shutDown() throws IOException{
		for(int i = 0; i < Spot.sizeOfSpotsList(); i++){
	    	Spot s = Spot.getSpotAt(i);
	    	s.destroy();
	    }
	}

	/**
	 * Tells the GUI to print a string to the user of the interface. 
	 * This method invokes the runLater method of the GUI, and sends
	 * a new thread as a parameter. The GUI then runs the run
	 * method when possible.
	 * @param str The string to print.
	 */
	public synchronized void println(final String str){
		this.gui.println(str);
	}

	public TouchState getTouchState(){
		synchronized (this.touchState){
			return TouchState.cloneState(this.touchState);
		}
	}
}
