package com.alangeorge.android.bloodhound;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.alangeorge.android.bloodhound.model.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
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
 *
 * In the future will support registering a GeoFence {@link com.alangeorge.android.bloodhound.MapDetailActivity#MAP_ACTION_GEOFENCE_SELECT}
 */
public class MapDetailActivity extends Activity {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = "MapDetailActivity";

    public static final int MAP_ACTION_LOCATION = 1;
    public static final int MAP_ACTION_LOCATION_DIFF = 2;
    public static final int MAP_ACTION_GEOFENCE_SELECT = 3;
    public static final String EXTRA_ACTION = "map_action_extra_action";
    public static final String EXTRA_START = "map_action_extra_start";
    public static final String EXTRA_END = "map_action_extra_end";

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
     *     <li>Extra: {@link com.alangeorge.android.bloodhound.MapDetailActivity#EXTRA_END}: the ending location to display</li>
     * </ul>
     *
     * @param savedInstanceState saved state if any
     */
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
                getActionBar().setTitle(R.string.action_bar_title_location);
                showSinglePoint(startLocationId);
                break;
            case MAP_ACTION_LOCATION_DIFF:
                getActionBar().setTitle(R.string.action_bar_title_location_diff);
                endLocationId = getIntent().getExtras().getLong(EXTRA_END);
                showStartEndPoints(startLocationId, endLocationId);
                break;
            case MAP_ACTION_GEOFENCE_SELECT:
                getActionBar().setTitle(R.string.action_bar_title_geofence_selection);
                startGeoFenceSelection();
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
        Uri locationUri = Uri.parse(LocationContentProvider.LOCATIONS_CONTENT_URI + "/" + startLocationId);

        Location location = Location.resolveLocation(locationUri);

        LatLng latLng = new LatLng(location.getLongitude(), location.getLatitude());

        assert getFragmentManager().findFragmentById(R.id.map) != null;
        GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

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
     * @param startLocationId the starting point to display
     * @param endLocationId the end point to display
     * */

    private void showStartEndPoints(long startLocationId, long endLocationId) {
        Uri startLocationUri = Uri.parse(LocationContentProvider.LOCATIONS_CONTENT_URI + "/" + startLocationId);
        Uri endLocationUri = Uri.parse(LocationContentProvider.LOCATIONS_CONTENT_URI + "/" + endLocationId);

        Location startLocation = Location.resolveLocation(startLocationUri);
        Location endLocation = Location.resolveLocation(endLocationUri);

        final LatLng startLatLng = new LatLng(startLocation.getLongitude(), startLocation.getLatitude());
        final LatLng endLatLng = new LatLng(endLocation.getLongitude(), endLocation.getLatitude());

        assert getFragmentManager().findFragmentById(R.id.map) != null;
        final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        map.addMarker(new MarkerOptions()
                .position(startLatLng)
                .title(startLocation.getTime().toString())
                .snippet("Start")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        map.addMarker(new MarkerOptions()
                .position(endLatLng)
                .title(endLocation.getTime().toString())
                .snippet("End")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // here we set the map to be bounded by our start and end points, we first must
        // let the map layout and then set the bounds, so we do it in the change listener
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition arg0) {
                // Move camera.
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(startLatLng).include(endLatLng);
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 350));
                // Remove listener to prevent position reset on camera move.
                map.setOnCameraChangeListener(null);
            }
        });
    }

    private void startGeoFenceSelection() {
        assert getFragmentManager().findFragmentById(R.id.map) != null;
        GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(getApplicationContext(), latLng.toString(), Toast.LENGTH_LONG).show();
            }
        });

        map.setMyLocationEnabled(true);

        android.location.Location myLocation = map.getMyLocation();

        LatLng myLatLng;

        if (myLocation != null) {
            myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
        }
    }
}
