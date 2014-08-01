package com.alangeorge.android.bloodhound;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.alangeorge.android.bloodhound.BloodHoundReceiver.BLOODHOUND_RECEIVER_ACTION;

public class TabsMainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {
    private static final String TAG = "TabsMainActivity";

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "673992645420";

    private GoogleCloudMessaging gcm;
    private String gcmRegistrationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tabs_main);

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            gcmRegistrationId = App.getGcmRegistrationId();

            if (gcmRegistrationId.isEmpty()) {
                registerInBackground();
            }
        }

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(
                        actionBar.getThemedContext(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[]{
                                getString(R.string.title_section1),
                                getString(R.string.title_section2),
                                getString(R.string.title_section3)
                        }
                ),
                this
        );

        // start location recording
        sendBroadcast(new Intent(BLOODHOUND_RECEIVER_ACTION));
    }

    @Override
    public void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (!savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            return;
        }

        getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        // Serialize the current dropdown position.

        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getSupportActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
//            case R.id.action_geofence_select:
//                Intent detailIntent = new Intent(this, GeoFenceSelectionActivity.class);
//                startActivity(detailIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        Fragment destination;

        switch (itemPosition) {
            case 0:
                destination = new LocationsFragment();
                break;
            case 1:
                destination = new LocationDiffFragment();
                break;
            case 2:
                destination = new GeoFenceFragment();
                break;
            default:
                throw new IllegalArgumentException("onNavigationItemSelected(" + itemPosition + "): invalid selection");
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.container, destination).commit();

        return true;
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    gcmRegistrationId = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + gcmRegistrationId;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
//                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    App.storeRegistrationId(gcmRegistrationId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                super.onPostExecute(msg);
                Log.d(TAG, msg);
            }
        }.execute(null, null, null);
    }


    // logging lifecycle methods
    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onPostResume() {
        Log.d(TAG, "onPostResume()");
        super.onPostResume();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart()");
        super.onRestart();
    }

}
