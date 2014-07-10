package com.alangeorge.android.bloodhound.model;

import android.database.Cursor;
import android.net.Uri;

import com.alangeorge.android.bloodhound.App;

import java.util.Date;

import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_ALL_COLUMNS;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_COLUMN_ID;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_COLUMN_LATITUDE_DIFF;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_COLUMN_LONGITUDE_DIFF;
import static com.alangeorge.android.bloodhound.model.provider.LocationContentProvider.LOCATIONS_DIFF_CONTENT_URI;

/**
 * Model Object representing a to and from {@link com.alangeorge.android.bloodhound.model.Location} difference.
 * Could be used to calculate target path.
 */
public class LocationDiff {
    private long id = -1;

    private Location fromLocation;
    private Location toLocation;
    private float latitudeDiff;
    private float longitudeDiff;

    public LocationDiff() {}

    public LocationDiff(long locationDiffId) {
        Uri locationUri = Uri.parse(LOCATIONS_DIFF_CONTENT_URI + "/" + locationDiffId);

        Cursor cursor = App.context.getContentResolver().query(
                locationUri,
                LOCATIONS_DIFF_ALL_COLUMNS,
                null,
                null,
                null
        );

        if (cursor != null) {
            cursor.moveToFirst();

            Location fromLocation = new Location();
            Location toLocation = new Location();

            setId(cursor.getLong(0));
            fromLocation.setId(cursor.getLong(1));
            fromLocation.setLatitude(cursor.getFloat(2));
            fromLocation.setLongitude(cursor.getFloat(3));
            fromLocation.setTime(new Date(cursor.getLong(4)));
            setLatitudeDiff(cursor.getFloat(5));
            setLongitudeDiff(cursor.getFloat(6));
            toLocation.setId(cursor.getLong(7));
            toLocation.setLatitude(cursor.getFloat(8));
            toLocation.setLongitude(cursor.getFloat(9));
            toLocation.setTime(new Date(cursor.getLong(10)));

            setFromLocation(fromLocation);
            setToLocation(toLocation);
            cursor.close();
        }
    }

    public Location getFromLocation() {
        return fromLocation;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setFromLocation(Location fromLocation) {
        this.fromLocation = fromLocation;
    }

    public Location getToLocation() {
        return toLocation;
    }

    public void setToLocation(Location toLocation) {
        this.toLocation = toLocation;
    }

    @SuppressWarnings("UnusedDeclaration")
    public float getLatitudeDiff() {
        return latitudeDiff;
    }

    public void setLatitudeDiff(float latitudeDiff) {
        this.latitudeDiff = latitudeDiff;
    }

    @SuppressWarnings("UnusedDeclaration")
    public float getLongitudeDiff() {
        return longitudeDiff;
    }

    public void setLongitudeDiff(float longitudeDiff) {
        this.longitudeDiff = longitudeDiff;
    }

    public LocationDiff getNext() {
        if (id == -1) {
            return null;
        }

        Cursor cursor = App.context.getContentResolver().query(
                LOCATIONS_DIFF_CONTENT_URI,
                LOCATIONS_DIFF_ALL_COLUMNS,
                LOCATIONS_DIFF_COLUMN_ID + " > ? and (" + LOCATIONS_DIFF_COLUMN_LATITUDE_DIFF + " != 0 or " + LOCATIONS_DIFF_COLUMN_LONGITUDE_DIFF + " != 0)",
                new String[]{String.valueOf(id)},
                LOCATIONS_DIFF_COLUMN_ID + " asc"
        );

        LocationDiff locationDiff = null;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            locationDiff = cursorToLocationDiff(cursor);
            cursor.close();
        }

        return locationDiff;
    }

    public LocationDiff getPrev() {
        if (id == -1) {
            return null;
        }

        Cursor cursor = App.context.getContentResolver().query(
                LOCATIONS_DIFF_CONTENT_URI,
                LOCATIONS_DIFF_ALL_COLUMNS,
                LOCATIONS_DIFF_COLUMN_ID + " < ? and (" + LOCATIONS_DIFF_COLUMN_LATITUDE_DIFF + " != 0 or " + LOCATIONS_DIFF_COLUMN_LONGITUDE_DIFF + " != 0)",
                new String[]{String.valueOf(id)},
                LOCATIONS_DIFF_COLUMN_ID + " desc"
        );

        LocationDiff locationDiff = null;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            locationDiff = cursorToLocationDiff(cursor);
            cursor.close();
        }

        return locationDiff;
    }

    /**
     * Takes a {@link android.database.Cursor} and converts it to a model object, {@link LocationDiff}
     *
     * @param cursor the {@link android.database.Cursor} to convert
     * @return the resulting {@link LocationDiff}
     */
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


    @Override
    public String toString() {
        return "LocationDiff{" +
                "id=" + id +
                ", fromLocation=" + fromLocation +
                ", toLocation=" + toLocation +
                ", latitudeDiff=" + latitudeDiff +
                ", longitudeDiff=" + longitudeDiff +
                '}';
    }
}
