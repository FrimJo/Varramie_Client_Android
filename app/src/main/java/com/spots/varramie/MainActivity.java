package com.spots.varramie;

import java.io.IOException;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
/*import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.google.android.vending.licensing.AESObfuscator;*/

@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity{

	//private final int PREFERENCE_MODE_PRIVATE = 0;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//		
//		boolean test = prefs.getBoolean("hide_others", false);
		
		//prefs.edit().putBoolean("hide_others", prefs.getBoolean("hide_others", true)).commit();
		
		SpotSurfaceView surface = new SpotSurfaceView(this);
		setContentView(surface);
		Client.INSTANCE.init(new IGUI(){

			private final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			
			@Override
			public void print(String str) {
				System.out.print(str);
				
			}

			@Override
			public void println(String str) {
				System.out.println(str);
				
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
			//startActivityForResult(new Intent(this, EmptyActivity.class), RESULT_OK);
			startActivityForResult(new Intent(this, EmptyActivity.class), RESULT_OK);
			return true;
		case R.id.action_exit:
			
			return true;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		try {
			Client.INSTANCE.shutDown();
		} catch (IOException e) {
			// Catches the exception and does nothing.
		}
		System.out.println("onDestroy in MainActivity");
	}
	
	public SharedPreferences getPreferences(){
		return PreferenceManager.getDefaultSharedPreferences(this);
	}
}
