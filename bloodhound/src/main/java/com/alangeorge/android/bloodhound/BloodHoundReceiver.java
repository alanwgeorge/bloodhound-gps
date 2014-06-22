package com.alangeorge.android.bloodhound;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * adb -s 10.0.1.28:5555 shell am broadcast -a android.intent.action.BOOT_COMPLETED -n com.alangeorge.android.bloodhound./BloodHoundReceiver
 * adb -s 10.0.1.28:5555 shell am broadcast -a om.alangeorge.android.bloodhound.BloodHoundReceiver -n com.alangeorge.android.bloodhound./BloodHoundReceiver
 */
@SuppressWarnings("WeakerAccess")
public class BloodHoundReceiver extends BroadcastReceiver {
    private static final String TAG = "BloodHoundReceiver";

    public static final String BLOODHOUND_RECEIVER_ACTION = "com.alangeorge.android.bloodhound.BloodHoundReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive("+ context + ", " +  intent + ")");
        Intent serviceIntent = new Intent(context, BloodHoundService.class);
        context.startService(serviceIntent);
    }
}
