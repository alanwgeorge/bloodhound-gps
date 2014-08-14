package com.alangeorge.android.bloodhound;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.SyncRequest;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import static com.alangeorge.android.bloodhound.model.provider.LocationContentProvider.AUTHORITY;

public class SyncAdaptorContentObserver extends ContentObserver {
    private static final String TAG = "SyncAdaptorContentObserver";

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public SyncAdaptorContentObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        Log.d(TAG, "onChange(" + selfChange + ")");
        super.onChange(selfChange);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.d(TAG, "onChange(" + selfChange + ", " + uri + ")");
        super.onChange(selfChange, uri);

        if (App.syncAccount != null) {
            Log.d(TAG, "requesting sync");

            SyncRequest.Builder builder = new SyncRequest.Builder();

            builder.setSyncAdapter(App.syncAccount, AUTHORITY)
                    .syncOnce();

//            ContentResolver.requestSync(App.syncAccount, AUTHORITY, new Bundle());
            ContentResolver.requestSync(builder.build());
        } else {
            Log.e(TAG, "App.syncAccount == null, can not request sync");
        }
    }
}
