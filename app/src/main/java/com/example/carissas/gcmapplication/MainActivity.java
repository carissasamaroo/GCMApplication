package com.example.carissas.gcmapplication;

import android.os.Bundle;
import android.view.Menu;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * http://www.programming-techniques.com/2014/01/google-cloud-messaging-gcm-in-android.html
 */
public class MainActivity extends Activity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String TAG = "GCMRelated";
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    String regid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = (Button) findViewById(R.id.register);

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
            regid = getRegistrationId(getApplicationContext());
            if(!regid.isEmpty()){
                button.setEnabled(false);
            }else{
                button.setEnabled(true);
            }//Closes if - else statement
        }//Closes if statement

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Check device for Play Services APK.
                if (checkPlayServices()) {
                    gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    regid = getRegistrationId(getApplicationContext());

                    /**
                     * http://stackoverflow.com/questions/25129611/gcm-register-service-not-available
                     * GCM Service is known to fail, which is why the application forces attempts for GCM registration in the background until
                     * a GCM registration ID is returned. After 5 unsuccessful attempts, the application would cease trying to register for GCM.
                     */
                    int noOfAttemptsAllowed = 5;
                    int noOfAttempts = 0;
                    boolean stopFetching = false;

                    while (!stopFetching){
                        noOfAttempts ++;

                        try{
                            Thread.sleep(2000);
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }//Closes try - catch clause

                        if (!regid.isEmpty() || noOfAttempts > noOfAttemptsAllowed) {
                            button.setEnabled(false);
                            Toast.makeText(getApplicationContext(), "Device already Registered or cannot register", Toast.LENGTH_SHORT).show();
                            stopFetching = true;
                        }else{
                            new RegisterApp(getApplicationContext(), gcm, getAppVersion(getApplicationContext())).execute();

                        }//Closes inner if - else statement
                    }//Closes while loop

                } else {
                    Log.i(TAG, "No valid Google Play Services APK found.");
                }//Closes outter if - else statement
            }//Closes onClick method
        });


    }//Closes onCreate method

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }//Closes onCreateOptionsMenu method

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }//Closes checkPlayServices method

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }//Closes if statement
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(getApplicationContext());
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }//Closes if statement
        return registrationId;
    }//Closes getRegistrationId method

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }//Closes getGCMPreferences method

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }//Closes try - catch clause
    }//Closes getAppVersion method

}//Closes MainActivity Class

