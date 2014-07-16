package com.alangeorge.android.bloodhound;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.alangeorge.android.bloodhound.model.GeoFence;
import com.alangeorge.android.bloodhound.model.Location;
import com.alangeorge.android.bloodhound.model.ModelException;
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

import static com.alangeorge.android.bloodhound.model.dao.DBHelper.GEOFENCES_ALL_COLUMNS;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.GEOFENCES_COLUMN_LATITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.GEOFENCES_COLUMN_LONGITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.GEOFENCES_COLUMN_NAME;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.GEOFENCES_COLUMN_RADIUS;
import static com.alangeorge.android.bloodhound.model.provider.LocationContentProvider.GEOFENCE_CONTENT_URI;

public class GeoFenceSelectionActivity extends Activity implements
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraChangeListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "GeoFenceActivity";
    private static final int DEFAULT_RADIUS_PICKER_INDEX = 20;
    private static final int NUMBER_OF_PICKER_ITEMS = 50;

    private GoogleMap map;
    private NumberPicker radiusInput;

    private Marker selectedMarker;
    private Map<Marker, Circle> markerCircleHashMap = new HashMap<Marker, Circle>();
    private Map<Marker, GeoFence> markerGeoFenceMap = new HashMap<Marker, GeoFence>();

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
        radiusInput = (NumberPicker) findViewById(R.id.radiusPicker);
        unSelectMarker();

        map.setOnMapClickListener(this);
        map.setOnMarkerDragListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnCameraChangeListener(this);

        map.setMyLocationEnabled(true);

        LoaderManager.enableDebugLogging(true);
        getLoaderManager().initLoader(0, null, this);

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
    public boolean onPrepareOptionsMenu(Menu menu) {
        //noinspection SimplifiableIfStatement
        if (selectedMarker == null) {
            return false;
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_discard:
                deleteSelectedGeoFence();
                unSelectMarker();
                return true;
            case R.id.action_save:
                // TODO deal with already existing geofence (example, tap existing, then save)
                GeoFence geoFence;
                try {
                    geoFence = persistSelectedMarker();
                    markerGeoFenceMap.put(selectedMarker, geoFence);
                } catch (ModelException e) {
                    e.printStackTrace();
                }
                unSelectMarker();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteSelectedGeoFence() {
        if (selectedMarker != null) {
            GeoFence selectedGeoFence = markerGeoFenceMap.get(selectedMarker);

            long id = selectedGeoFence.getId();

            if (getContentResolver().delete(Uri.parse(GEOFENCE_CONTENT_URI + "/" + id), null, null) == 1) {
                Log.d(TAG, "GeoFence deleted, id " + id);
            }

            markerCircleHashMap.remove(selectedMarker);
            markerGeoFenceMap.remove(selectedMarker);

        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.geofence_delete_with_no_marker_selected), Toast.LENGTH_LONG).show();
            Log.e(TAG, "geofence_delete_with_no_marker_selected");
        }
    }

    private GeoFence persistSelectedMarker() throws ModelException {
        if (selectedMarker != null) {
            ContentValues values = new ContentValues();

            values.put(GEOFENCES_COLUMN_LATITUDE, selectedMarker.getPosition().latitude);
            values.put(GEOFENCES_COLUMN_LONGITUDE, selectedMarker.getPosition().longitude);
            values.put(GEOFENCES_COLUMN_NAME, "GeoFence");
            values.put(GEOFENCES_COLUMN_RADIUS, markerCircleHashMap.get(selectedMarker).getRadius());

            Uri result = getContentResolver().insert(GEOFENCE_CONTENT_URI, values);

            return new GeoFence(result);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.geofence_save_with_no_marker_selected), Toast.LENGTH_LONG).show();
            Log.e(TAG, "geofence_save_with_no_marker_selected");
            return null;
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

    private void selectMarker(Marker marker) {
        selectedMarker = marker;
        float maxDistance = calculateMaxDistance(map.getProjection().getVisibleRegion());

        NumberPicker radiusInput = (NumberPicker) findViewById(R.id.radiusPicker);

        radiusInput.setDisplayedValues(getPickerValues(NUMBER_OF_PICKER_ITEMS, maxDistance));
        radiusInput.setValue(DEFAULT_RADIUS_PICKER_INDEX);

        radiusInput.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
    }

    private void unSelectMarker() {
        radiusInput.setVisibility(View.GONE);
        selectedMarker = null;
        invalidateOptionsMenu();
    }

    // start GoogleMap.OnMapClickListener
    /**
     * If a marker is selected, clicking on the map will deselect it.
     * If no marker is selected, clicking on the map will create a marker and radius on that location
     *
     * @param latLng coordinates of the click on the map
     */
    @Override
    public void onMapClick(LatLng latLng) {
        // if you click on the map and a marker is selected, un-select the current marker
        // and
        if (selectedMarker != null) {
            unSelectMarker();
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
                    .strokeWidth(0));

            markerCircleHashMap.put(marker, radius);
            selectMarker(marker);
            invalidateOptionsMenu();

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
        markerCircleHashMap.get(marker).setCenter(marker.getPosition());
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        markerCircleHashMap.get(marker).setCenter(marker.getPosition());
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
        selectMarker(marker);
        return true;
    }
    // end GoogleMap.OnMarkerClickListener

    // start LoaderManager.LoaderCallbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader()");
        return new CursorLoader(
                this,
                GEOFENCE_CONTENT_URI,
                GEOFENCES_ALL_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished()");

        map.clear();
        markerCircleHashMap = new HashMap<Marker, Circle>();

        data.moveToFirst();

        while (! data.isAfterLast()) {
            GeoFence geoFence = null;
            try {
                geoFence = new GeoFence(data);
            } catch (ModelException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.geofence_load_error), Toast.LENGTH_LONG).show();
                Log.e(TAG, getString(R.string.geofence_load_error) + ": " + e.getLocalizedMessage());
            }

            LatLng latLng = new LatLng(data.getFloat(2), data.getFloat(3));

            Marker marker = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));


            Circle radius = map.addCircle(new CircleOptions()
                    .center(marker.getPosition())
                    .radius(data.getFloat(4))
                    .strokeColor(R.color.DimGray)
                    .fillColor(R.color.DimGray));

            markerCircleHashMap.put(marker, radius);
            markerGeoFenceMap.put(marker, geoFence);

            data.moveToNext();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoadReset()");
    }
    // start LoaderManager.LoaderCallbacks
}
