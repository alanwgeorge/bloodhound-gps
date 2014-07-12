package com.alangeorge.android.bloodhound.model.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.alangeorge.android.bloodhound.model.dao.DBHelper;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import static com.alangeorge.android.bloodhound.model.dao.DBHelper.GEOFENCES_ALL_COLUMNS;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.GEOFENCES_COLUMN_CREATE_TIME;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.GEOFENCES_COLUMN_ID;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_ALL_COLUMNS;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_ID;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_ALL_COLUMNS;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_COLUMN_ID;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.TABLE_GEOFENSES;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.TABLE_LOCATIONS;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.TABLE_LOCATIONS_DIFF;

/**
 * {@link android.content.ContentProvider} implementation for the SQLite locations table and locations_diff view.
 */
@SuppressWarnings("WeakerAccess")
public class LocationContentProvider extends ContentProvider {
    private static final String TAG = "LocationContentProvider";
    private DBHelper dbHelper;

    // used for the UriMatcher
    private static final int LOCATIONS = 10;
    private static final int LOCATION_ID = 20;
    private static final int LOCATIONS_DIFF = 30;
    private static final int LOCATIONS_DIFF_ID = 40;
    private static final int GEOFENCES = 50;
    private static final int GEOFENCES_ID = 60;

    private static final String LOCATIONS_PATH = "locations";
    private static final String LOCATIONS_DIFF_PATH = "locations_diff";
    private static final String GEOFENCE_PATH = "geofence";

    public static final String AUTHORITY = "com.alangeorge.android.bloodhound.locations.contentprovider";
    public static final Uri LOCATIONS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + LOCATIONS_PATH);
    public static final Uri LOCATIONS_DIFF_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + LOCATIONS_DIFF_PATH);
    public static final Uri GEOFENCE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + GEOFENCE_PATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, LOCATIONS_PATH, LOCATIONS);
        sURIMatcher.addURI(AUTHORITY, LOCATIONS_PATH + "/#", LOCATION_ID);
        sURIMatcher.addURI(AUTHORITY, LOCATIONS_DIFF_PATH, LOCATIONS_DIFF);
        sURIMatcher.addURI(AUTHORITY, LOCATIONS_DIFF_PATH + "/#", LOCATIONS_DIFF_ID);
        sURIMatcher.addURI(AUTHORITY, GEOFENCE_PATH, GEOFENCES);
        sURIMatcher.addURI(AUTHORITY, GEOFENCE_PATH + "/#", GEOFENCES_ID);

    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate()");
        dbHelper = new DBHelper(getContext());
        return false;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();

        int rowsDeleted;

        switch (uriType) {
            case LOCATIONS:
                rowsDeleted = sqlDB.delete(TABLE_LOCATIONS, selection, selectionArgs);
                break;
            case LOCATION_ID:
                String locationId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(TABLE_LOCATIONS, LOCATIONS_COLUMN_ID + "=" + locationId, null);
                } else {
                    rowsDeleted = sqlDB.delete(TABLE_LOCATIONS, LOCATIONS_COLUMN_ID + "=" + locationId + " and " + selection, selectionArgs);
                }
                break;
            case GEOFENCES_ID:
                String geofenceId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(TABLE_GEOFENSES, GEOFENCES_COLUMN_ID + "=" + geofenceId, null);
                } else {
                    rowsDeleted = sqlDB.delete(TABLE_GEOFENSES, GEOFENCES_COLUMN_ID + "=" + geofenceId + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(LOCATIONS_CONTENT_URI, null);
        getContext().getContentResolver().notifyChange(LOCATIONS_DIFF_CONTENT_URI, null);
        getContext().getContentResolver().notifyChange(GEOFENCE_CONTENT_URI, null);

        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        Uri result;

        long id;

        switch (uriType) {
            case LOCATIONS:
                id = sqlDB.insert(TABLE_LOCATIONS, null, values);
                getContext().getContentResolver().notifyChange(LOCATIONS_CONTENT_URI, null);
                getContext().getContentResolver().notifyChange(LOCATIONS_DIFF_CONTENT_URI, null);
                result = Uri.parse("content://" + AUTHORITY + "/" + LOCATIONS_PATH + "/" + id);
                break;
            case GEOFENCES:
                values.put(GEOFENCES_COLUMN_CREATE_TIME, new Date().getTime());
                id = sqlDB.insert(TABLE_GEOFENSES, null, values);
                getContext().getContentResolver().notifyChange(GEOFENCE_CONTENT_URI, null);
                result = Uri.parse("content://" + AUTHORITY + "/" + GEOFENCE_PATH + "/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        return result;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,  String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        Cursor cursor;
        SQLiteDatabase db;

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case LOCATION_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(LOCATIONS_COLUMN_ID + "=" + uri.getLastPathSegment());
            case LOCATIONS:
                checkColumnsLocations(projection);
                queryBuilder.setTables(TABLE_LOCATIONS);
                db = dbHelper.getWritableDatabase();
                break;
            case LOCATIONS_DIFF_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(LOCATIONS_DIFF_COLUMN_ID + "=" + uri.getLastPathSegment());
            case LOCATIONS_DIFF:
                checkColumnsLocationsDiff(projection);
                queryBuilder.setTables(TABLE_LOCATIONS_DIFF);
                db = dbHelper.getWritableDatabase();
                break;
            case GEOFENCES_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(GEOFENCES_COLUMN_ID + "=" + uri.getLastPathSegment());
            case GEOFENCES:
                checkColumnsGeoFence(projection);
                queryBuilder.setTables(TABLE_GEOFENSES);
                db = dbHelper.getWritableDatabase();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int rowsUpdated;

        switch (uriType) {
            case LOCATIONS:
                rowsUpdated = sqlDB.update(TABLE_LOCATIONS, values, selection, selectionArgs);
                break;
            case LOCATION_ID:
                String id = uri.getLastPathSegment();

                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(TABLE_LOCATIONS, values, LOCATIONS_COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = sqlDB.update(TABLE_LOCATIONS, values, LOCATIONS_COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }

                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        // make sure that potential listeners are getting notified
        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(LOCATIONS_CONTENT_URI, null);
        getContext().getContentResolver().notifyChange(LOCATIONS_DIFF_CONTENT_URI, null);

        return rowsUpdated;
    }

    private void checkColumnsLocations(String[] projection) {
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(LOCATIONS_ALL_COLUMNS));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

    private void checkColumnsLocationsDiff(String[] projection) {
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(LOCATIONS_DIFF_ALL_COLUMNS));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

    private void checkColumnsGeoFence(String[] projection) {
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(GEOFENCES_ALL_COLUMNS));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

}
