package com.spots.varramie;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.NotificationCompat;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.AppInviteDialog;
import com.facebook.share.widget.ShareDialog;
import com.spots.depricated.Spot;
import com.spots.facebook.MainFragment;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.LinkedBlockingDeque;
/*import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.google.android.vending.licensing.AESObfuscator;*/

@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity{

    public static int threads = 0;

    private GLSurfaceView mGLView;
    public static int mNotificationHandel = 1;
    private static Context mContext;
    private Thread mNotificationThread;
    private static Notification mPokeNotification;
    private boolean stop = false;

    public static LinkedBlockingDeque<Notification> notificationsQ = new LinkedBlockingDeque<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        mPokeNotification = createPokeNotification("Poke", "Please come join me in Varramie");

        mGLView = new OpenGLSurfaceView(this);
        setContentView(mGLView);

        mNotificationThread = new Thread(){
            @Override
            public void run(){
                threads++;
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                while(!stop){
                    try {
                        Notification notification = notificationsQ.take();
                        notification.flags = Notification.FLAG_AUTO_CANCEL;

                        mNotificationManager.notify("Notification"/*String.valueOf(System.currentTimeMillis())*/, mNotificationHandel, notification);
                    } catch (InterruptedException e) {

                    }
                }

                threads--;
            }
        };
        mNotificationThread.start();
    }

    private static Notification createNotification(String title, String text, String link){

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.varramie_launcher_icon_trans)
                        .setContentTitle(title)
                        .setContentText(text);

        // pending intent is redirection using the deep-link
        Intent resultIntent = new Intent(Intent.ACTION_VIEW);
        resultIntent.setData(Uri.parse(link));

        PendingIntent pending = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pending);

        return notificationBuilder.build();
    }

    private static Notification createPokeNotification(String title, String text){

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.varramie_launcher_icon_trans)
                        .setContentTitle(title)
                        .setContentText(text);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(mContext, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent pending = stackBuilder.getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT );
        notificationBuilder.setContentIntent(pending);
        return notificationBuilder.build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);//invalidateOptionsMenu();
        MenuItem item;
        if(MainFragment.mAccessToken != null){
            item = menu.findItem(R.id.action_logout);
            menu.findItem(R.id.action_invite_facebook).setVisible(true);
        }
        else
            item = menu.findItem(R.id.action_use_facebook);
        item.setVisible(true);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, EmptyActivity.class));
                return true;
            case R.id.action_logout:
                AccessToken.setCurrentAccessToken(null);
                startActivityForResult(new Intent(this, com.spots.facebook.MainActivity.class), RESULT_OK);
                return true;
            case R.id.action_poke:
                // Send a poke push notification
                try{
                    Client.INSTANCE.addPackage(new PokePackage());
                    return true;
                }catch(InterruptedException e){
                    return false;
                }
            case R.id.action_use_facebook:
                startActivityForResult(new Intent(this, com.spots.facebook.MainActivity.class).putExtra("FROM_APP", true), RESULT_OK);
                return true;
            case R.id.action_invite_facebook:
                String appLinkUrl, previewImageUrl;

                appLinkUrl = "https://fb.me/1607257309532666";
                previewImageUrl = "https://fbcdn-photos-h-a.akamaihd.net/hphotos-ak-xtf1/t39.2081-0/11409246_1617226408535756_223822047_n.png";



                if (AppInviteDialog.canShow()) {
                    AppInviteContent content = new AppInviteContent.Builder()
                            .setApplinkUrl(appLinkUrl)
                            .setPreviewImageUrl(previewImageUrl)
                            .build();

                    AppInviteDialog appInviteDialog = new AppInviteDialog(this);
                    CallbackManager mCalbackManager = CallbackManager.Factory.create();
                    appInviteDialog.registerCallback(mCalbackManager, new FacebookCallback<AppInviteDialog.Result>()
                    {
                        @Override
                        public void onSuccess(AppInviteDialog.Result result)
                        {
                            Client.INSTANCE.println("onSuccess");
                        }

                        @Override
                        public void onCancel()
                        {
                            Client.INSTANCE.println("onCancel");
                        }

                        @Override
                        public void onError(FacebookException e)
                        {
                            Client.INSTANCE.println("onError");
                        }
                    });
                    appInviteDialog.show(content);
                }

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        invalidateOptionsMenu();
        super.onResume();
        mGLView.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        stopService(SplashScreen.mStartService);
        stop = true;
        mNotificationThread.interrupt();
        super.onDestroy();
        Log.d("MESSAGE", "Nr Threads: " + threads);
    }

    public static void notifyFormUrl(String url) throws InterruptedException {
        notificationsQ.put(createNotification("Form", "Please fill out this form", url));
    }

    public static void notifyPoke() throws InterruptedException {
        notificationsQ.put(createPokeNotification("Poke", "Please come join me in Varramie"));
    }

    @Override
    public void onBackPressed() {
        /*new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this activity?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();*/
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
    }
}