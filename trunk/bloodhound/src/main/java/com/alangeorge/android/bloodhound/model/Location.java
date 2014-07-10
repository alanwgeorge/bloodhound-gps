package com.alangeorge.android.bloodhound.model;

import android.database.Cursor;
import android.net.Uri;

import com.alangeorge.android.bloodhound.App;

import java.util.Date;

import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_ALL_COLUMNS;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_ID;
import static com.alangeorge.android.bloodhound.model.provider.LocationContentProvider.LOCATIONS_CONTENT_URI;

/**
 * Model object representing a recorded location.
 */
public class Location {
    private long id;
    private float latitude;
    private float longitude;
    private Date time;

    public Location() {}

    public Location(long locationId) {
        Uri locationUri = Uri.parse(LOCATIONS_CONTENT_URI + "/" + locationId);

        Cursor cursor = App.context.getContentResolver().query(
                locationUri,
                LOCATIONS_ALL_COLUMNS,
                null,
                null,
                null
        );

        if (cursor != null) {
            cursor.moveToFirst();
            setId(cursor.getLong(0));
            setLatitude(cursor.getFloat(1));
            setLongitude(cursor.getFloat(2));
            setTime(new Date(cursor.getLong(3)));
            cursor.close();
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * Retrieves the last (highest _id) location recorded
     *
     * @return location translated in to a model object
     */
    public static Location getLastLocation() {

        Cursor cursor = App.context.getContentResolver().query(
                LOCATIONS_CONTENT_URI,
                LOCATIONS_ALL_COLUMNS,
                null,
                null,
                LOCATIONS_COLUMN_ID + " desc"
        );

        Location location = null;
        if (cursor != null) {
            cursor.moveToFirst();
            location = cursorToLocation(cursor);
            cursor.close();
        }

        return location;
    }

    /**
     * Takes a {@link android.database.Cursor} and converts it to a model object, {@link Location}
     *
     * @param cursor the {@link android.database.Cursor} to convert
     * @return the resulting {@link Location}
     */
    private static Location cursorToLocation(Cursor cursor) {
        Location location = new Location();

        location.setId(cursor.getLong(0));
        location.setLatitude(cursor.getFloat(1));
        location.setLongitude(cursor.getFloat(2));
        location.setTime(new Date(cursor.getLong(3)));

        return location;
    }
    @Override
    public String toString() {
        return "Location{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", time=" + time +
                '}';
    }
}
