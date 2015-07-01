package com.spots.varramie;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.spots.facebook.MainFragment;
import com.spots.liquidfun.Cluster;
import com.spots.liquidfun.ClusterManager;
import com.spots.liquidfun.Renderer;

import org.jbox2d.common.Vec2;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

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

    private  InetAddress			group_address;
    private boolean 				hasJoined = false;
    private boolean					receiveStop = false;
    private boolean					stopSender = false;

    // Threads
    private Sender					senderThread;
    private AliveThread				aliveThread;
    private JoinThread				joinThread;
    private ReceiveThread			receiveThread;
    private String                  userId;

    //private WifiManager.MulticastLock multicastLock;


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
        try {
            byte[] byteAddress = intent.getByteArrayExtra("SERVER_IP_BYTE");
            this.server_address = InetAddress.getByAddress(byteAddress);
            this.server_port = intent.getIntExtra("SERVER_PORT_INT", 0);
            this.userId = intent.getStringExtra("USER_ID_STRING");
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

        senderThread = new Sender();
        aliveThread = new AliveThread();
        joinThread = new JoinThread();
        receiveThread = new ReceiveThread();

        this.receiveThread.start();	// Start the receive thread
        this.joinThread.start();	// Begin try to connect to the server

        return START_STICKY;
    }

    @Override
    public boolean stopService(Intent name){

        this.stop = true;
        this.stopSender = true;
        this.receiveStop = true;
        this.aliveThread.interrupt();
        this.joinThread.interrupt();
        this.senderThread.interrupt();
        this.receiveThread.interrupt();
        try {
            multiSocket.leaveGroup(group_address);
        } catch (IOException e) {
            // Catches the exception but does nothing
        }
        this.multiSocket.close();
        return super.stopService(name);
    }

    @Override
    public void onDestroy()
    {
        Client.INSTANCE.println("onDestory in service UDP.");
        stopSelf();
        super.onDestroy();
    }

    private class ReceiveThread extends Thread {

        public ReceiveThread(){
            super("Receive Thread");
        }

        @Override
        public void run() {
            Looper.prepare();
            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            System.setProperty("java.net.preferIPv4Stack", "true");

            while (!receiveStop) {
                try {
                    multiSocket.receive(receivePacket);

                    byte[] bytes = receivePacket.getData();
                    byte[] byteArray;
                    ByteBuffer bb;
                    byte action = bytes[0];
                    switch (action) {
                        case OpCodes.JOIN:
                            hasJoined = true;
                            break;
                        case OpCodes.NOTREG:
                            bb = ByteBuffer.wrap( new byte[]{bytes[0], bytes[1]}, 0, 2);
                            if(!Checksum.isCorrect(bb.array()))
                                throw new ChecksumException("Wrong checksum received in NOTREG");
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

                            }
                            break;
                        case OpCodes.QUIT:
                            bb = ByteBuffer.wrap( new byte[]{bytes[0], bytes[1], bytes[2] }, 0, 3);
                            if(!Checksum.isCorrect(bb.array()))
                                throw new ChecksumException("Wrong checksum received in QUIT");

                            joinThread.start();
                            // Restart the join process
                            break;
                        case OpCodes.ALIVE:
                            break;
                        default:

                            int default_id_length = bytes[2] & 0xff;
                            bb = ByteBuffer.allocateDirect(23 + default_id_length);
                            bb.put(bytes, 0, 23 + default_id_length);
                            byteArray = new byte[23 + default_id_length];
                            bb.position(0);
                            bb.get(byteArray);

                            if(!Checksum.isCorrect(byteArray))
                                throw new ChecksumException("The checksum of the package is not correct.");


                            byte[] default_id_array = new byte[default_id_length];
                            bb.position(3);
                            bb.get(default_id_array);
                            String default_id = new String(default_id_array, "UTF-8");


                            bb.position(3 + default_id_length);
                            float x = bb.getFloat();
                            float y = bb.getFloat();
                            float pressure = bb.getFloat();
                            float vel_x = bb.getFloat();
                            float vel_y = bb.getFloat();

                            Vec2 position_norm = new Vec2(x, y);
                            Vec2 velocity = new Vec2(vel_x, vel_y);

                            Client.INSTANCE.receiveTouch(position_norm, pressure, default_id, action, velocity);
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
        }
    }

    private class AliveThread extends Thread{

        public AliveThread(){
            super("Alive Thread");
        }

        @Override
        public void run(){
            Looper.prepare();
            try {
                while(ClusterManager.myClusterId == null){
                    sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            byte[] bytes = PDU_Factory.alive(ClusterManager.myClusterId);
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
            Looper.prepare();
            receiveStop = false;
            stopSender = false;
            hasJoined = false;
            byte[] bytes = PDU_Factory.join(userId);
            //byte checksum = bytes[1];
            //bytes[1] = (byte) -13;
            DatagramPacket dp = new DatagramPacket(bytes, bytes.length, server_address, server_port);
            try {
                while(!hasJoined){
                    multiSocket.send(dp);
                    sleep(1000);
                }
                sendMessage(OpCodes.JOIN, userId);
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

        // Send an Intent with an action named "my-event".
        private void sendMessage(byte action, String id) {
            Intent intent = new Intent("my-event");
            // add data
            intent.putExtra("action", action);
            intent.putExtra("id", id);
            LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
        }
    }

    private class Sender extends Thread{

        public Sender(){
            super("Sender Thread");
        }

        @Override
        public void run(){
            Looper.prepare();
            TouchState prevTouchState = new TouchState(OpCodes.ACTION_UP, new Vec2(0.0f, 0.0f), 0.32f, new Vec2(0.0f, 0.0f));
            try {
                while(ClusterManager.myCluster == null){
                    sleep(1000);
                }

                Vec2 position_norm = null, velocity = null;
                while(!stopSender){
                    TouchState touchState = Client.INSTANCE.takeTouchState();
                    if(!touchState.equals(prevTouchState)){
                        prevTouchState = touchState;
                        position_norm = touchState.getPositionNorm();
                        velocity = touchState.getVelocity();
                        byte action = touchState.getState();
                        byte[] bytes = PDU_Factory.touch_action(position_norm.x, position_norm.y, touchState.getPressure(), action, userId, velocity.x, velocity.y);
                        DatagramPacket dp = new DatagramPacket(bytes, bytes.length, server_address, server_port);
                        try {
                            multiSocket.send(dp);
                        } catch (SocketException e) {

                        } catch (IOException e) {
                            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                byte[] quitBytes = PDU_Factory.quit(userId);
                DatagramPacket dp = new DatagramPacket(quitBytes, quitBytes.length, server_address, server_port);
                multiSocket.send(dp);
            } catch (IOException e) {
                // Catches the exception but does nothing.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    private class ChecksumException extends Exception {
        public ChecksumException(String str){
            super(str);
        }
    }
}