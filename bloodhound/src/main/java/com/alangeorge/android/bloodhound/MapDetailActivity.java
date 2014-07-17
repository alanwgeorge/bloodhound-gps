package com.alangeorge.android.bloodhound;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alangeorge.android.bloodhound.model.Location;
import com.alangeorge.android.bloodhound.model.LocationDiff;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * This {@link android.app.Activity} handles all mapping views for the app.
 * <p/>
 * Which is currently:
 * <ul>
 *     <li>Single recorded {@link com.alangeorge.android.bloodhound.model.Location}, {@link com.alangeorge.android.bloodhound.MapDetailActivity#MAP_ACTION_LOCATION}</li>
 *     <li>A location diff {@link com.alangeorge.android.bloodhound.model.LocationDiff}, {@link com.alangeorge.android.bloodhound.MapDetailActivity#MAP_ACTION_LOCATION_DIFF}</li>
 * </ul>
 */
public class MapDetailActivity extends ActionBarActivity {
    private static final String TAG = "MapDetailActivity";

    public static final int MAP_ACTION_LOCATION = 1;
    public static final int MAP_ACTION_LOCATION_DIFF = 2;
    public static final String EXTRA_ACTION = "map_action_extra_action";
    public static final String EXTRA_START = "map_action_extra_start";

    private long currentId;
    private int mapMode;

    /**
     * Selects map behavior based on {@link android.content.Intent#getExtras()} of {@link com.alangeorge.android.bloodhound.MapDetailActivity#EXTRA_ACTION}
     * <p/>
     * Other {@link android.content.Intent#getExtras()}:
     * <p/>
     * For {@link com.alangeorge.android.bloodhound.MapDetailActivity#MAP_ACTION_LOCATION}, recorded locations
     * <ul>
     *     <li>Extra: {@link com.alangeorge.android.bloodhound.MapDetailActivity#EXTRA_START}: the location to display</li>
     * </ul>
     * <p/>
     * For {@link com.alangeorge.android.bloodhound.MapDetailActivity#MAP_ACTION_LOCATION_DIFF}, location changes
     * <ul>
     *     <li>Extra: {@link com.alangeorge.android.bloodhound.MapDetailActivity#EXTRA_START}: the starting location to display</li>
     * </ul>
     *
     * @param savedInstanceState saved state if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        int action;
        long startLocationId;

        action = getIntent().getExtras().getInt(EXTRA_ACTION);
        startLocationId = getIntent().getExtras().getLong(EXTRA_START);

        mapMode = action;

        switch (action) {
            case MAP_ACTION_LOCATION:
                getSupportActionBar().setTitle(R.string.action_bar_title_location);
                currentId = startLocationId;
                showSinglePoint(startLocationId);
                break;
            case MAP_ACTION_LOCATION_DIFF:
                getSupportActionBar().setTitle(R.string.action_bar_title_location_diff);

                Button next = (Button) findViewById(R.id.diff_map_next);
                Button prev = (Button) findViewById(R.id.diff_map_prev);

                next.setVisibility(View.VISIBLE);
                prev.setVisibility(View.VISIBLE);

                currentId = startLocationId;

                showStartEndPoints(startLocationId);
                break;
            default:
                throw new IllegalArgumentException("Unknown Action: " + action);
        }
    }

    /**
     * Maps the Home (android.R.id.home) selection to ending this {@link android.app.Activity}
     *
     * @param item menu item selected
     * @return where the menu selection was handled or not
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Implements {@link com.alangeorge.android.bloodhound.MapDetailActivity#MAP_ACTION_LOCATION} to display a single
     * recorded location.
     *
     * @param startLocationId the point to display
     */
    private void showSinglePoint(long startLocationId) {

        Location location = new Location(startLocationId);

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        assert getSupportFragmentManager().findFragmentById(R.id.map) != null;
        GoogleMap map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        map.addMarker(new MarkerOptions().position(latLng).title(location.getTime().toString()));

        // Move the camera instantly to point with a zoom of 15.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

    /**
     * Implements {@link com.alangeorge.android.bloodhound.MapDetailActivity#MAP_ACTION_LOCATION_DIFF} to display a start and
     * end points of subsequently recorded locations.
     *
     * @param locationDiffId the location diff to display
     * */
    private void showStartEndPoints(long locationDiffId) {

        LocationDiff locationDiff = new LocationDiff(locationDiffId);

        final LatLng startLatLng = new LatLng(locationDiff.getFromLocation().getLatitude(), locationDiff.getFromLocation().getLongitude());
        final LatLng endLatLng = new LatLng(locationDiff.getToLocation().getLatitude(), locationDiff.getToLocation().getLongitude());

        assert getSupportFragmentManager().findFragmentById(R.id.map) != null;
        final GoogleMap map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        map.clear();

        map.addMarker(new MarkerOptions()
                .position(startLatLng)
                .title(locationDiff.getFromLocation().getTime().toString())
                .snippet(String.valueOf(locationDiff.getFromLocation().getId()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        map.addMarker(new MarkerOptions()
                .position(endLatLng)
                .title(locationDiff.getToLocation().getTime().toString())
                .snippet(String.valueOf(locationDiff.getToLocation().getId()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(startLatLng).include(endLatLng);
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 1000, 1000, 300));

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d(TAG, "onCameraChange()");
                // Move camera.
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(startLatLng).include(endLatLng);
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
                // Remove listener to prevent position reset on camera move.
                map.setOnCameraChangeListener(null);
            }
        });
    }

    public void onNextDiffClick(View view) {
        Log.d(TAG, "onNextDiffClick()");

        switch (mapMode) {
            case MAP_ACTION_LOCATION_DIFF:
                LocationDiff currentLocationDiff = new LocationDiff(currentId);
                LocationDiff nextDiff = currentLocationDiff.getNext();

                if (nextDiff == null) {
                    Toast.makeText(getApplicationContext(), "No more Location Diffs", Toast.LENGTH_LONG).show();
                    break;
                }

                currentId = nextDiff.getId();
                showStartEndPoints(nextDiff.getFromLocation().getId());
                break;
            default:
                throw new IllegalArgumentException("Unknown mapMode: " + mapMode);
        }
    }

    public void onPrevDiffClick(View view) {
        Log.d(TAG, "onPrevDiffClick()");

        switch (mapMode) {
            case MAP_ACTION_LOCATION_DIFF:
                LocationDiff currentLocationDiff = new LocationDiff(currentId);
                LocationDiff prevDiff = currentLocationDiff.getPrev();

                if (prevDiff == null) {
                    Toast.makeText(getApplicationContext(), "No more Location Diffs", Toast.LENGTH_LONG).show();
                    break;
                }

                currentId = prevDiff.getId();
                showStartEndPoints(prevDiff.getFromLocation().getId());
                break;
            default:
                throw new IllegalArgumentException("Unknown mapMode: " + mapMode);
        }
    }
}
