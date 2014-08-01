package com.alangeorge.android.bloodhound;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class App extends Application {
    private static final String TAG = "App";
    private static final String PROPERTY_GCM_REG_ID = "gcm_registration_id";
    private static final String PROPERTY_APP_VERSION = "app_version";

    public static Context context;

    /**
     * Here we make a statically scoped public ApplicationContext available.
     * Currently this is used from the {@link com.alangeorge.android.bloodhound.BloodHoundService} to gain access to
     * the Applications ContentProvider
     */
    @Override public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }


    public static String getGcmRegistrationId() {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_GCM_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    public static int getAppVersion() {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static void storeRegistrationId(String gcmRegistrationId) {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion();
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_GCM_REG_ID, gcmRegistrationId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    private static SharedPreferences getGCMPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}