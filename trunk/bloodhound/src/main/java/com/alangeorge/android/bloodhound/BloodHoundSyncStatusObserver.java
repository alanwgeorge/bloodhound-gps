package com.alangeorge.android.bloodhound;

import android.content.SyncStatusObserver;
import android.util.Log;

import static android.content.ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
import static android.content.ContentResolver.SYNC_OBSERVER_TYPE_PENDING;
import static android.content.ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS;

public class BloodHoundSyncStatusObserver implements SyncStatusObserver {
    private static final String TAG = "BloodHoundSyncStatusObserver";

    @Override
    public void onStatusChanged(int which) {
        switch (which) {
            case SYNC_OBSERVER_TYPE_SETTINGS:
                Log.d(TAG, "onStatusChanged(SYNC_OBSERVER_TYPE_SETTINGS)");
                break;
            case SYNC_OBSERVER_TYPE_ACTIVE:
                Log.d(TAG, "onStatusChanged(SYNC_OBSERVER_TYPE_ACTIVE)");
                break;
            case SYNC_OBSERVER_TYPE_PENDING:
                Log.d(TAG, "onStatusChanged(SYNC_OBSERVER_TYPE_PENDING)");
                break;
            default:
                Log.d(TAG, "Unknown status, onStatusChanged(" + which + ")");
        }
    }
}
