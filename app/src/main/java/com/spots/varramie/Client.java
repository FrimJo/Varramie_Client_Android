package com.spots.varramie;

import android.graphics.Point;
import android.text.method.Touch;
import android.view.MotionEvent;

import com.spots.liquidfun.Cluster;
import com.spots.liquidfun.ClusterManager;
import com.spots.liquidfun.Renderer;

import org.jbox2d.common.Vec2;

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
    private Thread sender;
    public boolean running = true;
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
        sender = new Thread(){

            @Override
            public void run(){

                while(running){
                    if(latestTouch != null){
                        break;
                    }
                    try{
                        sleep(10);
                    } catch (InterruptedException e) {

                    }
                }
                long time;
                TouchState ts;
                while(running){
                    try {
                        ts = new TouchState(latestTouch);
                        if(ClusterManager.myCluster != null){
                            time = 10;
                        }else{
                            ts.setState(OpCodes.ACTION_UP);
                            time = 100;
                        }
                        touchStates.add(ts);
                        sleep(time);
                    } catch (InterruptedException e) {

                    }
                }

            }
        };
        sender.start();
	}

    private TouchState latestTouch;

	public void sendTouchAction(final Vec2 position_screen, final byte action, final float pressure, final Vec2 velocity) {
        sender.interrupt();

        if(ClusterManager.myCluster == null){
            if(ClusterManager.myClusterIsCreating) return;
            ClusterManager.createNewCluster(ClusterManager.myClusterId, position_screen);
            ClusterManager.myClusterIsCreating = true;
            return;
        }else if(ClusterManager.myClusterIsCreating){
            ClusterManager.myClusterIsCreating = false;
        }


        // Set my spot status
        switch (action) {
			case OpCodes.ACTION_DOWN:
                ClusterManager.myCluster.push(position_screen, pressure);
				break;
			case OpCodes.ACTION_MOVE:
                ClusterManager.myCluster.push(position_screen, pressure);
				break;
			case OpCodes.ACTION_UP:
                ClusterManager.myCluster.push(position_screen, pressure);
                ClusterManager.myCluster.destroyPhysicsGroup();
                ClusterManager.myCluster = null;
				break;
			default:
				break;
		}

        Vec2 position_norm = new Vec2(position_screen.x / Renderer.screenW, position_screen.y / Renderer.screenH);

		//touchStates.add(new TouchState(action, position_norm, pressure, velocity));
        latestTouch = new TouchState(action, position_norm, pressure, velocity);
	}

	public void receiveTouch(Vec2 position_norm, float pressure, int id, byte action, Vec2 velocity){
        if(id == ClusterManager.myClusterId)
            return;

        Cluster c = ClusterManager.allClusters.get(id);

        // Convert normalized cordinates to screen cordinates
        Vec2 position_screen = new Vec2(position_norm.x * Renderer.screenW, position_norm.y * Renderer.screenH);

        Boolean isCreating = ClusterManager.allClustersIsCreating.get(id);

        if(c == null){
            if(isCreating == null){
                ClusterManager.allClustersIsCreating.put(id, true);
                ClusterManager.createNewCluster(id, position_screen);
                MainActivity.notfiyUserConnected();
            }
            return;
        }else if(isCreating.booleanValue()){
            ClusterManager.allClustersIsCreating.put(id, false);
        }




		switch (action) {
			case OpCodes.ACTION_DOWN:
                c.push(position_screen, pressure);
				break;
			case OpCodes.ACTION_MOVE:
                c.push(position_screen, pressure);
				break;
			case OpCodes.ACTION_UP:
                c.destroyPhysicsGroup();
                ClusterManager.allClustersIsCreating.remove(id);
				break;

			default:
				break;
		}



		/*if(mySpot.isActive()){
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
		}*/
	}

	public void shutDown(){
        running = false;
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
}
