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

import com.alangeorge.android.bloodhound.model.dao.LocationDao;

import java.util.Arrays;
import java.util.HashSet;

@SuppressWarnings("WeakerAccess")
public class LocationContentProvider extends ContentProvider {
    private LocationDao locationDao;

    // used for the UriMatcher
    private static final int LOCATIONS = 10;
    private static final int LOCATION_ID = 20;

    private static final String AUTHORITY = "com.alangeorge.android.bloodhound.locations.contentprovider";
    private static final String BASE_PATH = "locations";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
    @SuppressWarnings("UnusedDeclaration")
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/locations";
    @SuppressWarnings("UnusedDeclaration")
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/location";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, LOCATIONS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", LOCATION_ID);
    }

    @Override
    public boolean onCreate() {
        locationDao = new LocationDao(getContext());
        return false;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = locationDao.getWritableDatabase();

        int rowsDeleted;

        switch (uriType) {
            case LOCATIONS:
                rowsDeleted = sqlDB.delete(LocationDao.TABLE_LOCATIONS, selection, selectionArgs);
                break;
            case LOCATION_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(LocationDao.TABLE_LOCATIONS, LocationDao.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(LocationDao.TABLE_LOCATIONS, LocationDao.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

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
        SQLiteDatabase sqlDB = locationDao.getWritableDatabase();

        long id;

        switch (uriType) {
            case LOCATIONS:
                id = sqlDB.insert(LocationDao.TABLE_LOCATIONS, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,  String[] selectionArgs, String sortOrder) {
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(LocationDao.TABLE_LOCATIONS);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case LOCATIONS:
                break;
            case LOCATION_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(LocationDao.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = locationDao.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = locationDao.getWritableDatabase();
        int rowsUpdated;

        switch (uriType) {
            case LOCATIONS:
                rowsUpdated = sqlDB.update(LocationDao.TABLE_LOCATIONS, values, selection, selectionArgs);
                break;
            case LOCATION_ID:
                String id = uri.getLastPathSegment();

                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(LocationDao.TABLE_LOCATIONS, values, LocationDao.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = sqlDB.update(LocationDao.TABLE_LOCATIONS, values, LocationDao.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }

                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {
                LocationDao.COLUMN_LATITUDE,
                LocationDao.COLUMN_LONGITUDE,
                LocationDao.COLUMN_TIME,
                LocationDao.COLUMN_TIME_STRING,
                LocationDao.COLUMN_ID
        };

        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
