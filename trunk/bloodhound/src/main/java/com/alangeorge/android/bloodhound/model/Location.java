package com.alangeorge.android.bloodhound.model;

import android.database.Cursor;
import android.net.Uri;

import com.alangeorge.android.bloodhound.App;
import com.alangeorge.android.bloodhound.model.dao.LocationDao;

import java.util.Date;

import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_ID;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LATITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LONGITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME_STRING;

public class Location {
    private long id;
    private float latitude;
    private float longitude;
    private Date time;

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

    public static Location resolveLocation(Uri locationUri) {
        String[] projection = {
                LOCATIONS_COLUMN_ID,
                LOCATIONS_COLUMN_LONGITUDE,
                LOCATIONS_COLUMN_LATITUDE,
                LOCATIONS_COLUMN_TIME,
                LOCATIONS_COLUMN_TIME_STRING
        };

        Cursor cursor = App.context.getContentResolver().query(locationUri, projection, null, null, null);

        Location location = null;
        if (cursor != null) {
            cursor.moveToFirst();
            location = LocationDao.cursorToLocation(cursor);
        }

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
