package com.alangeorge.android.bloodhound.model.dao;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.alangeorge.android.bloodhound.LocationContentProvider;
import com.alangeorge.android.bloodhound.model.Location;

import java.util.Date;

@SuppressWarnings("WeakerAccess")
public class LocationDao extends SQLiteOpenHelper {
    private static final String TAG = "LocationDao";
    private static final String DATABASE_NAME = "locations.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_LOCATIONS = "locations";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_TIME_STRING = "time_str";
    @SuppressWarnings("WeakerAccess")
    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_TIME, COLUMN_TIME_STRING};

    public static final String DATABASE_CREATE = "create table " + TABLE_LOCATIONS + " (" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_LATITUDE + " real not null, " + COLUMN_LONGITUDE
            + " real not null, " + COLUMN_TIME + " integer not null, " + COLUMN_TIME_STRING + " text not null);";

    private SQLiteDatabase database;
    private Context context;
    private ContentObserver contentObserver = new LocationContentObserver(null);

    public LocationDao(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        onCreate(db);
    }

    public void open() {
        database = getWritableDatabase();
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public synchronized void close() {
        super.close();
    }

    @SuppressWarnings("UnusedReturnValue")
    public Location create(double latitude, double longitude) {
        Date now = new Date();

        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_TIME, now.getTime());
        values.put(COLUMN_TIME_STRING, now.toString());

        long insertId = database.insert(TABLE_LOCATIONS, null, values);

        Cursor cursor = database.query(TABLE_LOCATIONS, ALL_COLUMNS, COLUMN_ID + " = " + insertId, null, null, null, null);
        cursor.moveToFirst();
        Location newLocation = cursorToLocation(cursor);
        cursor.close();

        context.getContentResolver().notifyChange(LocationContentProvider.CONTENT_URI, null);
        context.getContentResolver().notifyChange(LocationContentProvider.CONTENT_URI, contentObserver);

        Log.d(TAG, "new location = " + newLocation);

        return newLocation;
    }

    public static Location cursorToLocation(Cursor cursor) {
        Location location = new Location();

        location.setId(cursor.getLong(0));
        location.setLatitude(cursor.getFloat(1));
        location.setLongitude(cursor.getFloat(2));
        location.setTime(new Date(cursor.getLong(3)));

        return location;
    }

    private class LocationContentObserver extends ContentObserver {
        public static final String TAG = "LocationContentObserver";
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public LocationContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            Log.d(TAG, "deliverSelfNotifications()");
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "onChange(" + selfChange + ")");
            super.onChange(selfChange);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d(TAG, "onChange(" + selfChange + ", " + uri + ")");
            super.onChange(selfChange, uri);
        }
    }
}
