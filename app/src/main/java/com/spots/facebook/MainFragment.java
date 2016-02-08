package com.spots.facebook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.spots.varramie.*;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by fredrikjohansson on 15-06-30.
 */
public class MainFragment extends Fragment{

    public static AccessToken mAccessToken;
    private static Profile mProfile;

    private CallbackManager mCallbackManager;
    private AccessTokenTracker mTokenTracker;
    private ProfileTracker mProfileTracker;


    private FacebookCallback<LoginResult> mCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            Client.INSTANCE.println("Facebook onSuccess");
            mAccessToken = AccessToken.getCurrentAccessToken();
            Intent intent = new Intent(getActivity(), SplashScreen.class);
            startActivity(intent);
        }

        @Override
        public void onCancel() {
            Client.INSTANCE.println("Facebook Cancel");
        }

        @Override
        public void onError(FacebookException e) {
            Client.INSTANCE.println("FacebookException");
            e.printStackTrace();
        }
    };

    public MainFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());

        mCallbackManager = CallbackManager.Factory.create();

        mTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
                Client.INSTANCE.println("onCurrentAccessTokenChanged");
                mAccessToken = newToken;
                if(mAccessToken != null){
                    startActivity(new Intent(getActivity(), SplashScreen.class));
                }
            }
        };

        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                mProfile = newProfile;
            }
        };

        mTokenTracker.startTracking();
        mProfileTracker.startTracking();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        Intent remoteIntent = getActivity().getIntent();

        boolean isFromApp = remoteIntent.getBooleanExtra("FROM_APP", false);
        if(isFromApp){
            Client.INSTANCE.println("Facebook onViewCreated isFromApp");

            LoginManager loginManager = LoginManager.getInstance();
            loginManager.registerCallback(mCallbackManager, mCallback);

            Collection<String> permissions = Arrays.asList();
            loginManager.logInWithReadPermissions(this, permissions);

        }else{
            Client.INSTANCE.println("Facebook onViewCreated !isFromApp");
            mAccessToken = null;
            Intent intent = new Intent(getActivity(), SplashScreen.class);
            //getActivity().finish();
            startActivity(intent);
            //return;
        }
/*
        // Setup the login button for facebook users
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions();
        loginButton.setFragment(this);
        loginButton.registerCallback(mCallbackManager, mCallback);



        // Setup the bypass facebook button for users without facebook
        Button noFacebook = (Button) view.findViewById(R.id.no_facebook_button);
        noFacebook.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mAccessToken = null;
                Intent intent = new Intent(getActivity(), SplashScreen.class);
                getActivity().finish();
                startActivity(intent);
            }
        });
*/
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStop() {
        super.onStop();
        mTokenTracker.stopTracking();
        mProfileTracker.stopTracking();
    }

}
