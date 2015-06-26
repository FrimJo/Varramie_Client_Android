package com.spots.varramie;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.NotificationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.spots.liquidfun.Renderer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingDeque;
/*import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.google.android.vending.licensing.AESObfuscator;*/

@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity{

    private GLSurfaceView mGLView;
    public static int usrConnectedHandel = 1;
    public static Notification usrConnectedNotification;
    public static LinkedBlockingDeque<Notification> notificationsQ = new LinkedBlockingDeque<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLView = new OpenGLSurfaceView(this);
        setContentView(mGLView);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Spot.setDefaultHidden(pref.getBoolean("hide_others", false));

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.varramie_launcher_icon_trans)
                        .setContentTitle("User connected!")
                        .setContentText("A user has connect to Varramie, go check it out.");

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        // mId allows you to update the notification later on.
        usrConnectedNotification = mBuilder.build();

        new Thread(){
            @Override
            public void run(){
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                try {
                    while(true){
                        Notification notification = notificationsQ.take();
                        mNotificationManager.notify(usrConnectedHandel, notification);
                    }
                } catch (InterruptedException e) {

                }
            }
        }.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(new Intent(this, EmptyActivity.class), RESULT_OK);
                return true;
            case R.id.action_exit:
                stopService(new Intent(".UDP"));
                finish();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();
        mGLView.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onDestroy(){
        Client.INSTANCE.shutDown();
        System.out.println("onDestroy in MainActivity");
        super.onDestroy();
    }

    public static void notfiyUserConnected(){
        notificationsQ.push(usrConnectedNotification);
    }

    public SharedPreferences getPreferences(){
        return PreferenceManager.getDefaultSharedPreferences(this);
    }
}