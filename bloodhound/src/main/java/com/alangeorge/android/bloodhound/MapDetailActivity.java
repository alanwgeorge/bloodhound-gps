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

public class MapDetailActivity extends Activity {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = "MapDetailActivity";

    public static final int MAP_ACTION_LOCATION = 1;
    public static final int MAP_ACTION_LOCATION_DIFF = 2;
    public static final int MAP_ACTION_GEOFENCE_SELECT = 3;
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
