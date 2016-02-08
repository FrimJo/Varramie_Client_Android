package com.spots.facebook;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.applinks.AppLinkData;

import bolts.AppLinks;

/**
 * Created by fredrikjohansson on 15-07-02.
 *
 */
public class IncomingAppLinks extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ...
        FacebookSdk.sdkInitialize(getApplicationContext());
        Uri targetUrl = AppLinks.getTargetUrlFromInboundIntent(this, getIntent());
        if (targetUrl != null) {
            Log.i("Activity", "App Link Target URL: " + targetUrl.toString());
        } else {
            AppLinkData.fetchDeferredAppLinkData(this, new AppLinkData.CompletionHandler() {
                @Override
                public void onDeferredAppLinkDataFetched(AppLinkData appLinkData) {
                    //process applink data
                }
            });
        }
    }

}
