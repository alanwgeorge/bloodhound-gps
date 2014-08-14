package com.alangeorge.android.bloodhound;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BloodHoundSyncService extends Service {
    private static final String TAG = "BloodHoundSyncService";

    private static BloodHoundSyncAdapter bloodHoundSyncAdapter = null;
    private static final Object lock = new Object();

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();

        synchronized (lock) {
            if (bloodHoundSyncAdapter == null) {
                bloodHoundSyncAdapter = new BloodHoundSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind(" + intent + ")");
        return bloodHoundSyncAdapter.getSyncAdapterBinder();
    }
}
