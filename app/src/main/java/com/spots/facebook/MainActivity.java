package com.spots.facebook;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;

import com.spots.varramie.Client;
import com.spots.varramie.R;

/**
 * Created by fredrikjohansson on 15-06-30.
 */
public class MainActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Client.INSTANCE.println("Facebook.MainActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        Client.INSTANCE.println("Facebook.MainActivity onDestroy");
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
