package com.alangeorge.android.bloodhound;

import android.content.ContentProvider;
import android.content.ContentResolver;
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
import java.util.HashSet;

import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_ALL_COLUMNS;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_ID;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_ALL_COLUMNS;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.TABLE_LOCATIONS;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.TABLE_LOCATIONS_DIFF;

@SuppressWarnings("WeakerAccess")
public class LocationContentProvider extends ContentProvider {
    private static final String TAG = "LocationContentProvider";
    private DBHelper dbHelper;

    // used for the UriMatcher
    private static final int LOCATIONS = 10;
    private static final int LOCATION_ID = 20;
    private static final int LOCATIONS_DIFF = 30;

    private static final String AUTHORITY = "com.alangeorge.android.bloodhound.locations.contentprovider";
    private static final String LOCATIONS_PATH = "locations";
    private static final String LOCATIONS_DIFF_PATH = "locations_diff";

    public static final Uri LOCATIONS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + LOCATIONS_PATH);
    public static final Uri LOCATIONS_DIFF_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + LOCATIONS_DIFF_PATH);
//    @SuppressWarnings("UnusedDeclaration")
//    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/locations";
    @SuppressWarnings("UnusedDeclaration")
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/location";
    @SuppressWarnings("UnusedDeclaration")
    public static final String CONTENT_DIFF_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/locations_diff";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, LOCATIONS_PATH, LOCATIONS);
        sURIMatcher.addURI(AUTHORITY, LOCATIONS_PATH + "/#", LOCATION_ID);
        sURIMatcher.addURI(AUTHORITY, LOCATIONS_DIFF_PATH, LOCATIONS_DIFF);
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
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(TABLE_LOCATIONS, LOCATIONS_COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(TABLE_LOCATIONS, LOCATIONS_COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);

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

        long id;

        switch (uriType) {
            case LOCATIONS:
                id = sqlDB.insert(TABLE_LOCATIONS, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);

        return Uri.parse(LOCATIONS_PATH + "/" + id);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,  String[] selectionArgs, String sortOrder) {
        // Using SQLiteQueryBuilder instead of query() method
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
            case LOCATIONS_DIFF:
                checkColumnsLocationsDiff(projection);
                queryBuilder.setTables(TABLE_LOCATIONS_DIFF);
                db = dbHelper.getWritableDatabase();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        //noinspection ConstantConditions
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

        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    private void checkColumnsLocations(String[] projection) {
//        String[] available = {
//                LocationDao.LOCATIONS_COLUMN_LATITUDE,
//                LocationDao.LOCATIONS_COLUMN_LONGITUDE,
//                LocationDao.LOCATIONS_COLUMN_TIME,
//                LocationDao.LOCATIONS_COLUMN_TIME_STRING,
//                LocationDao.LOCATIONS_COLUMN_ID
//        };

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
}
