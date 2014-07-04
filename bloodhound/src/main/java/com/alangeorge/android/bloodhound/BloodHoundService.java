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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;

import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LATITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LONGITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME_STRING;

/**
 * Below are example commands to access the database of a device with BloodHound installed
 *
 * $ adb -s 10.0.1.28:5555 backup -f data.ab -noapk com.alangeorge.android.bloodhound
 * $ dd if=data.ab bs=1 skip=24 | python -c "import zlib,sys;sys.stdout.write(zlib.decompress(sys.stdin.read()))" | tar -xvf -
 * $ sqlite3 apps/com.alangeorge.android.bloodhound/db/locations.db
 * sqlite> select * from locations;
 */
@SuppressWarnings("WeakerAccess")
public class BloodHoundService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
    private static final String TAG = "BloodHoundService";

    private static final long UPDATE_INTERVAL = 60000L;
    @SuppressWarnings("UnusedDeclaration")
    public static final String BLOODHOUND_SERVICE_ACTION = "com.alangeorge.android.bloodhound.BloodHoundService";

    private static boolean isRunning = false;

    private ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(1, new ServiceThreadFactory());
//    private LocationClient locationClient;

    private static final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged(" + location + ")");
            addLocation(location);
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
        locationManager.removeUpdates(locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_INTERVAL, 0, locationListener);
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
//        Location location = locationClient.getLastLocation();
//        Log.d(TAG, "location = " + location);
//
//        addLocation(location);
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
//        locationClient = new LocationClient(this, this, this);
//        locationClient.connect();
//
//        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
//        locationClient.disconnect();
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

    // start Google Play connection callbacks
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Google Play Services onConnect()");
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "Google Play Services onDisconnect()");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Google Play Services onConnectionFailed(" +connectionResult  + ")");
    }
    // end Google Play connection callbacks
}
