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

import org.jbox2d.collision.Collision;
import org.jbox2d.common.Vec2;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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

    private InetAddress				server_address;
    private int					    server_port;
    private DatagramSocket	        socket;

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
            ClusterManager.myClusterId = userId;
        } catch (UnknownHostException e) {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }

        try {
            this.socket = new DatagramSocket();
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
        this.socket.close();
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
            MainActivity.threads++;
            Looper.prepare();
            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            System.setProperty("java.net.preferIPv4Stack", "true");

            while (!receiveStop) {
                try {
                    socket.receive(receivePacket);

                    byte[] bytes = receivePacket.getData();
                    byte action = bytes[0];
                    switch (action) {
                        case OpCodes.JOIN:
                            /*int join_id_length = bytes[2] & 0xff;
                            ByteBuffer join_buffer = ByteBuffer.allocateDirect(3 + join_id_length);
                            join_buffer.put(bytes, 0, 3 + join_id_length);
                            byte[] join_byteArray = new byte[3 + join_id_length];
                            join_buffer.position(0);
                            join_buffer.get(join_byteArray);

                            if(!Checksum.isCorrect(join_byteArray))
                                throw new ChecksumException("The checksum of the package is not correct.");

                            byte[] join_id_array = new byte[join_id_length];
                            join_buffer.position(3);
                            join_buffer.get(join_id_array);
                            String join_id = new String(join_id_array, "UTF-8");

                            if(join_id.equals(userId))*/
                                hasJoined = true;
                            //else if(hasJoined)
                                // User joined is another user, and active user has joined


                            break;
                        case OpCodes.NOTREG:
                            if(!Checksum.isCorrect(new byte[]{bytes[0], bytes[1]}))
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
                            if(!Checksum.isCorrect(new byte[]{bytes[0], bytes[1], bytes[2]}))
                                throw new ChecksumException("Wrong checksum received in QUIT");

                            joinThread.start();
                            // Restart the join process
                            break;
                        case OpCodes.ALIVE:
                            break;
                        case OpCodes.FORM:

                            // Converts the buffer in to an array to
                            // be able to see whether the checksum is correct.
                            int form_url_length = bytes[2] & 0xff;
                            int total_length = 3 + form_url_length;
                            ByteBuffer form_buffer = ByteBuffer.allocateDirect(total_length);
                            form_buffer.put(bytes, 0, total_length);
                            byte[] form_byteArray = new byte[total_length];
                            form_buffer.position(0);
                            form_buffer.get(form_byteArray);

                            if(!Checksum.isCorrect(form_byteArray))
                                throw new ChecksumException("The checksum of the package is not correct.");

                            // Gets the URL from the package
                            byte[] form_url_array = new byte[form_url_length];
                            form_buffer.position(3);
                            form_buffer.get(form_url_array);
                            String form_url = new String(form_url_array, "UTF-8");
                            MainActivity.notifyFormUrl(form_url.concat("="+userId));
                            break;
                        case OpCodes.POKE:
                            Client.INSTANCE.println("Received packet POKE.");

                            int poke_id_length = bytes[2] & 0xff;		// Retrieves the length of the id by converting it from 'byte' to 'int'
                            int totalLength_poke = 3 + poke_id_length;

                            ByteBuffer bb = ByteBuffer.allocateDirect(totalLength_poke);
                            bb.put(bytes, 0, totalLength_poke);
                            byte[] byteArray = new byte[totalLength_poke];
                            bb.position(0);
                            bb.get(byteArray);

                            if(!Checksum.isCorrect(byteArray))
                                throw new ChecksumException("The checksum of the package is not correct.");


                            MainActivity.notifyPoke();


                            break;
                        default:

                            int default_id_length = bytes[2] & 0xff;
                            ByteBuffer default_buffer = ByteBuffer.allocateDirect(23 + default_id_length);
                            default_buffer.put(bytes, 0, 23 + default_id_length);
                            byte[] default_byteArray = new byte[23 + default_id_length];
                            default_buffer.position(0);
                            default_buffer.get(default_byteArray);

                            if(!Checksum.isCorrect(default_byteArray))
                                throw new ChecksumException("The checksum of the package is not correct.");


                            byte[] default_id_array = new byte[default_id_length];
                            default_buffer.position(3);
                            default_buffer.get(default_id_array);
                            String default_id = new String(default_id_array, "UTF-8");


                            default_buffer.position(3 + default_id_length);
                            float x = default_buffer.getFloat();
                            float y = default_buffer.getFloat();
                            float pressure = default_buffer.getFloat();
                            float vel_x = default_buffer.getFloat();
                            float vel_y = default_buffer.getFloat();

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
                } catch (InterruptedException e){

                }
            }
            MainActivity.threads--;
        }
    }

    private class AliveThread extends Thread{

        public AliveThread(){
            super("Alive Thread");
        }

        @Override
        public void run(){
            MainActivity.threads++;
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
                    socket.send(dp);
                } catch (InterruptedException e) {
                    break;
                } catch (IOException e) {
                    Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
            MainActivity.threads--;
        }
    }


    private class JoinThread extends Thread{

        public JoinThread(){
            super("Join Thread");
        }

        @Override
        public void run(){
            MainActivity.threads++;
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
                    socket.send(dp);
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
            MainActivity.threads--;
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
            MainActivity.threads++;
            Looper.prepare();
            try {

                Vec2 position_screen = null, velocity = null;
                try{ Client.INSTANCE.sender.start(); } catch(IllegalThreadStateException e){}

                while(!stopSender){
                    Object o = Client.INSTANCE.takePackage();
                    byte[] bytes;
                    if(o.getClass() == TouchState.class){
                        TouchState touchState = (TouchState) o;

                        position_screen = touchState.getPositionScreen();

                        Vec2 position_norm = new Vec2(position_screen.x / Renderer.screenW, position_screen.y / Renderer.screenH);

                        velocity = touchState.getVelocity();
                        byte action = touchState.getState();
                        bytes = PDU_Factory.touch_action(position_norm.x, position_norm.y, touchState.getPressure(), action, userId, velocity.x, velocity.y);
                        DatagramPacket dp = new DatagramPacket(bytes, bytes.length, server_address, server_port);
                        try {
                            socket.send(dp);
                        } catch (SocketException e) {
                            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }else if(o.getClass() == CollisionPackage.class){
                        CollisionPackage cp = (CollisionPackage) o;
                        bytes = PDU_Factory.collision(cp.position.x, cp.position.y, cp.idA, cp.idB);
                        DatagramPacket dp = new DatagramPacket(bytes, bytes.length, server_address, server_port);
                        try {
                            socket.send(dp);
                        } catch (SocketException e) {

                        } catch (IOException e) {
                            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }else if(o.getClass() == PokePackage.class){
                        bytes = PDU_Factory.poke(userId);
                        DatagramPacket dp = new DatagramPacket(bytes, bytes.length, server_address, server_port);
                        try {
                            socket.send(dp);
                        } catch (SocketException e) {

                        } catch (IOException e) {
                            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                byte[] quitBytes = PDU_Factory.quit(userId);
                DatagramPacket dp = new DatagramPacket(quitBytes, quitBytes.length, server_address, server_port);
                socket.send(dp);
            } catch (IOException e) {
                // Catches the exception but does nothing.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MainActivity.threads--;
        }

    }


    private class ChecksumException extends Exception {
        public ChecksumException(String str){
            super(str);
        }
    }
}