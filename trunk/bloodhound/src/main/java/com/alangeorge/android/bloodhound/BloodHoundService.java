package com.alangeorge.android.bloodhound;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LATITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LONGITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME_STRING;

/**
 * This service is responsible for recording the devices location at configurable intervals and minimum
 * location change.  Currently uses {@link android.location.LocationListener}
 */
@SuppressWarnings("WeakerAccess")
public class BloodHoundService extends Service {
    private static final String TAG = "BloodHoundService";

    private static final long UPDATE_INTERVAL = 60000L;
    private static final float MIN_CHANGE_TO_REPORT = 0.0f;

    @SuppressWarnings("UnusedDeclaration")
    public static final String BLOODHOUND_SERVICE_ACTION = "com.alangeorge.android.bloodhound.BloodHoundService";

    private static boolean isRunning = false;
    private static Date lastUpdate;

    private ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(1, new ServiceThreadFactory());

    private static final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged(" + location + ")");
            addLocation(location);
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
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener); // ensure we only register one listener
        // TODO use locationManager.getBestProvider() versus just network provider
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_INTERVAL, MIN_CHANGE_TO_REPORT, locationListener);
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
        // historically we fetched the current location here with Google Play Services, but now we are using a LocationListener that calls back when location changes
        // now we are logging the age of the last update to track that we are getting timely location updates.
        // perhaps we could alert or re-register our LocationListener if the last update reaches a certain age.
        Log.d(TAG, "lastUpdate: " + lastUpdate.toString());
        Log.d(TAG, "now: " + new Date().toString());
    }

    private static void addLocation(Location location) {
        Date now = new Date();

        ContentValues values = new ContentValues();
        values.put(LOCATIONS_COLUMN_LATITUDE, location.getLatitude());
        values.put(LOCATIONS_COLUMN_LONGITUDE, location.getLongitude());
        values.put(LOCATIONS_COLUMN_TIME, now.getTime());
        values.put(LOCATIONS_COLUMN_TIME_STRING, now.toString());

        App.context.getContentResolver().insert(LocationContentProvider.LOCATIONS_CONTENT_URI, values);
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

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved(" + rootIntent + ")");
        super.onTaskRemoved(rootIntent);
    }
}
