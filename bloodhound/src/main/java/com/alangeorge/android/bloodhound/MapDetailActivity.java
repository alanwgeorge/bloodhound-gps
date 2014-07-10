package com.alangeorge.android.bloodhound;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.alangeorge.android.bloodhound.model.Location;
import com.alangeorge.android.bloodhound.model.LocationDiff;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.HashMap;
import java.util.Map;

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
    @SuppressLint("AppCompatMethod")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        int action;
        long startLocationId;

        action = getIntent().getExtras().getInt(EXTRA_ACTION);
        startLocationId = getIntent().getExtras().getLong(EXTRA_START);

        mapMode = action;

        switch (action) {
            case MAP_ACTION_LOCATION:
                getActionBar().setTitle(R.string.action_bar_title_location);
                currentId = startLocationId;
                showSinglePoint(startLocationId);
                break;
            case MAP_ACTION_LOCATION_DIFF:
                getActionBar().setTitle(R.string.action_bar_title_location_diff);

                Button next = (Button) findViewById(R.id.diff_map_next);
                Button prev = (Button) findViewById(R.id.diff_map_prev);

                next.setVisibility(View.VISIBLE);
                prev.setVisibility(View.VISIBLE);

                currentId = startLocationId;

                showStartEndPoints(startLocationId);
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

        Location location = new Location(startLocationId);

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

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
     * @param locationDiffId the location diff to display
     * */
    private void showStartEndPoints(long locationDiffId) {

        LocationDiff locationDiff = new LocationDiff(locationDiffId);

        final LatLng startLatLng = new LatLng(locationDiff.getFromLocation().getLatitude(), locationDiff.getFromLocation().getLongitude());
        final LatLng endLatLng = new LatLng(locationDiff.getToLocation().getLatitude(), locationDiff.getToLocation().getLongitude());

        assert getFragmentManager().findFragmentById(R.id.map) != null;
        final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

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

    private void startGeoFenceSelection() {
        assert getFragmentManager().findFragmentById(R.id.map) != null;
        final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        final int defaultRadiusPickerIndex = 20;
        final int numberOfPickerItems = 50;
        final Map<Marker, Circle> fences = new HashMap<Marker, Circle>();

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return false;
            }
        });

        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {
                fences.get(marker).setCenter(marker.getPosition());
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                fences.get(marker).setCenter(marker.getPosition());
            }
        });

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d(TAG, "onCameraChange(): zoom:" + cameraPosition.zoom);
                float maxDistance = calculateMaxDistance(map.getProjection().getVisibleRegion());
                Log.d(TAG, "onCameraChange(): screen distance top left to bottom right:" + maxDistance);

                NumberPicker radiusInput = (NumberPicker) findViewById(R.id.radiusPicker);

                if (radiusInput.getVisibility() == View.VISIBLE) {
                    radiusInput.setDisplayedValues(getPickerValues(numberOfPickerItems, maxDistance));
                    radiusInput.setValue(defaultRadiusPickerIndex);
                }
            }
        });

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                NumberPicker radiusInput = (NumberPicker) findViewById(R.id.radiusPicker);

                if (radiusInput.getVisibility() == View.VISIBLE) {
                    radiusInput.setVisibility(View.GONE);
                } else {

                    float maxDistanceMeters = calculateMaxDistance(map.getProjection().getVisibleRegion());

                    final String[] pickerLabels = getPickerValues(numberOfPickerItems, maxDistanceMeters);

                    Marker marker = map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .draggable(true)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));



                    final Circle radius = map.addCircle(new CircleOptions()
                                    .center(marker.getPosition())
                                    .radius(Double.valueOf(pickerLabels[defaultRadiusPickerIndex]))
                                    .strokeColor(R.color.DimGray)
                                    .fillColor(R.color.DimGray)
                    );

                    fences.put(marker, radius);

                    radiusInput.setMaxValue(pickerLabels.length - 1);
                    radiusInput.setMinValue(0);
                    radiusInput.setDisplayedValues(pickerLabels);
                    radiusInput.setValue(defaultRadiusPickerIndex);
                    radiusInput.setVisibility(View.VISIBLE);

                    radiusInput.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                        @Override
                        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                            radius.setRadius(Double.valueOf(picker.getDisplayedValues()[newVal]));
                        }
                    });
                }
            }
        });

        map.setMyLocationEnabled(true);

        Location myLocation = Location.getLastLocation();

        if (myLocation != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 5));
            map.animateCamera(CameraUpdateFactory.zoomTo(15), 1000, null);
        }
    }

    private String[] getPickerValues(int numberOfPickerItems, float maxDistance) {
        int maxRadiusMeters = Math.round(maxDistance / 2);
        int radiusIncrement = maxRadiusMeters / numberOfPickerItems;

        final String[] pickerLabels = new String[numberOfPickerItems];
        pickerLabels[0] = Integer.toString(radiusIncrement);
        for (int i = 1; i < pickerLabels.length; i++) {
            pickerLabels[i] = Integer.toString((i + 1) * radiusIncrement);

        }
        return pickerLabels;
    }

    private float calculateMaxDistance(VisibleRegion visibleRegion) {
        android.location.Location farLeft = new android.location.Location("internal");
        android.location.Location nearRight = new android.location.Location("internal");
        farLeft.setLatitude(visibleRegion.farLeft.latitude);
        farLeft.setLongitude(visibleRegion.farLeft.longitude);
        nearRight.setLatitude(visibleRegion.nearRight.latitude);
        nearRight.setLongitude(visibleRegion.nearRight.longitude);

        float distance = farLeft.distanceTo(nearRight);

        Log.d(TAG, "calculateMaxDistance(): screen distance top left to bottom right:" + distance);
        return distance;
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
