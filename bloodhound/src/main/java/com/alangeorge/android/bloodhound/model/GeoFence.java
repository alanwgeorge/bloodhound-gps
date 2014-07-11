package com.alangeorge.android.bloodhound.model;

import android.database.Cursor;
import android.net.Uri;

import com.alangeorge.android.bloodhound.App;

import java.util.Date;

import static com.alangeorge.android.bloodhound.model.dao.DBHelper.GEOFENCES_ALL_COLUMNS;
import static com.alangeorge.android.bloodhound.model.provider.LocationContentProvider.GEOFENCE_CONTENT_URI;

public class GeoFence {
    private long id;
    private float latitude;
    private float longitude;
    private float radius;
    private Date createTime;

    public GeoFence() {}

    public GeoFence(long id) {
        Uri locationUri = Uri.parse(GEOFENCE_CONTENT_URI + "/" + id);

        Cursor cursor = App.context.getContentResolver().query(
                locationUri,
                GEOFENCES_ALL_COLUMNS,
                null,
                null,
                null
        );

        if (cursor != null) {
            cursor.moveToFirst();
            setId(cursor.getLong(0));
            setLatitude(cursor.getFloat(1));
            setLongitude(cursor.getFloat(2));
            setRadius(cursor.getFloat(3));
            setCreateTime(new Date(cursor.getLong(4)));
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

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "GeoFence{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", radius=" + radius +
                ", createTime=" + createTime +
                '}';
    }
}
