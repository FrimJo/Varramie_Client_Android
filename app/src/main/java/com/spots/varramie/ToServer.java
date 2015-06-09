package com.spots.varramie;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.content.Intent;
import android.os.IBinder;
import android.view.MotionEvent;

/**
 * This class handles all the connections of the clients towards the server.
 * @author Fredrik Johansson
 * @author Matias Edin
 *
 */
public class ToServer extends UDP{
	private static int			threadCounter = 0;

	/**
	 * Is called by the super class TCP when it receives a message from the server.
	 * After receiving the bytes this method checks the OpCode of the message and depending
	 * on the code it uses different cases to interpret the message act there after.
	 * @param bytes The bytes received from the connected server.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	@Override
	public synchronized void receive(byte[] bytes) throws IOException{


	}

}

