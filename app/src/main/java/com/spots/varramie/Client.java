package com.spots.varramie;

import android.graphics.Point;
import android.view.MotionEvent;
import java.io.IOException;


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
	private final TouchState		touchState = new TouchState(OpCodes.ACTION_UP, (short) 0, (short) 0);
	
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

	public void sendTouchAction(final float x, final float y, byte action) {
		synchronized (this.touchState){
			this.touchState.setState(x, y, action);
		}

		// Set my spot status
		switch (action) {
			case OpCodes.ACTION_DOWN:
				Spot.activateMySpot(x, y);
				//connectedServer.interruptSenderThread();
				break;
			case OpCodes.ACTION_MOVE:
				Spot.updateMySpot(x, y);

				break;
			case OpCodes.ACTION_UP:
				Spot.deactivateMySpot(x, y);
				break;

			default:
				break;
		}
	}

	public void receiveTouch(float x, float y, int id, int action){
		Spot s = Spot.getSpotAt(id);

		if(s == null)
			return;

		switch (action) {
			case OpCodes.ACTION_DOWN:
				s.activate(x, y);
				break;
			case OpCodes.ACTION_MOVE:
				s.update(x, y);
				break;
			case OpCodes.ACTION_UP:
				s.deactivate(x ,y);
				break;

			default:
				break;
		}

		Spot mySpot = Spot.getMySpot();
		if(mySpot.isActive()){

			float my_x = mySpot._dx;
			float my_y = mySpot._dy;
			if( (my_x > x-160.0f && my_x < x+160.0f) && (my_y > y-160.0f && my_y < y+160.0f ) ){
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
