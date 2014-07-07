package com.alangeorge.android.bloodhound.model.provider.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.IsolatedContext;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.util.Log;

import com.alangeorge.android.bloodhound.model.provider.LocationContentProvider;

import java.util.Date;

import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_ID;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LATITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LONGITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME_STRING;

public class LocationContentProviderTest extends ProviderTestCase2<LocationContentProvider> {
    private static final String TAG = "LocationContentProviderTest";

    private MockContentResolver mockContentResolver;
    private IsolatedContext mockContext;

    public LocationContentProviderTest() {
        super(LocationContentProvider.class, LocationContentProvider.AUTHORITY);
    }

    @Override
    public void setUp() throws Exception {
        Log.d(TAG, "setup()");
        super.setUp();
        mockContentResolver = getMockContentResolver();
        mockContext = getMockContext();
    }

    public void testInsertLocation() throws Exception {
        Log.d(TAG, "testInsertLocation()");

        Date now = new Date();

        float lat = 37.6789f;
        float lng = -122.6578f;

        ContentValues values = new ContentValues();
        values.put(LOCATIONS_COLUMN_LATITUDE, lat);
        values.put(LOCATIONS_COLUMN_LONGITUDE, lng);
        values.put(LOCATIONS_COLUMN_TIME, now.getTime());
        values.put(LOCATIONS_COLUMN_TIME_STRING, now.toString());

        Uri uri = mockContentResolver.insert(LocationContentProvider.LOCATIONS_CONTENT_URI, values);

        Uri itemUri = Uri.parse("content://" + LocationContentProvider.AUTHORITY + "/" + uri.getEncodedPath());

        String[] projection = {
                LOCATIONS_COLUMN_ID,
                LOCATIONS_COLUMN_LATITUDE,
                LOCATIONS_COLUMN_LONGITUDE,
                LOCATIONS_COLUMN_TIME,
                LOCATIONS_COLUMN_TIME_STRING
        };

        Cursor cursor = mockContentResolver.query(itemUri, projection, null, null, null);

        cursor.moveToFirst();

        assertTrue("Latitude not what was passed in", cursor.getFloat(1) == lat);
        assertTrue("Longitude not what was passed in", cursor.getFloat(2) == lng);
    }

    @Override
    public void tearDown() throws Exception {
        Log.d(TAG, "tearDown()");
        super.tearDown();
    }
}
