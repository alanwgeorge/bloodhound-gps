package com.alangeorge.android.bloodhound;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;

import java.util.Date;

import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LATITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LONGITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME_STRING;
import static com.alangeorge.android.bloodhound.model.provider.LocationContentProvider.LOCATIONS_CONTENT_URI;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class LocationUpdateIntentService extends IntentService {
    private static final String TAG = "LocationUpdateIntentService";

    public LocationUpdateIntentService() {
        super("LocationUpdateIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent(" + intent + ")");
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            Location location = bundle.getParcelable(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
            recordLocation(location);
        }
    }

    private void recordLocation(Location location) {
        Date now = new Date();

        ContentValues values = new ContentValues();
        values.put(LOCATIONS_COLUMN_LATITUDE, location.getLatitude());
        values.put(LOCATIONS_COLUMN_LONGITUDE, location.getLongitude());
        values.put(LOCATIONS_COLUMN_TIME, now.getTime());
        values.put(LOCATIONS_COLUMN_TIME_STRING, now.toString());

        App.context.getContentResolver().insert(LOCATIONS_CONTENT_URI, values);
    }
}
