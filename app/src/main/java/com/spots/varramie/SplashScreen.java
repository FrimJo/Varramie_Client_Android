package com.spots.varramie;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookActivity;
import com.spots.facebook.MainFragment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * Created by fredrikjohansson on 15-06-15.
 */
public class SplashScreen extends Activity {
    // Splash screen timer
    private TextView splasyText;
    public static Intent mStartService;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        final Context context = this;


        Client.INSTANCE.init(new IGUI() {

            private final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

            @Override
            public void print(String str) {
                Log.d("MESSAGE", str);
            }

            @Override
            public void println(String str) {
                Log.d("MESSAGE", str);
            }

            @Override
            public String getInput() {
                return "";
            }

            @Override
            public void onColide() {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                if(pref.getBoolean("vibrate_collide", false))
                    this.vibrator.vibrate(300);
            }
        });

        setContentView(R.layout.splash);
        splasyText = (TextView) findViewById(R.id.splashText);
        splasyText.setText("Connecting to server");
        StartServices();

    }

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent

            switch (intent.getByteExtra("action", (byte) 0)){
                case OpCodes.JOIN:
                    splasyText.setText("Connection established");
                    Intent i = new Intent(getApplication(), MainActivity.class);
                    finish();
                    startActivity(i);

                    break;
                case OpCodes.QUIT:

                    break;
                case OpCodes.DEFAULT:
                default:
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("my-event"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void StartServices()
    {
       // Start UDPService
        mStartService = new Intent(getBaseContext(), UDP.class);
        byte[] byteAddress;
        try {
            String userId;
            if(MainFragment.mAccessToken != null) {
                userId = MainFragment.mAccessToken.getUserId();
            }else{
                // Check if there is a stored UUID
                SharedPreferences preferences = getSharedPreferences("USER_PREFERENCES", 0);
                userId = preferences.getString("UUID", "");
                if(userId.equals("")){
                    SharedPreferences.Editor editor = preferences.edit();
                    userId = UUID.randomUUID().toString();
                    editor.putString("UUID" , userId);
                    editor.commit();
                }
            }

            //byteAddress = InetAddress.getByName("172.20.10.2").getAddress();  //My iPhone address
            byteAddress = InetAddress.getByName("194.165.237.13").getAddress();  //Work address
            //byteAddress = InetAddress.getByName("192.168.0.3").getAddress(); //Home address
            //byteAddress = InetAddress.getByName("130.239.184.13").getAddress(); //My mac at work address
            mStartService.putExtra("SERVER_IP_BYTE", byteAddress);
            //mStartService.putExtra("SERVER_PORT_INT", 8002);
            mStartService.putExtra("SERVER_PORT_INT", 8002); // Remote desktop
            mStartService.putExtra("USER_ID_STRING", userId);
            mStartService.addFlags(Service.START_STICKY_COMPATIBILITY);
            startService(mStartService);
        } catch (UnknownHostException e) {
            Toast.makeText(getBaseContext(), "Could not start network service, pleae restart the app.", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onBackPressed() {

    }

}
