package com.alangeorge.android.bloodhound;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AuthenticatorService extends Service {
    private static final String TAG = "AuthenticatorService";

    private Authenticator authenticator;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();

        authenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind(" + intent + ")");
        return authenticator.getIBinder();
    }
}
