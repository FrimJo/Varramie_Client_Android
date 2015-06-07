package com.spots.varramie;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A basic class for handeling UDP connections.
 * @author Fredrik Johansson
 *
 */
public abstract class UDP extends Thread{
	
	protected Boolean				stop = new Boolean(false);
	
	private String					server_ip;
	private int						server_port;
	private Queue<PDU>				packageQueue = new LinkedList<PDU>();
	private DatagramSocket			socket;
	private MulticastSocket			multiSocket;
	private Sender					sender;
	
	/**
	 * Constructor of UDP class.
	 * @param thread_name The name of the thread.
	 * @param server_ip Server IP.
	 * @param server_port Server port.
	 * @throws UnknownHostException Thrown to indicate that the IP address of a host could not be determined.
	 * @throws SocketException Thrown to indicate that there is an error creating or accessing a Socket.
	 */
	public UDP(String thread_name, String server_ip, int server_port) throws IOException {
		super(thread_name);
		this.server_ip = server_ip;
		this.server_port = server_port;
		this.socket = new DatagramSocket();
		this.multiSocket = new MulticastSocket(4446);
		this.start();
		this.sender = new Sender();
		this.sender.start();

		// The hard-coded port number is 4446 (the client must have a MulticastSocket bound to this port)
		// Created in this way, the DatagramPacket is destined for all clients listening to port number 4446 who are member of the "203.0.113.0" group.
		// To become a member of the "203.0.113.0" group, the client calls the MulticastSocket's joinGroup method with the InetAddress that identifies the group
	}
	
	/**
	 * This run method overrides the run method in class Thread. It is invoked form
	 * the constructor of this class. It listens on the socket and pouches received
	 * data up to the ToServer and ToClient classes through the method receive.
	 */
	@Override
	public void run(){
		try {
			socket = new DatagramSocket();
			byte[] buffer = new byte[65535];
			DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
			while(!stop){
				socket.receive(receivePacket);
				try{
					receive(buffer);
				} catch (IOException e) {
					disconnect();
				}
			}
		} catch (SocketException e) {
			disconnect();
		} catch (IOException e) {
			//Catches the IOException but keeps the thread running.
		}
	}
	
	/**
	 * An abstract class with needs to be implemented by any class witch want to
	 * extend this class. 
	 * @param bytes The data containing bytes.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public abstract void receive(byte[] bytes) throws IOException;
	
	/**
	 * Sends a PDU through the UDP protocol using datagram packets.
	 * @param pdu PDU to send.
	 * @throws UnknownHostException Thrown to indicate that the IP address of a host could not be determined.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public void sendPDU(final PDU pdu) throws UnknownHostException, IOException{
		synchronized (packageQueue){
			this.packageQueue.add(pdu);
		}
	}

	private class Sender extends Thread{



		@Override
		public void run(){
			try {
				socket = new DatagramSocket();
				InetAddress address = InetAddress.getByName(server_ip);

				while(!stop){
					PDU pdu;
					synchronized (packageQueue){
						pdu = packageQueue.poll();
					}
					if(pdu != null){

						byte[] bytes = pdu.getBytes();
						int length = bytes.length;

						DatagramPacket dp = new DatagramPacket(bytes, length, address, server_port);
						try{
							socket.send(dp);
						} catch (IOException e2) {
							String str = "couldn't send data";
						} catch (Exception e3) {
							e3.printStackTrace();
						}
					}

				}
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}

		/**
	 * Makes the run thread of UDP to end and close the sockets.
	 */
	public void disconnect(){
		this.stop = true;
	}
}
