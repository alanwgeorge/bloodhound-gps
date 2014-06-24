package com.alangeorge.android.bloodhound.model.dao;

import android.content.Context;
import android.database.Cursor;

import com.alangeorge.android.bloodhound.model.Location;
import com.alangeorge.android.bloodhound.model.LocationDiff;

import java.util.Date;

@SuppressWarnings("WeakerAccess")
public class LocationDiffDao {

    private DBHelper dbHelper;

    public LocationDiffDao(Context context) {
        dbHelper = new DBHelper(context);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void open() {
        dbHelper.getWritableDatabase();
    }

    @SuppressWarnings("UnusedDeclaration")
    public synchronized void close() {
        dbHelper.close();
    }

    public static LocationDiff cursorToLocationDiff(Cursor cursor) {
        LocationDiff locationDiff = new LocationDiff();

        Location fromLocation = new Location();
        Location toLocation = new Location();

        locationDiff.setId(cursor.getLong(0));
        fromLocation.setId(cursor.getLong(1));
        fromLocation.setLatitude(cursor.getFloat(2));
        fromLocation.setLongitude(cursor.getFloat(3));
        fromLocation.setTime(new Date(cursor.getLong(4)));
        locationDiff.setLatitudeDiff(cursor.getFloat(5));
        locationDiff.setLongitudeDiff(cursor.getFloat(6));
        toLocation.setId(cursor.getLong(7));
        toLocation.setLatitude(cursor.getFloat(8));
        toLocation.setLongitude(cursor.getFloat(9));
        toLocation.setTime(new Date(cursor.getLong(10)));

        locationDiff.setFromLocation(fromLocation);
        locationDiff.setToLocation(toLocation);

        return locationDiff;
    }
}
