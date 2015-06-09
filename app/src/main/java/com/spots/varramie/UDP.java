package com.spots.varramie;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;

import dalvik.bytecode.OpcodeInfo;

/**
 * A basic class for handeling UDP connections.
 * @author Fredrik Johansson
 *
 */
public class UDP extends Service {
	
	protected Boolean				stop = false;

	private final int				PORT = 4446;
	private final String			IP = "224.0.0.251";

	private InetAddress				server_address;
	private  int					server_port;
	private final Queue<PDU>		packageQueue = new LinkedList<PDU>();
	private  MulticastSocket		multiSocket;
	private Sender					senderThread = new Sender();
	private  InetAddress			group_address;
	private boolean 				hasJoined = false;
	private boolean					receiveStop = false;
	private boolean					stopSender = false;
	private AliveThread				aliveThread = new AliveThread();
	private JoinThread				joinThread = new JoinThread();
	private ReceiveThread			receiveThread = new ReceiveThread();


	public UDP(){
		super();
	}

	@Override
	public IBinder onBind(Intent arg0)
	{

		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		MainActivity.unlockMulticast();

		try {
			byte[] byteAddress = intent.getByteArrayExtra("SERVER_IP_BYTE");
			this.server_address = InetAddress.getByAddress(byteAddress);
			this.server_port = intent.getIntExtra("SERVER_PORT_INT", 0);
		} catch (UnknownHostException e) {
			Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
		}

		try {
			this.multiSocket = new MulticastSocket(PORT);
			this.group_address = InetAddress.getByName(IP);
			this.multiSocket.joinGroup(new InetSocketAddress(this.group_address, this.PORT), NetworkInterface.getByName("wlan0"));
		} catch (IOException e) {
			Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
		}

		this.receiveThread.start();	// Start the receive thread
		this.joinThread.start();	// Begin try to connect to the server

		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		disconnect();
		super.onDestroy();
	}

