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

import com.spots.liquidfun.Renderer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
/*import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.google.android.vending.licensing.AESObfuscator;*/

@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity{

	private GLSurfaceView mGLView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		GLSurfaceView view = new GLSurfaceView(this);

		view.setEGLContextClientVersion(2);

		Renderer renderer = new Renderer();
		view.setRenderer(renderer);

		setContentView(view);

		/*
		mGLView = new OpenGLSurfaceView(this);
		setContentView(mGLView);*/

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
			stopService(new Intent(".UDP"));
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
}
