package com.spots.varramie;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.spots.liquidfun.Cluster;
import com.spots.liquidfun.ClusterManager;
import com.spots.liquidfun.Renderer;

import org.jbox2d.common.Vec2;

import java.io.FileDescriptor;
import java.io.IOException;
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
    private Sender					senderThread = new Sender();
    private  InetAddress			group_address;
    private boolean 				hasJoined = false;
    private boolean					receiveStop = false;
    private boolean					stopSender = false;
    private AliveThread				aliveThread = new AliveThread();
    private JoinThread				joinThread = new JoinThread();
    private ReceiveThread			receiveThread = new ReceiveThread();

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
		/*WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		multicastLock = wifi.createMulticastLock("multicastLock");
		multicastLock.setReferenceCounted(true);
		multicastLock.acquire();*/


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
    public boolean stopService(Intent name){
        disconnect();
        return super.stopService(name);
    }

    @Override
    public void onDestroy()
    {
        stopSelf();
        super.onDestroy();
    }

    public void receive(ByteBuffer bb){

        byte action = bb.get(0);
        int client_id = bb.get(2) & 0xff;
        Vec2 position_norm = new Vec2(bb.getFloat(3), bb.getFloat(7));
        float pressure = bb.getFloat(11);
        Vec2 velocity = new Vec2(bb.getFloat(15), bb.getFloat(19));

        Client.INSTANCE.receiveTouch(position_norm, pressure, client_id, action, velocity);
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
                            ClusterManager.myClusterId = id;
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
                            bb = ByteBuffer.wrap(new byte[]{ bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5],
                                    bytes[6], bytes[7], bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], bytes[14],
                                    bytes[15], bytes[16], bytes[17], bytes[18], bytes[19], bytes[20], bytes[21], bytes[22] }, 0, 23);
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
        }
    }

    private class AliveThread extends Thread{

        public AliveThread(){
            super("Alive Thread");
        }

        @Override
        public void run(){
            try {
                while(ClusterManager.myCluster == null){
                    sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            byte[] bytes = PDU_Factory.alive(ClusterManager.myCluster.getId());
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
            hasJoined = false;
            byte[] bytes = PDU_Factory.join();
            //byte checksum = bytes[1];
            //bytes[1] = (byte) -13;
            DatagramPacket dp = new DatagramPacket(bytes, bytes.length, server_address, server_port);
            try {
                while(!hasJoined){
                    multiSocket.send(dp);
                    sleep(1000);
                }
                sendMessage(OpCodes.JOIN);
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
        private void sendMessage(byte action) {
            Intent intent = new Intent("my-event");
            // add data
            intent.putExtra("action", action);
            LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
        }
    }

    private class Sender extends Thread{

        public Sender(){
            super("Sender Thread");
        }

        @Override
        public void run(){
            TouchState prevTouchState = new TouchState(OpCodes.ACTION_UP, new Vec2(0.0f, 0.0f), 0.32f, new Vec2(0.0f, 0.0f));

            try {
                while(ClusterManager.myCluster == null){
                    sleep(1000);
                }

                int id = ClusterManager.myCluster.getId();
                Vec2 position_norm = null, velocity = null;
                while(!stopSender){
                    TouchState touchState = Client.INSTANCE.takeTouchState();
                    if(!touchState.equals(prevTouchState)){
                        prevTouchState = touchState;
                        position_norm = touchState.getPositionNorm();
                        velocity = touchState.getVelocity();
                        byte action = touchState.getState();
                        byte[] bytes = PDU_Factory.touch_action(position_norm.x, position_norm.y, touchState.getPressure(), action, id, velocity.x, velocity.y);
                        DatagramPacket dp = new DatagramPacket(bytes, bytes.length, server_address, server_port);
                        try {
                            multiSocket.send(dp);
                        } catch (SocketException e) {

                        } catch (IOException e) {
                            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                byte[] quitBytes = PDU_Factory.quit(id);
                DatagramPacket dp = new DatagramPacket(quitBytes, quitBytes.length, server_address, server_port);
                multiSocket.send(dp);
            } catch (IOException e) {
                // Catches the exception but does nothing.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Makes the run thread of UDP to end and close the sockets.
     */
    public void disconnect(){
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

    }

    private class ChecksumException extends Exception {
        public ChecksumException(String str){
            super(str);
        }
    }
}