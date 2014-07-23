package com.alangeorge.android.bloodhound;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.alangeorge.android.bloodhound.model.GeoFence;
import com.alangeorge.android.bloodhound.model.Location;
import com.alangeorge.android.bloodhound.model.ModelException;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
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


public class GeoFenceFragment extends Fragment implements
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraChangeListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "GeoFenceFragment";
    private static final int DEFAULT_RADIUS_PICKER_INDEX = 20;
    private static final int NUMBER_OF_PICKER_ITEMS = 50;

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private NumberPicker radiusInput;

    private Marker selectedMarker;
    private Map<Marker, Circle> markerCircleHashMap = new HashMap<Marker, Circle>();
    private Map<Marker, GeoFence> markerGeoFenceMap = new HashMap<Marker, GeoFence>();

    public GeoFenceFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_geo_fence, container, false);

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_NORMAL)
                .compassEnabled(false)
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false);

        mapFragment = SupportMapFragment.newInstance(options);

        fragmentTransaction.replace(R.id.map, mapFragment);
        fragmentTransaction.commit();

        return view;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView()");
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu()");
        inflater.inflate(R.menu.geo_fence, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu()");
        //noinspection SimplifiableIfStatement
        if (selectedMarker == null && menu.hasVisibleItems()) {
            menu.findItem(R.id.action_discard).setVisible(false);
            menu.findItem(R.id.action_save).setVisible(false);
        } else {
            menu.findItem(R.id.action_save).setVisible(true);
            menu.findItem(R.id.action_discard).setVisible(true);
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

            if (getActivity().getContentResolver().delete(Uri.parse(GEOFENCE_CONTENT_URI + "/" + id), null, null) == 1) {
                Log.d(TAG, "GeoFence deleted, id " + id);
            }

            markerCircleHashMap.remove(selectedMarker);
            markerGeoFenceMap.remove(selectedMarker);

        } else {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.geofence_delete_with_no_marker_selected), Toast.LENGTH_LONG).show();
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

            Uri result = getActivity().getContentResolver().insert(GEOFENCE_CONTENT_URI, values);

            return new GeoFence(result);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.geofence_save_with_no_marker_selected), Toast.LENGTH_LONG).show();
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

        NumberPicker radiusInput = (NumberPicker) getActivity().findViewById(R.id.radiusPicker);

        radiusInput.setDisplayedValues(getPickerValues(NUMBER_OF_PICKER_ITEMS, maxDistance));
        radiusInput.setValue(DEFAULT_RADIUS_PICKER_INDEX);

        radiusInput.setVisibility(View.VISIBLE);
        getActivity().invalidateOptionsMenu();
    }

    private void unSelectMarker() {
        radiusInput.setVisibility(View.GONE);
        selectedMarker = null;
        getActivity().invalidateOptionsMenu();
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
            getActivity().invalidateOptionsMenu();

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

        NumberPicker radiusInput = (NumberPicker) getActivity().findViewById(R.id.radiusPicker);

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
                getActivity(),
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
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.geofence_load_error), Toast.LENGTH_LONG).show();
                Log.e(TAG, getString(R.string.geofence_load_error) + ": " + e.getLocalizedMessage());
                continue;
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

    // start lifecycle logging
    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach()");
        super.onAttach(activity);
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        Log.d(TAG, "onInflate()");
        super.onInflate(activity, attrs, savedInstanceState);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();

        //noinspection ConstantConditions
        assert  getView().findViewById(R.id.radiusPicker) != null;
        radiusInput = (NumberPicker) getView().findViewById(R.id.radiusPicker);
        unSelectMarker();

        Location myLocation = Location.getLastLocation();

        map = mapFragment.getMap();
        Log.d(TAG, "map:" + map);

        if (map != null) {
            map.setOnMapClickListener(this);
            map.setOnMarkerDragListener(this);
            map.setOnMarkerClickListener(this);
            map.setOnCameraChangeListener(this);

            map.setMyLocationEnabled(true);

            if (myLocation != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 5));
                map.animateCamera(CameraUpdateFactory.zoomTo(15), 1000, null);
            }

            // onLoadFinished uses map so only load if map != null
            LoaderManager.enableDebugLogging(true);
            getLoaderManager().initLoader(0, null, this);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_no_google_maps_available), Toast.LENGTH_LONG).show();
        }

        LoaderManager.enableDebugLogging(true);
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onDestroyOptionsMenu() {
        Log.d(TAG, "onDestroyOptionsMenu()");
        super.onDestroyOptionsMenu();
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        Log.d(TAG, "onOptionsMenuClosed()");
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Log.d(TAG, "onCreateContextMenu()");
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        Log.d(TAG, "onViewStateRestored()");
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged()");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        Log.d(TAG, "onLowMemory()");
        super.onLowMemory();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d(TAG, "onContextItemSelected()");
        return super.onContextItemSelected(item);
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach()");
        super.onDetach();
    }
    // end lifecycle logging
}
