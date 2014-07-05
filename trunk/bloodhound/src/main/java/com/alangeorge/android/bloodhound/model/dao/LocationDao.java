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

import static com.alangeorge.android.bloodhound.model.dao.DBHelper.TABLE_LOCATIONS;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_ALL_COLUMNS;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_ID;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LATITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LONGITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME_STRING;

/**
 * @deprecated Use {@link com.alangeorge.android.bloodhound.LocationContentProvider}
 */
@SuppressWarnings({"WeakerAccess", "UnusedDeclaration"})
public class LocationDao {
    private static final String TAG = "LocationDao";

    private SQLiteDatabase database;
    private SQLiteOpenHelper dbHelper;
    private Context context;
    private ContentObserver contentObserver = new LocationContentObserver(null);

    public LocationDao(Context context) {
        dbHelper = new DBHelper(context);
        this.context = context;
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public synchronized void close() {
        dbHelper.close();
    }

    @SuppressWarnings("UnusedReturnValue")
    public Location create(double latitude, double longitude) {
        Date now = new Date();

        ContentValues values = new ContentValues();
        values.put(LOCATIONS_COLUMN_LATITUDE, latitude);
        values.put(LOCATIONS_COLUMN_LONGITUDE, longitude);
        values.put(LOCATIONS_COLUMN_TIME, now.getTime());
        values.put(LOCATIONS_COLUMN_TIME_STRING, now.toString());

        long insertId = database.insert(TABLE_LOCATIONS, null, values);

        Cursor cursor = database.query(TABLE_LOCATIONS, LOCATIONS_ALL_COLUMNS, LOCATIONS_COLUMN_ID + " = " + insertId, null, null, null, null);
        cursor.moveToFirst();
        Location newLocation = DBHelper.cursorToLocation(cursor);
        cursor.close();

        context.getContentResolver().notifyChange(LocationContentProvider.LOCATIONS_CONTENT_URI, null);
        context.getContentResolver().notifyChange(LocationContentProvider.LOCATIONS_CONTENT_URI, contentObserver);

        Log.d(TAG, "new location = " + newLocation);

        return newLocation;
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
