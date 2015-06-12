package com.spots.varramie;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.view.MotionEvent;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A basic class for handeling UDP connections.
 * @author Fredrik Johansson
 *
 */
public class UDP extends Service {

	private int						BUFFER_SIZE = 255;

	protected Boolean				stop = false;

	private final int				PORT = 4446;
	private final String			IP = "224.0.0.3";

	private InetAddress				server_address;
	private  int					server_port;
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
			this.multiSocket.joinGroup(group_address);
			//this.multiSocket.joinGroup(new InetSocketAddress(this.group_address, this.PORT), NetworkInterface.getByName("wlan0"));
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

	public void receive(ByteBuffer bb){

		int client_id = bb.get(2) & 0xff;
		float client_touchX = bb.getFloat(3);
		float client_touchY = bb.getFloat(7);

		try{
			byte action = bb.get(0);
			Client.INSTANCE.receiveTouch(client_touchX, client_touchY, client_id, action);

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private class ReceiveThread extends Thread {

		public ReceiveThread(){
			super("Receive Thread");
		}

		@Override
		public void run() {

			byte[] buffer = new byte[BUFFER_SIZE];
			DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
			System.setProperty("java.net.preferIPv4Stack", "true");

			while (!receiveStop) {
				try {
					multiSocket.receive(receivePacket);

					byte[] bytes = receivePacket.getData();
					ByteBuffer bb;
					byte action = bytes[0];
					switch (action) {
						case OpCodes.JOIN:
							bb = ByteBuffer.wrap( new byte[]{bytes[0], bytes[1], bytes[2]}, 0, 3);
							if(!Checksum.isCorrect(bb.array()))
								throw new ChecksumException("Wrong checksum received in JOIN");
							int id = bb.get(2) & 0xff;
							hasJoined = true;
							Spot.setMySpotId(id);
							break;
						case OpCodes.NOTREG:
							bb = ByteBuffer.wrap( new byte[]{bytes[0], bytes[1]}, 0, 2);
							if(!Checksum.isCorrect(bb.array()))
								throw new ChecksumException("Wrong checksum received in NOTREG");
							Client.INSTANCE.println("Need to establish a connection first.");
							if (!joinThread.isAlive()) {
								aliveThread.interrupt();
								try {
									aliveThread.join();
								} catch (InterruptedException e) {
								}
								aliveThread = new AliveThread();
								stopSender = true;
								senderThread.interrupt();
								try {
									senderThread.join();
								} catch (InterruptedException e) {
								}
								senderThread = new Sender();
								joinThread = new JoinThread();
								joinThread.start();

								Client.INSTANCE.println("Restarting the join process . . .");
							}
							break;
						case OpCodes.QUIT:
							bb = ByteBuffer.wrap( new byte[]{bytes[0], bytes[1], bytes[2] }, 0, 3);
							if(!Checksum.isCorrect(bb.array()))
								throw new ChecksumException("Wrong checksum received in QUIT");
							Client.INSTANCE.println("You have been disconnected");
							joinThread.start();
							// Restart the join process
							Client.INSTANCE.println("Restarting the join process . . .");
							break;
						case OpCodes.ALIVE:
							break;
						default:
							bb = ByteBuffer.wrap(new byte[]{ 	bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5],
																bytes[6], bytes[7], bytes[8], bytes[9], bytes[10] }, 0, 11);
							if(!Checksum.isCorrect(bb.array()))
								throw new ChecksumException("Wrong checksum received in DEFAULT");
							receive(bb);
							break;
					}
				} catch (SocketException e) {
					break;
				} catch (IOException e) {
					Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
				} catch (ChecksumException e) {
					e.printStackTrace();
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
			byte[] bytes = PDU_Factory.alive(Spot.getMySpotId());
			DatagramPacket dp = new DatagramPacket(bytes, bytes.length, server_address, server_port);
			while(!stop){
				try {
					sleep(10000);
					multiSocket.send(dp);
				} catch (InterruptedException e) {
					break;
				} catch (IOException e) {
					Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
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
			byte[] bytes = PDU_Factory.join();
			//byte checksum = bytes[1];
			//bytes[1] = (byte) -13;
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
				Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
			} catch (Exception e){
				Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class Sender extends Thread{

		public Sender(){
			super("Sender Thread");


		}


		@Override
		public void run(){
			TouchState prevTouchState = new TouchState(OpCodes.ACTION_UP, 0.0f, 0.0f);
			Client.INSTANCE.println("Sending thread started.");
			long time = 10;
			while(!stopSender){
				TouchState touchState = Client.INSTANCE.getTouchState();
				if(!touchState.equals(prevTouchState)){
					prevTouchState = touchState;
					byte action = touchState.getState();
					switch (action) {
						case OpCodes.ACTION_DOWN:
							time = 10;
							break;
						case OpCodes.ACTION_MOVE:
							time = 10;
							break;
						case OpCodes.ACTION_UP:
							time = 1000;
							break;
						default:
							time = 10;
							break;
					}


					byte[] bytes = PDU_Factory.touch_action(touchState.getX(), touchState.getY(), action, Spot.getMySpotId());
					DatagramPacket dp = new DatagramPacket(bytes, bytes.length, server_address, server_port);
					try {
						multiSocket.send(dp);
					} catch (SocketException e) {
						break;
					} catch (IOException e) {
						Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
					}
				}
				try {
					Thread.sleep(time);
				} catch (InterruptedException e) {
				}

			}
			Client.INSTANCE.println("Sending thread stoped.");
			byte[] quitBytes = PDU_Factory.quit(Spot.getMySpotId());
			DatagramPacket dp = new DatagramPacket(quitBytes, quitBytes.length, server_address, server_port);

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

	private class ChecksumException extends Exception {
		public ChecksumException(String str){
			super(str);
		}
	}
}
