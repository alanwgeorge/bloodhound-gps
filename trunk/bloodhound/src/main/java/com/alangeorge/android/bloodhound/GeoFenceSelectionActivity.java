package com.alangeorge.android.bloodhound;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;

import com.alangeorge.android.bloodhound.model.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.HashMap;
import java.util.Map;

public class GeoFenceSelectionActivity extends Activity implements
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraChangeListener {

    private static final String TAG = "GeoFenceActivity";
    private static final int DEFAULT_RADIUS_PICKER_INDEX = 20;
    private static final int NUMBER_OF_PICKER_ITEMS = 50;

    private Map<Marker, Circle> fences = new HashMap<Marker, Circle>();
    private GoogleMap map;

    @SuppressLint("AppCompatMethod")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fence);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(R.string.action_bar_title_geofence_selection);
        }

        assert getFragmentManager().findFragmentById(R.id.map) != null;
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        map.setOnMapClickListener(this);
        map.setOnMarkerDragListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnCameraChangeListener(this);

        map.setMyLocationEnabled(true);

        Location myLocation = Location.getLastLocation();

        if (myLocation != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 5));
            map.animateCamera(CameraUpdateFactory.zoomTo(15), 1000, null);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.geo_fence, menu);
        return true;
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

    // start GoogleMap.OnMapClickListener
    @Override
    public void onMapClick(LatLng latLng) {
        NumberPicker radiusInput = (NumberPicker) findViewById(R.id.radiusPicker);

        if (radiusInput.getVisibility() == View.VISIBLE) {
            radiusInput.setVisibility(View.GONE);
        } else {

            float maxDistanceMeters = calculateMaxDistance(map.getProjection().getVisibleRegion());

            final String[] pickerLabels = getPickerValues(NUMBER_OF_PICKER_ITEMS, maxDistanceMeters);

            Marker marker = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));


            final Circle radius = map.addCircle(new CircleOptions()
                            .center(marker.getPosition())
                            .radius(Double.valueOf(pickerLabels[DEFAULT_RADIUS_PICKER_INDEX]))
                            .strokeColor(R.color.DimGray)
                            .fillColor(R.color.DimGray)
            );

            fences.put(marker, radius);

            radiusInput.setMaxValue(pickerLabels.length - 1);
            radiusInput.setMinValue(0);
            radiusInput.setDisplayedValues(pickerLabels);
            radiusInput.setValue(DEFAULT_RADIUS_PICKER_INDEX);
            radiusInput.setVisibility(View.VISIBLE);

            radiusInput.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    radius.setRadius(Double.valueOf(picker.getDisplayedValues()[newVal]));
                }
            });

        }
    }
    // end GoogleMap.OnMapClickListener

    // start GoogleMap.OnMarkerDragListener
    @Override
    public void onMarkerDragStart(Marker marker) {}

    @Override
    public void onMarkerDrag(Marker marker) {
        fences.get(marker).setCenter(marker.getPosition());
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        fences.get(marker).setCenter(marker.getPosition());
    }
    // end GoogleMap.OnMarkerDragListener

    // start GoogleMap.OnCameraChangeListener
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.d(TAG, "onCameraChange(): zoom:" + cameraPosition.zoom);
        float maxDistance = calculateMaxDistance(map.getProjection().getVisibleRegion());
        Log.d(TAG, "onCameraChange(): screen distance top left to bottom right:" + maxDistance);

        NumberPicker radiusInput = (NumberPicker) findViewById(R.id.radiusPicker);

        if (radiusInput.getVisibility() == View.VISIBLE) {
            radiusInput.setDisplayedValues(getPickerValues(NUMBER_OF_PICKER_ITEMS, maxDistance));
            radiusInput.setValue(DEFAULT_RADIUS_PICKER_INDEX);
        }
    }
    // end GoogleMap.OnCameraChangeListener

    // start GoogleMap.OnMarkerClickListener
    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
    // end GoogleMap.OnMarkerClickListener
}
