package com.alangeorge.android.bloodhound.model.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * {@link android.database.sqlite.SQLiteOpenHelper} for our {@link com.alangeorge.android.bloodhound.model.Location} table
 * {@link com.alangeorge.android.bloodhound.model.LocationDiff} view and {@link com.alangeorge.android.bloodhound.model.GeoFence} table
 *  <p>
 * Below are example commands (OSX) to access the database of a device with BloodHound installed
 * <p>
 * <pre>
 * {@code
 * $ adb -s 10.0.1.28:5555 backup -f data.ab -noapk com.alangeorge.android.bloodhound
 * $ dd if=data.ab bs=1 skip=24 | python -c "import zlib,sys;sys.stdout.write(zlib.decompress(sys.stdin.read()))" | tar -xvf -
 * $ sqlite3 apps/com.alangeorge.android.bloodhound/db/locations.db
 * sqlite> select * from locations;
 * sqlite> select * from locations_diff;
 * sqlite> select * from geofences;
 * }
 * </pre>
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    private static final String DATABASE_NAME = "locations.db";
    private static final int DATABASE_VERSION = 12;

    // locations Table
    public static final String TABLE_LOCATIONS = "locations";
    public static final String LOCATIONS_COLUMN_ID = "_id";
    public static final String LOCATIONS_COLUMN_LATITUDE = "latitude";
    public static final String LOCATIONS_COLUMN_LONGITUDE = "longitude";
    public static final String LOCATIONS_COLUMN_TIME = "time";
    public static final String LOCATIONS_COLUMN_TIME_STRING = "time_str";

    public static final String[] LOCATIONS_ALL_COLUMNS = {
            LOCATIONS_COLUMN_ID,
            LOCATIONS_COLUMN_LATITUDE,
            LOCATIONS_COLUMN_LONGITUDE,
            LOCATIONS_COLUMN_TIME,
            LOCATIONS_COLUMN_TIME_STRING
    };

    @SuppressWarnings("UnusedDeclaration")
    public static final String LOCATIONS_DATABASE_CREATE = "create table " + TABLE_LOCATIONS + " (" + LOCATIONS_COLUMN_ID
            + " integer primary key autoincrement, " + LOCATIONS_COLUMN_LATITUDE + " real not null, " + LOCATIONS_COLUMN_LONGITUDE
            + " real not null, " + LOCATIONS_COLUMN_TIME + " integer not null, " + LOCATIONS_COLUMN_TIME_STRING + " text not null);";

    // loc_diff view
    public static final String TABLE_LOCATIONS_DIFF = "loc_diff";
    public static final String LOCATIONS_DIFF_COLUMN_ID = "_id";
    public static final String LOCATIONS_DIFF_COLUMN_ID1 = "id1";
    public static final String LOCATIONS_DIFF_COLUMN_LATITUDE1 = "latitude1";
    public static final String LOCATIONS_DIFF_COLUMN_LONGITUDE1 = "longitude1";
    public static final String LOCATIONS_DIFF_COLUMN_TIME1 = "time1";
    public static final String LOCATIONS_DIFF_COLUMN_LATITUDE_DIFF = "latitude_diff";
    public static final String LOCATIONS_DIFF_COLUMN_LONGITUDE_DIFF = "longitude_diff";
    public static final String LOCATIONS_DIFF_COLUMN_ID2 = "id2";
    public static final String LOCATIONS_DIFF_COLUMN_LATITUDE2 = "latitude2";
    public static final String LOCATIONS_DIFF_COLUMN_LONGITUDE2 = "longitude2";
    public static final String LOCATIONS_DIFF_COLUMN_TIME2 = "time2";

    public static final String[] LOCATIONS_DIFF_ALL_COLUMNS = {
            LOCATIONS_DIFF_COLUMN_ID,
            LOCATIONS_DIFF_COLUMN_ID1,
            LOCATIONS_DIFF_COLUMN_LATITUDE1,
            LOCATIONS_DIFF_COLUMN_LONGITUDE1,
            LOCATIONS_DIFF_COLUMN_TIME1,
            LOCATIONS_DIFF_COLUMN_LATITUDE_DIFF,
            LOCATIONS_DIFF_COLUMN_LONGITUDE_DIFF,
            LOCATIONS_DIFF_COLUMN_ID2,
            LOCATIONS_DIFF_COLUMN_LATITUDE2,
            LOCATIONS_DIFF_COLUMN_LONGITUDE2,
            LOCATIONS_DIFF_COLUMN_TIME2
    };

    public static final String LOCATIONS_DIFF_DATABASE_CREATE = "create view " + TABLE_LOCATIONS_DIFF + " as select l1._id as " +
            LOCATIONS_DIFF_COLUMN_ID + ",l1._id as " + LOCATIONS_DIFF_COLUMN_ID1 + ", l1.latitude as " + LOCATIONS_DIFF_COLUMN_LATITUDE1 +
            ", l1.longitude as " + LOCATIONS_DIFF_COLUMN_LONGITUDE1 + ", l1.time as " + LOCATIONS_DIFF_COLUMN_TIME1 +
            ", round((l1.latitude - l2.latitude),4) as " + LOCATIONS_DIFF_COLUMN_LATITUDE_DIFF + ", round((l1.longitude - l2.longitude),4) as " +
            LOCATIONS_DIFF_COLUMN_LONGITUDE_DIFF + ", l2._id as " + LOCATIONS_DIFF_COLUMN_ID2 + ", l2.latitude as " +
            LOCATIONS_DIFF_COLUMN_LATITUDE2 + ", l2.longitude as " + LOCATIONS_DIFF_COLUMN_LONGITUDE2 + ", l2.time as " +
            LOCATIONS_DIFF_COLUMN_TIME2 + " from " + TABLE_LOCATIONS + " l1, " + TABLE_LOCATIONS +
            " l2 where l1.rowid = l2.rowid-1 order by l1.time";

    // geofences table
    public static final String TABLE_GEOFENSES = "geofences";
    public static final String GEOFENCES_COLUMN_ID = "_id";
    public static final String GEOFENCES_COLUMN_NAME = "name";
    public static final String GEOFENCES_COLUMN_LATITUDE = "latitude";
    public static final String GEOFENCES_COLUMN_LONGITUDE = "longitude";
    public static final String GEOFENCES_COLUMN_RADIUS = "radius";
    public static final String GEOFENCES_COLUMN_CREATE_TIME = "createtime";

    public static final String[] GEOFENCES_ALL_COLUMNS = {
            GEOFENCES_COLUMN_ID,
            GEOFENCES_COLUMN_NAME,
            GEOFENCES_COLUMN_LATITUDE,
            GEOFENCES_COLUMN_LONGITUDE,
            GEOFENCES_COLUMN_RADIUS,
            GEOFENCES_COLUMN_CREATE_TIME
    };

    public static final String GEOFENCE_DATABASE_CREATE = "create table " + TABLE_GEOFENSES + " (" + GEOFENCES_COLUMN_ID
            + " integer primary key autoincrement, " + GEOFENCES_COLUMN_NAME + " text not null, " + GEOFENCES_COLUMN_LATITUDE
            + " real not null, " + GEOFENCES_COLUMN_LONGITUDE + " real not null, " + GEOFENCES_COLUMN_RADIUS + " real not null, "
            + GEOFENCES_COLUMN_CREATE_TIME + " integer not null);";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate()");
//        db.execSQL(LOCATIONS_DATABASE_CREATE);
        db.execSQL(LOCATIONS_DIFF_DATABASE_CREATE);
        db.execSQL(GEOFENCE_DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        db.execSQL("DROP VIEW IF EXISTS " + TABLE_LOCATIONS_DIFF);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GEOFENSES);
        onCreate(db);
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        Log.d(TAG, "getWritableDatabase(DATABASE_VERSION:" + DATABASE_VERSION + ")");
        return super.getWritableDatabase();
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        Log.d(TAG, "getReadableDatabase(DATABASE_VERSION:" + DATABASE_VERSION + ")");
        return super.getReadableDatabase();
    }
}
