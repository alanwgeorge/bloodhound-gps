package com.alangeorge.android.bloodhound.model.dao;

import android.content.Context;
import android.database.Cursor;

import com.alangeorge.android.bloodhound.model.Location;
import com.alangeorge.android.bloodhound.model.LocationDiff;

import java.util.Date;

/**
 * @deprecated Use {@link com.alangeorge.android.bloodhound.LocationContentProvider}
 */
@SuppressWarnings("WeakerAccess")
public class LocationDiffDao {

    private DBHelper dbHelper;

    public LocationDiffDao(Context context) {
        dbHelper = new DBHelper(context);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void open() {
        dbHelper.getWritableDatabase();
    }

    @SuppressWarnings("UnusedDeclaration")
    public synchronized void close() {
        dbHelper.close();
    }

}