	/**
	 * An abstract class with needs to be implemented by any class witch want to
	 * extend this class. 
	 * @param bytes The data containing bytes.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public void receive(byte[] bytes) throws IOException{
		PDU pdu = new PDU(bytes, 13);

		int client_touchX = (int) pdu.getInt(1);
		int client_touchY = (int) pdu.getInt(5);
		int client_id = (int) pdu.getInt(9);

		int local_id = Spot.getMySpotId();
		if(local_id == client_id)
			return;


		try{

			int action = pdu.getByte(0);
			Client.INSTANCE.receiveTouch(client_touchX, client_touchY, client_id, action);

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private class ReceiveThread extends Thread{

		public ReceiveThread(){
			super("ReceiveThread");
		}
		/**
		 * This run method overrides the run method in class Thread. It is invoked form
		 * the constructor of this class. It listens on the socket and pouches received
		 * data up to the ToServer and ToClient classes through the method receive.
		 */
		@Override
		public void run(){
			byte[] buffer = new byte[255];
			DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
			System.setProperty("java.net.preferIPv4Stack", "true");


			while(!receiveStop) {
				try {
					multiSocket.receive(receivePacket);

					PDU pdu = new PDU(buffer, buffer.length);
					int action = pdu.getByte(0);
					switch (action) {
						case OpCodes.JOIN:
							int id = (int) pdu.getInt(1);
							hasJoined = true;
							Spot.setMySpotId(id);
							break;
						case OpCodes.NOTREG:
							Client.INSTANCE.println("Need to establish a connection first.");
							if (!joinThread.isAlive()) {
								aliveThread.interrupt();
								try { aliveThread.join(); } catch (InterruptedException e) { }
								aliveThread = new AliveThread();
								stopSender = true;
								senderThread.interrupt();
								try { senderThread.join(); } catch (InterruptedException e) {}
								senderThread = new Sender();
								joinThread = new JoinThread();
								joinThread.start();

								Client.INSTANCE.println("Restarting the join process . . .");
							}
							break;
						case OpCodes.QUIT:
							Client.INSTANCE.println("You have been disconnected");
							joinThread.start();
							// Restart the join process
							Client.INSTANCE.println("Restarting the join process . . .");
							break;
						default:
							receive(buffer);
							break;
					}
				} catch (SocketException e) {
					break;
				} catch (IOException e) {
					Client.INSTANCE.println("IOException" + e.getMessage());
				}
			}
			MainActivity.lockMulticast();
		}
	}

	private class AliveThread extends Thread{

		public AliveThread(){
			super("Alive Thread");
		}

		@Override
		public void run(){
			PDU alivePDU = PDU_Factory.alive(Spot.getMySpotId());
			byte[] bytes = alivePDU.getBytes();
			DatagramPacket dp = new DatagramPacket(bytes, bytes.length, server_address, server_port);
			while(!stop){
				try {
					sleep(10000);
					multiSocket.send(dp);
				} catch (InterruptedException e) {
					break;
				} catch (IOException e) {
					Client.INSTANCE.println("IOException: " + e.getMessage());
				}
			}
		}
	}

	private class JoinThread extends Thread{

		public JoinThread(){
			super("Join Thread");
		}

		@Override
		public void run(){
			receiveStop = false;
			stopSender = false;
			Client.INSTANCE.println("Trying to establish a connection with server.");
			hasJoined = false;
			PDU joinPDU = PDU_Factory.join();
			byte[] bytes = joinPDU.getBytes();
			DatagramPacket dp = new DatagramPacket(bytes, bytes.length, server_address, server_port);
			try {
				while(!hasJoined){
					multiSocket.send(dp);
					sleep(1000);
					Client.INSTANCE.println("Resend JOIN package . . .");
				}
				Client.INSTANCE.println("Connection established.");
				aliveThread.start();	// Continually send alive messages to server
				senderThread.start();	// Start to send status to server
			} catch (InterruptedException e) {
				// Escapes the loop
			} catch (IOException e) {
				Client.INSTANCE.println("IOException: " + e.getMessage());
			}
		}
	}

	private class Sender extends Thread{

		public Sender(){
			super("Sender Thread");
		}

		@Override
		public void run(){
			Client.INSTANCE.println("Sending thread started.");
			long time = 10;
			while(!stopSender){
				TouchState touchState = Client.INSTANCE.getTouchState();
				PDU pdu = PDU_Factory.touch_action(touchState.getX(), touchState.getY(), touchState.getState());

				switch (touchState.getState()){
					case MotionEvent.ACTION_DOWN | MotionEvent.ACTION_MOVE:
						time = 10;
						break;
					case MotionEvent.ACTION_UP:
						time = 1000;
						break;
					default:
						time = 10;
						break;
				}
				try {
					Thread.sleep(time);
				} catch (InterruptedException e) {
				}

				try {
					byte[] bytes = pdu.getBytes();
					DatagramPacket dp = new DatagramPacket(bytes, bytes.length, server_address, server_port);
					multiSocket.send(dp);
				} catch (SocketException e) {
					break;
				} catch (IOException e) {
					Client.INSTANCE.println("IOException: " + e.getMessage());
				}

			}
			Client.INSTANCE.println("Sending thread stoped.");
			PDU quitPDU = new PDU(1);
			quitPDU.setByte(0, (byte) OpCodes.QUIT);
			byte[] bytes = quitPDU.getBytes();
			DatagramPacket dp = new DatagramPacket(bytes, bytes.length, server_address, server_port);

			try {
				for(int i = 0; i < 10; i++){
					multiSocket.send(dp);
				}
			} catch (IOException e) {
				// Catches the exception but does nothing.
			}
		}

	}

	protected void interruptSenderThread(){
		this.senderThread.interrupt();
	}

	/**
	 * Makes the run thread of UDP to end and close the sockets.
	 */
	public void disconnect(){
		Client.INSTANCE.println("Shuttding down . . .");
		this.stop = true;
		this.stopSender = true;
		this.receiveStop = true;
		this.aliveThread.interrupt();
		this.joinThread.interrupt();
		this.senderThread.interrupt();
		try {
			multiSocket.leaveGroup(group_address);
		} catch (IOException e) {
			// Catches the exception but does nothing
		}
		this.multiSocket.close();

		Client.INSTANCE.println("Connection closed");
	}
}
