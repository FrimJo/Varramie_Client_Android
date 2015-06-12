package com.spots.varramie;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
/*import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.google.android.vending.licensing.AESObfuscator;*/

@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity{

	//private final int PREFERENCE_MODE_PRIVATE = 0;
    private static WifiManager.MulticastLock multicastLock;
	private Intent startService;
	private GLSurfaceView mGLView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		multicastLock = wifi.createMulticastLock("multicastLock");

		//SpotSurfaceView surface = new SpotSurfaceView(this);

		mGLView = new OpenGLSurfaceView(this);
		setContentView(mGLView);

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
				this.vibrator.vibrate(300);
			}
		});

		StartServices();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		Spot.setDefaultHidden(pref.getBoolean("hide_others", false));
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
			stopService(this.startService);
			finish();
			return true;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy(){
		try {
			Client.INSTANCE.shutDown();
		} catch (IOException e) {
			// Catches the exception and does nothing.
		}
		System.out.println("onDestroy in MainActivity");
		super.onDestroy();
	}
	
	public SharedPreferences getPreferences(){
		return PreferenceManager.getDefaultSharedPreferences(this);
	}

	public static void unlockMulticast(){
		multicastLock.setReferenceCounted(true);
		multicastLock.acquire();
	}

	public static void lockMulticast(){
		if (multicastLock != null) {
			multicastLock.release();
			multicastLock = null;
		}
	}

	private void StartServices()
	{
		// Start UDPService
		this.startService = new Intent(getBaseContext(), UDP.class);
		byte[] byteAddress = new byte[0];
		try {
			//byteAddress = InetAddress.getByName("194.165.237.13").getAddress();
			byteAddress = InetAddress.getByName("194.165.237.13").getAddress();
			this.startService.putExtra("SERVER_IP_BYTE", byteAddress);
			this.startService.putExtra("SERVER_PORT_INT", 8001);
			this.startService(this.startService);
		} catch (UnknownHostException e) {
			Toast.makeText(getBaseContext(), "Could not start network service, pleae restart the app.", Toast.LENGTH_SHORT).show();
		}


	}
}
