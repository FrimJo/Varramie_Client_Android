package com.spots.varramie;

import android.graphics.Point;
import android.text.method.Touch;
import android.util.Log;
import android.view.MotionEvent;

import com.spots.liquidfun.Cluster;
import com.spots.liquidfun.ClusterManager;
import com.spots.liquidfun.Physics;
import com.spots.liquidfun.Renderer;

import org.jbox2d.common.Vec2;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;


public enum Client {
    INSTANCE;

    private IGUI					gui;
    private final LinkedBlockingDeque<Object> touchStates = new LinkedBlockingDeque<>();
    private final ConcurrentHashMap<String, TouchState> touchM = new ConcurrentHashMap<>();
    public Thread sender;
    public boolean running = true;
    private boolean vibrate_collide;
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
                MainActivity.threads++;

                while(running){

                    try {
                        if(touchM.containsKey(ClusterManager.myClusterId)){
                            TouchState ts = touchM.get(ClusterManager.myClusterId);
                            touchStates.put(ts);
                            if(ts.getState() == OpCodes.ACTION_UP)
                                sleep(1000);
                            else
                                sleep(32);
                        }else{
                            touchStates.put(new TouchState(OpCodes.ACTION_UP, new Vec2(0.0f, 0.0f), 0.0f, new Vec2(0.0f, 0.0f), ClusterManager.myClusterId));
                            sleep(1000);
                        }
                    }catch(InterruptedException e){

                    }
                }
                MainActivity.threads--;
            }
        };

        if(!touchM.isEmpty()){
            touchM.clear();
        }
        Physics.clearClients();
    }

    public Set<Map.Entry<String, TouchState>> getTouchMapValues(){
        return touchM.entrySet();
    }

    public void sendTouchAction(final Vec2 position_screen, final byte action, final float pressure, final Vec2 velocity) {

        touchM.put(ClusterManager.myClusterId, new TouchState(action, position_screen, pressure, velocity, ClusterManager.myClusterId));


        sender.interrupt();
    }

    public void receiveTouch(Vec2 position_norm, float pressure, String id, byte action, Vec2 velocity){
        Vec2 position_screen = new Vec2(position_norm.x * Renderer.screenW, position_norm.y * Renderer.screenH);

        touchM.put(id, new TouchState(action, position_screen, pressure, velocity, id));

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

    public void addPackage(Object o) throws InterruptedException {
        touchStates.put(o);
    }

    public Object takePackage() throws InterruptedException {
        return touchStates.take();
    }

    public void ressetTouch(final String id){
        touchM.remove(id);
    }

    public void onColide(){
        this.gui.onColide();
    }

    public void setVibrateOnCollide(boolean vibrate){
        vibrate_collide = vibrate;
    }
}