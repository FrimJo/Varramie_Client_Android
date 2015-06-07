package com.spots.varramie;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.graphics.Point;
import android.net.ParseException;
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
	
	private IGUI			gui;
	private ToServer		connectedServer;
	//private ToDNS			dns_server;
	
	/**
	 * The constructor for class Client. Being a singleton implies that
	 * the constructor is empty because object wont and can't be
	 * generated. This class is initialized from main in the
	 * init method and used through Client.INSTANCE.[method].
	 */
	private Client(){ }
	
	/**
	 * This is the initialize method, it uses a GUI and
	 * setup the connection towards the name server (DNS).
	 * @param gui The GUI to be used with the client, needs to implement GUI_Interface_Client.java.
	 */
	public void init(final IGUI gui){
		this.gui = gui;
		println("Initiating the client . . .");
		
		boolean end = false;
		println("Please enter IP and port of the server (xxx.xxx.xxx.xxx:yyyy): ");
		while(!end){
			String in = gui.getInput();
			//in = "10.0.2.2:8000";
			//in = "130.239.237.19:8000";
			in = "194.165.237.13:8001";
			String[] server_ip_port_array = in.split(":");
			String server_ip = server_ip_port_array[0];
			try{
				int server_port = Integer.parseInt(server_ip_port_array[1]);
				addToServer(server_ip, server_port);
				end = true;
			}catch(IOException e){
				e.printStackTrace();
				println("Wrong IP and/or port, please try again.");
			}catch(ParseException e3){
				e3.printStackTrace();
				println("Wrong IP and/or port, please try again.");
			}catch(Exception e2){
				e2.printStackTrace();
				println("Got something else wrong.");
			}
		}

	}
	
	public void receiveId(int id){

	}
	
	public void receiveTouch(int x, int y, int id, int action){		
		Spot spot = Spot.getSpot(id);
		if(spot == null){
			spot = new Spot(id, false);
			Spot.putSpot(id, spot);
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
	 * This method creates a new ToServer object. 
	 * @param server_ip IP of server.
	 * @param server_port Port of server.
	 * @param topic The topic of the server.
	 * @param nr_clients Number of currently connected clients.
	 * @return A new ready to connect server connection, defined by the values received.
	 * @throws UnknownHostException Thrown to indicate that the IP address of a host could not be determined.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public void addToServer(final String server_ip, final int server_port) throws UnknownHostException, IOException{
		this.connectedServer = new ToServer(server_ip, server_port);
	}
	
	/**
	 * Disconnects the client from a specific server.
	 * @param server Server to disconnect from.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public void disconnectFromServer(final ToServer server) throws IOException{
		server.disconnect();
	}
	
	/**
	 * Signals the client to shut down and disconnect from all servers.
	 * @param servers A list of servers to disconnect from.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public void shutDown() throws IOException{
		for(int i = 0; i < Spot.sizeOfSpotsList(); i++){
	    	Spot s = Spot.getSpotAt(i);
	    	s.destroy();
	    }
		this.connectedServer.disconnect();
		
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

	public void sendTouchAction(final int x, final int y, int action) throws SocketException, IOException {
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Spot.activateMySpot(new Point(x,y));
			this.connectedServer.sendTouchAction(x, y, OpCodes.DOWN);
			break;
		case MotionEvent.ACTION_MOVE:
			Spot.updateMySpot(new Point(x,y));
			this.connectedServer.sendTouchAction(x, y, OpCodes.MOVE);
			break;
		case MotionEvent.ACTION_UP:
			Spot.deactivateMySpot(new Point(x,y));
			this.connectedServer.sendTouchAction(x, y, OpCodes.UP);
			break;

		default:
			break;
		}
	}
}
