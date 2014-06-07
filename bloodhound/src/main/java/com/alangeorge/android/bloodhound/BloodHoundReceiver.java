package com.alangeorge.android.bloodhound;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.alangeorge.android.bloodhound.BloodHoundService.BLOODHOUND_SERVICE_ACTION;

public class BloodHoundReceiver extends BroadcastReceiver {
    private static final String TAG = "BloodHoundReceiver";
    public BloodHoundReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive("+ context + ", " +  intent + ")");
        Intent serviceIntent = new Intent(context, BloodHoundService.class);
        context.startService(serviceIntent);
    }
}
