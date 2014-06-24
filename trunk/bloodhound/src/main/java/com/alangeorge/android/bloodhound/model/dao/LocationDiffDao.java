package com.alangeorge.android.bloodhound.model.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

@SuppressWarnings("WeakerAccess")
public class LocationDiffDao {
    private static final String TAG = "LocationDiffDao";
//    private static final String DATABASE_NAME = "locations.db";
//    private static final int DATABASE_VERSION = 3;
//
//    public static final String TABLE_LOCATIONS_DIFF = "loc_diff";
//    public static final String COLUMN_ID1 = "id1";
//    public static final String COLUMN_LATITUDE1 = "latitude1";
//    public static final String COLUMN_LONGITUDE1 = "longitude1";
//    public static final String COLUMN_TIME1 = "time1";
//    public static final String COLUMN_LATITUDE_DIFF = "latitude_diff";
//    public static final String COLUMN_LONGITUDE_DIFF = "longitude_diff";
//    public static final String COLUMN_ID2 = "id2";
//    public static final String COLUMN_LATITUDE2 = "latitude2";
//    public static final String COLUMN_LONGITUDE2 = "longitude2";
//    public static final String COLUMN_TIME2 = "time2";
//
//    @SuppressWarnings("WeakerAccess")
//    public static final String[] LOCATIONS_DIFF_ALL_COLUMNS = {
//            COLUMN_ID1,
//            COLUMN_LATITUDE1,
//            COLUMN_LONGITUDE1,
//            COLUMN_TIME1,
//            COLUMN_LATITUDE_DIFF,
//            COLUMN_LONGITUDE_DIFF,
//            COLUMN_ID2,
//            COLUMN_LATITUDE2,
//            COLUMN_LONGITUDE2,
//            COLUMN_TIME2
//    };
//
//    public static final String DATABASE_CREATE = "create view " + TABLE_LOCATIONS_DIFF + "as select l1._id as " + COLUMN_ID1 +
//            ", l1.latitude as " + COLUMN_LATITUDE1 + ", l1.longitude as " + COLUMN_LONGITUDE1 + ", l1.time as " + COLUMN_TIME1 +
//            ", round((l1.latitude - l2.latitude),4) as " + COLUMN_LATITUDE_DIFF + ", round((l1.longitude - l2.longitude),4) as " +
//            COLUMN_LATITUDE_DIFF + ", l2._id as " + COLUMN_ID2 + ", l2.latitude as " + COLUMN_LATITUDE2 + ", l2.longitude as " + COLUMN_LONGITUDE2 +
//            ", l2.time as " + COLUMN_TIME2 + "from " + LocationDao.TABLE_LOCATIONS + " l1, " + LocationDao.TABLE_LOCATIONS +
//            " l2 where l1.rowid = l2.rowid-1 order by l1.time";

    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private Context context;

    public LocationDiffDao(Context context) {
        dbHelper = new DBHelper(context);
        this.context = context;
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public synchronized void close() {
        dbHelper.close();
    }
}
