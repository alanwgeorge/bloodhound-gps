package com.alangeorge.android.bloodhound;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This receiver can be used to start the {@link com.alangeorge.android.bloodhound.BloodHoundService}. In the manifest, this
 * receiver should have an IntentFilter for the android.intent.action.BOOT_COMPLETE action so the service is restarted on reboot.
 * <p/>
 * Commands for sending this receiver intents.
 * <pre>
 * {@code
 * $ adb -s 10.0.1.28:5555 shell am broadcast -a android.intent.action.BOOT_COMPLETED -n com.alangeorge.android.bloodhound./BloodHoundReceiver
 * $ adb -s 10.0.1.28:5555 shell am broadcast -a com.alangeorge.android.bloodhound.BloodHoundReceiver -n com.alangeorge.android.bloodhound./BloodHoundReceiver
 * }
 * </pre>
 */
@SuppressWarnings("WeakerAccess")
public class BloodHoundReceiver extends BroadcastReceiver {
    private static final String TAG = "BloodHoundReceiver";

    public static final String BLOODHOUND_RECEIVER_ACTION = "com.alangeorge.android.bloodhound.BloodHoundReceiver.start";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive("+ context + ", " +  intent + ")");

        if (intent.getAction().equalsIgnoreCase(BLOODHOUND_RECEIVER_ACTION) || intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, BloodHoundService.class);
            context.startService(serviceIntent);
        }
    }
}
