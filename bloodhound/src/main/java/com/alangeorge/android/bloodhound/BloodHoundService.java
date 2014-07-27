package com.alangeorge.android.bloodhound;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.alangeorge.android.bloodhound.model.GeoFence;
import com.alangeorge.android.bloodhound.model.ModelException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.alangeorge.android.bloodhound.model.dao.DBHelper.GEOFENCES_ALL_COLUMNS;
import static com.alangeorge.android.bloodhound.model.provider.LocationContentProvider.GEOFENCE_CONTENT_URI;
import static com.google.android.gms.location.LocationRequest.PRIORITY_LOW_POWER;

/**
 * This service is responsible for recording the devices location at configurable intervals and minimum
 * location change.  Currently uses {@link android.location.LocationListener}
 */
@SuppressWarnings("WeakerAccess")
public class BloodHoundService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG = "BloodHoundService";

    private static final long UPDATE_INTERVAL = 60000L;
    private static final float MIN_CHANGE_TO_REPORT = 0.0f;

    private static boolean isRunning = false;

    private ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(1, new ServiceThreadFactory());
    private Intent geoFenceTransitionIntent;
    private PendingIntent geoFenceTransitionPendingIntent;
    private GoogleApiClient googleApiClient;
    private Intent locationUpdateIntent;
    private PendingIntent locationUpdatePendingIntent;
    private LocationRequest locationRequest;


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();

        geoFenceTransitionIntent  = new Intent(this, GeoFenceTransitionIntentService.class);
        locationUpdateIntent  = new Intent(this, LocationUpdateIntentService.class);

        locationRequest = LocationRequest.create()
                .setPriority(PRIORITY_LOW_POWER)
                .setInterval(UPDATE_INTERVAL)
                .setSmallestDisplacement(MIN_CHANGE_TO_REPORT)
                .setFastestInterval(UPDATE_INTERVAL);


        googleApiClient = new GoogleApiClient.Builder(App.context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();
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
        Log.d(TAG, "tick");
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
            PendingResult<Status> addGeofencesResult = LocationServices.GeofencingApi.addGeofences(googleApiClient, geofences, createGeoFencePendingIntent());
            addGeofencesResult.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.d(TAG, "geofence monitoring added successfully");
                    } else {
                        Toast.makeText(App.context, "Unable to monitor GeoFences: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        PendingResult<Status> locationUpdateRequestResult = LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, createLocationUpdatePendingIntent());
        locationUpdateRequestResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d(TAG, "location updates requested successfully");
                } else {
                    Toast.makeText(App.context, "Unable to get location updates: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        googleApiClient.disconnect();
    }

    private PendingIntent createGeoFencePendingIntent() {
        // If the PendingIntent already exists
        if (null != geoFenceTransitionPendingIntent) {
            return geoFenceTransitionPendingIntent;
        } else {
            geoFenceTransitionPendingIntent = PendingIntent.getService(
                    this,
                    0,
                    geoFenceTransitionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            return geoFenceTransitionPendingIntent;
        }
    }

    private PendingIntent createLocationUpdatePendingIntent() {
        // If the PendingIntent already exists
        if (null != locationUpdatePendingIntent) {
            return locationUpdatePendingIntent;
        } else {
            locationUpdatePendingIntent = PendingIntent.getService(
                    this,
                    0,
                    locationUpdateIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            return locationUpdatePendingIntent;
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

    // start GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Google Play Services onConnect()");

        addGeoFenceListeners();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google Play Services onConnectionSuspended()");
    }
    // end GoogleApiClient.ConnectionCallbacks

    // start GooglePlayServicesClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Google Play Services onConnectionFailed(" + connectionResult + ")");
    }
    // end GooglePlayServicesClient.OnConnectionFailedListener
}
