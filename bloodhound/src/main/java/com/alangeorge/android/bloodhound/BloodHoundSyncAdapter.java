package com.alangeorge.android.bloodhound;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

public class BloodHoundSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "BloodHoundSyncAdapter";

    @SuppressWarnings("UnusedDeclaration")
    private ContentResolver contentProvider;

    public BloodHoundSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        contentProvider = context.getContentResolver();
    }

    @SuppressWarnings("UnusedDeclaration")
    public BloodHoundSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);

        contentProvider = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "onPerformSync()");
    }
}
