package com.alangeorge.android.bloodhound;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.alangeorge.android.bloodhound.model.GeoFence;
import com.alangeorge.android.bloodhound.model.ModelException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.LocationClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.alangeorge.android.bloodhound.model.dao.DBHelper.GEOFENCES_ALL_COLUMNS;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LATITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LONGITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME_STRING;
import static com.alangeorge.android.bloodhound.model.provider.LocationContentProvider.GEOFENCE_CONTENT_URI;
import static com.alangeorge.android.bloodhound.model.provider.LocationContentProvider.LOCATIONS_CONTENT_URI;

/**
 * This service is responsible for recording the devices location at configurable intervals and minimum
 * location change.  Currently uses {@link android.location.LocationListener}
 */
@SuppressWarnings("WeakerAccess")
public class BloodHoundService extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClient.OnAddGeofencesResultListener,
        LocationClient.OnRemoveGeofencesResultListener,
        LocationListener {

    private static final String TAG = "BloodHoundService";

    private static final long UPDATE_INTERVAL = 60000L;
    private static final float MIN_CHANGE_TO_REPORT = 0.0f;

    private static boolean isRunning = false;
    private static Date lastUpdate;

    private ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(1, new ServiceThreadFactory());
    private Intent geoFenceTransitionIntent;
    private PendingIntent geoFenceTransitionPendingIntent;
    private LocationClient locationClient;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(this); // ensure we only register one listener
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_INTERVAL, MIN_CHANGE_TO_REPORT, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, MIN_CHANGE_TO_REPORT, this);

        geoFenceTransitionIntent  = new Intent(this, GeoFenceTransitionIntentService.class);

        locationClient = new LocationClient(this, this, this);
        locationClient.connect();
    }

    private void startTimer() {
        if (threadPool == null || threadPool.isShutdown() || threadPool.isTerminated()) {
            Log.d(TAG, "thread pool not active, starting new one");
            threadPool = new ScheduledThreadPoolExecutor(1, new ServiceThreadFactory());
        }

        if (! isRunning) {
            Log.d(TAG, "isRunning false, scheduling timer thread");
            threadPool.scheduleAtFixedRate(new Thread(){ public void run() {onTimerTick();}}, 1000L, UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
            isRunning = true;
        } else {
            Log.d(TAG, "service thread already scheduled");
        }
    }

    private void onTimerTick() {
        Location location = locationClient.getLastLocation();
        Log.d(TAG, "tick LocationClient.getLastLocation() " + location);
        addLocation(location);
    }

    private void addLocation(Location location) {
        Date now = new Date();

        ContentValues values = new ContentValues();
        values.put(LOCATIONS_COLUMN_LATITUDE, location.getLatitude());
        values.put(LOCATIONS_COLUMN_LONGITUDE, location.getLongitude());
        values.put(LOCATIONS_COLUMN_TIME, now.getTime());
        values.put(LOCATIONS_COLUMN_TIME_STRING, now.toString());

        App.context.getContentResolver().insert(LOCATIONS_CONTENT_URI, values);
    }

    private void addGeoFenceListeners() {
        Cursor geoFenceCursor = App.context.getContentResolver().query(GEOFENCE_CONTENT_URI, GEOFENCES_ALL_COLUMNS, null, null, null);

        ArrayList<Geofence> geofences = new ArrayList<Geofence>();

        geoFenceCursor.moveToFirst();
        while (! geoFenceCursor.isAfterLast()) {
            GeoFence geoFence;
            try {
                geoFence = new GeoFence(geoFenceCursor);
            } catch (ModelException e) {
                Log.e(TAG, getString(R.string.geofence_load_error) + ": " + e.getLocalizedMessage());                e.printStackTrace();
                continue;
            }

            Geofence googleGeofence = new Geofence.Builder()
                    .setCircularRegion(
                            geoFence.getLatitude(),
                            geoFence.getLongitude(),
                            geoFence.getRadius()
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setRequestId(String.valueOf(geoFence.getId()))
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            Log.d(TAG, "created google geofence; " + googleGeofence);

            geofences.add(googleGeofence);
            geoFenceCursor.moveToNext();
        }

        geoFenceCursor.close();

        if (! geofences.isEmpty()) {
            locationClient.addGeofences(geofences, createGeoFencePendingIntent(), this);
        }
    }

    private PendingIntent createGeoFencePendingIntent() {
        // If the PendingIntent already exists
        if (null != geoFenceTransitionPendingIntent) {
            return geoFenceTransitionPendingIntent;
        } else {
            return PendingIntent.getService(
                    this,
                    0,
                    geoFenceTransitionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind(" + intent + ")");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand(" + intent + ", " + flags + ", " + startId +")");
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged(" + newConfig + ")");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        Log.d(TAG, "onLowMemory()");
        super.onLowMemory();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTrimMemory(int level) {
        Log.d(TAG, "onTaskRemoved(" + level + ")");
        super.onTrimMemory(level);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind(" + intent + ")");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind(" + intent + ")");
        super.onRebind(intent);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved(" + rootIntent + ")");
        super.onTaskRemoved(rootIntent);
    }

    // start GooglePlayServicesClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Google Play Services onConnect()");

        addGeoFenceListeners();
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "Google Play Services onDisconnect()");
    }
    // end GooglePlayServicesClient.ConnectionCallbacks

    // start GooglePlayServicesClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Google Play Services onConnectionFailed(" + connectionResult + ")");
    }
    // end GooglePlayServicesClient.OnConnectionFailedListener

    // start LocationClient.OnAddGeofencesResultListener
    @Override
    public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
        Log.d(TAG, "onAddGeofencesResult(" + statusCode +  "," + Arrays.toString(geofenceRequestIds) +")");

        if (statusCode != GeofenceStatusCodes.SUCCESS) {
            Toast.makeText(App.context, "failed to add GeoFences: " + statusCode, Toast.LENGTH_LONG).show();

        }
    }
    // end LocationClient.OnAddGeofencesResultListener

    // start LocationClient.OnRemoveGeofencesResultListener
    @Override
    public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
        Log.d(TAG, "onRemoveGeofencesByPendingIdsResult()");
    }

    @Override
    public void onRemoveGeofencesByPendingIntentResult(int i, PendingIntent pendingIntent) {
        Log.d(TAG, "onRemoveGeofencesByPendingIntentResult()");
    }
    // end LocationClient.OnRemoveGeofencesResultListener

    // start LocationListener
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged(" + location + ")");
//        addLocation(location);
        lastUpdate = new Date();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged(" + provider + ", " + status + ", " + extras + ")");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderChanged(" + provider + ")");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled(" + provider + ")");
    }
    // end LocationListener
}
