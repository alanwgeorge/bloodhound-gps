package com.alangeorge.android.bloodhound;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.alangeorge.android.bloodhound.model.Location;
import com.alangeorge.android.bloodhound.model.dao.LocationDao;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Uri locationUri = getIntent().getExtras().getParcelable(LocationContentProvider.CONTENT_ITEM_TYPE);

        Location location = resolveLocation(locationUri);

        LatLng latLng = new LatLng(location.getLongitude(), location.getLatitude());

        GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        map.addMarker(new MarkerOptions().position(latLng).title("Was Here"));

        // Move the camera instantly to hamburg with a zoom of 15.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

    private Location resolveLocation(Uri locationUri) {
        String[] projection = {
                LocationDao.COLUMN_ID,
                LocationDao.COLUMN_LONGITUDE,
                LocationDao.COLUMN_LATITUDE,
                LocationDao.COLUMN_TIME_STRING,
                LocationDao.COLUMN_TIME
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
