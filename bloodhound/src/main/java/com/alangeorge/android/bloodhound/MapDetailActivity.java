package com.alangeorge.android.bloodhound;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.alangeorge.android.bloodhound.model.Location;
import com.alangeorge.android.bloodhound.model.dao.LocationDao;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_ID;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LATITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LONGITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME_STRING;

public class MapDetailActivity extends Activity {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = "MapDetailActivity";

    public static final int MAP_ACTION_LOCATION = 1;
    public static final int MAP_ACTION_LOCATION_DIFF = 2;
    public static final String EXTRA_ACTION = "map_action_extra_action";
    public static final String EXTRA_START = "map_action_extra_start";
    public static final String EXTRA_END = "map_action_extra_end";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        int action;
        long startLocationId;
        long endLocationId;

        action = getIntent().getExtras().getInt(EXTRA_ACTION);
        startLocationId = getIntent().getExtras().getLong(EXTRA_START);

        switch (action) {
            case MAP_ACTION_LOCATION:
                showSinglePoint(startLocationId);
                break;
            case MAP_ACTION_LOCATION_DIFF:
                endLocationId = getIntent().getExtras().getLong(EXTRA_END);
                showStartEndPoints(startLocationId, endLocationId);
                break;
            default:
                throw new IllegalArgumentException("Unknown Action: " + action);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSinglePoint(long startLocationId) {
        Uri locationUri = Uri.parse(LocationContentProvider.LOCATIONS_CONTENT_URI + "/" + startLocationId);

        Location location = resolveLocation(locationUri);

        LatLng latLng = new LatLng(location.getLongitude(), location.getLatitude());

        GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        map.addMarker(new MarkerOptions().position(latLng).title(location.getTime().toString()));

        // Move the camera instantly to point with a zoom of 15.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

    private void showStartEndPoints(long startLocationId, long endLocationId) {
        Uri startLocationUri = Uri.parse(LocationContentProvider.LOCATIONS_CONTENT_URI + "/" + startLocationId);
        Uri endLocationUri = Uri.parse(LocationContentProvider.LOCATIONS_CONTENT_URI + "/" + endLocationId);

        Location startLocation = resolveLocation(startLocationUri);
        Location endLocation = resolveLocation(endLocationUri);

        LatLng startLatLng = new LatLng(startLocation.getLongitude(), startLocation.getLatitude());
        LatLng endLatLng = new LatLng(endLocation.getLongitude(), endLocation.getLatitude());

        GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        map.addMarker(new MarkerOptions().position(startLatLng).title(startLocation.getTime().toString()));
        map.addMarker(new MarkerOptions().position(endLatLng).title(endLocation.getTime().toString()));

        // Move the camera instantly to point with a zoom of 15.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 15));

        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

    private Location resolveLocation(Uri locationUri) {
        String[] projection = {
                LOCATIONS_COLUMN_ID,
                LOCATIONS_COLUMN_LONGITUDE,
                LOCATIONS_COLUMN_LATITUDE,
                LOCATIONS_COLUMN_TIME,
                LOCATIONS_COLUMN_TIME_STRING
        };

        Cursor cursor = getContentResolver().query(locationUri, projection, null, null, null);

        Location location = null;
        if (cursor != null) {
            cursor.moveToFirst();
            location = LocationDao.cursorToLocation(cursor);
        }

        return location;
    }

}
