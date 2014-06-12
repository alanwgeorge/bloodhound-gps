package com.alangeorge.android.bloodhound;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.alangeorge.android.bloodhound.model.dao.LocationDao;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import java.sql.SQLException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * adb -s 10.0.1.28:5555 backup -f data.ab -noapk com.alangeorge.android.bloodhound
 * dd if=data.ab bs=1 skip=24 | python -c "import zlib,sys;sys.stdout.write(zlib.decompress(sys.stdin.read()))" | tar -xvf -
 */
public class BloodHoundService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
    private static final String TAG = "BloodHoundService";

    public static final long UPDATE_INTERVAL = 60000L;
    public static final String BLOODHOUND_SERVICE_ACTION = "com.alangeorge.android.bloodhound.BloodHoundService";

    private static boolean isRunning = false;

    private ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(1, new ServiceThreadFactory());
    private LocationClient locationClient;
    private LocationDao locationDao;

    public BloodHoundService() {    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();
    }

    private void startTimer() {
        if (threadPool == null || threadPool.isShutdown() || threadPool.isTerminated()) {
            Log.d(TAG, "thread pool not active, starting new one");
            threadPool = new ScheduledThreadPoolExecutor(1, new ServiceThreadFactory());
        }

        if (! isRunning) {
            Log.d(TAG, "isRuning false, scheduling timer thread");
            threadPool.scheduleAtFixedRate(new Thread(){ public void run() {onTimerTick();}}, 1000L, UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
            isRunning = true;
        } else {
            Log.d(TAG, "service thread already scheduled");
        }
    }

    protected void onTimerTick() {
        Location location = locationClient.getLastLocation();
        Log.d(TAG, "location = " + location);
        locationDao.create(location.getLatitude(), location.getLongitude());
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind(" + intent + ")");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand(" + intent + ", " + flags + ", " + startId +")");
        locationClient = new LocationClient(this, this, this);
        locationClient.connect();
        locationDao = new LocationDao(this);
        try {
            locationDao.open();
        } catch (SQLException e) {
            Log.e(TAG, "could not open location db: " + e.getLocalizedMessage(), e);
            throw new RuntimeException("could not open location db: " + e.getLocalizedMessage(), e);
        }
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        locationClient.disconnect();
        locationDao.close();
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
}
