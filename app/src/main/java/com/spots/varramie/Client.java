package com.spots.varramie;

import android.graphics.Point;
import android.view.MotionEvent;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;


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
	private final LinkedBlockingDeque<TouchState> touchStates = new LinkedBlockingDeque<>();
	//private final TouchState		touchState = new TouchState(OpCodes.ACTION_UP, 0.0f, 0.0f, 0.35f);
	
	/**
	 * The constructor for class Client. Being a singleton implies that
	 * the constructor is empty because object wont and can't be
	 * generated. This class is initialized from main in the
	 * init method and used through Client.INSTANCE.[method].
	 */
	Client(){ }


	public void init(final IGUI gui){
		this.gui = gui;
		//touchStates.add(new TouchState(OpCodes.ACTION_UP, 0.0f, 0.0f, 0.32f));
		println("Initiating the client . . .");
	}

	public void sendTouchAction(final float x, final float y, final byte action, final float pressure) {


		// Set my spot status
		switch (action) {
			case OpCodes.ACTION_DOWN:
				Spot.SpotManager.activateMySpot(x, y, pressure);
				//connectedServer.interruptSenderThread();
				break;
			case OpCodes.ACTION_MOVE:
				Spot.SpotManager.updateMySpot(x, y, pressure);

				break;
			case OpCodes.ACTION_UP:
				Spot.SpotManager.deactivateMySpot(x, y, pressure);
				break;

			default:
				break;
		}

		TouchState t = new TouchState(action, x, y, pressure);
		touchStates.add(t);
	}

	public void receiveTouch(float x, float y, float pressure, int id, byte action){
		Spot s = Spot.SpotManager.getSpot(id);

		if(s == null)
			return;
		Spot mySpot = Spot.SpotManager.getMySpot();
		if(id == mySpot.getId())
			return;

		switch (action) {
			case OpCodes.ACTION_DOWN:
				s.activate(x, y, pressure);
				break;
			case OpCodes.ACTION_MOVE:
				s.update(x, y, pressure);
				break;
			case OpCodes.ACTION_UP:
				s.deactivate(x, y, pressure);
				break;

			default:
				break;
		}

		if(mySpot.isActive()){
			TouchState t = mySpot.getState();
			float my_x = t.getX();
			float my_y = t.getY();
			float my_pressure = t.getPressure();

			float dx = my_x - x;
			float dy = my_y - y;
			double s_pow = Math.pow(dx,2.0) + Math.pow(dy,2.0);

			if(s_pow <= Math.pow((my_pressure + pressure)*0.05f, 2.0)){
				this.gui.onColide();
				println("Touch!");	
			}
		}
	}

	public void shutDown() throws IOException{

	}

	/**
	 * Tells the GUI to print a string to the user of the interface. 
	 * This method invokes the runLater method of the GUI, and sends
	 * a new thread as a parameter. The GUI then runs the run
	 * method when possible.
	 * @param str The string to print.
	 */
	public synchronized void println(final String str){
		if(this.gui != null)
			this.gui.println(str);
	}

	public TouchState takeTouchState() throws InterruptedException {
		return touchStates.take();
	}

	public void addTouchState(final TouchState t){
		touchStates.add(t);
	}
}
