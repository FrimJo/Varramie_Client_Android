package com.spots.varramie;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class EmptyActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getFragmentManager().beginTransaction().replace(android.R.id.content, new MainSettingsFragmenter()).commit();
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}
	
	
	
	public static class MainSettingsFragmenter extends PreferenceFragment{
		
		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
			
		}
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		switch (key) {
		case "hide_others":
			if(sharedPreferences.getBoolean(key, true))
				Spot.hideAllSpots();
			else
				Spot.showAllSpots();
			break;

		default:
			break;
		}
		
	}
}
