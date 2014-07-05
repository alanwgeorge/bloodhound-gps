package com.alangeorge.android.bloodhound;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import static com.alangeorge.android.bloodhound.model.provider.LocationContentProvider.LOCATIONS_CONTENT_URI;
import static com.alangeorge.android.bloodhound.MapDetailActivity.EXTRA_ACTION;
import static com.alangeorge.android.bloodhound.MapDetailActivity.EXTRA_START;
import static com.alangeorge.android.bloodhound.MapDetailActivity.MAP_ACTION_LOCATION;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_ID;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LATITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LONGITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME_STRING;

/**
 * This fragment represents a ListView of recorded locations.  The list is backed by a CursorAdaptor
 * which in turn is feed by the LocationsContentProvider which is a wrapper for an SQLite schema
 */
public class LocationsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "LocationsFragment";

    private static final String ARG_SECTION_NUMBER = "section_number";

    private SimpleCursorAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_main, container, false);
        fillData();
        return rootView;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        Log.d(TAG, "onListItemClick(" + position + ")");

        Intent detailIntent = new Intent(getActivity(), MapDetailActivity.class);

        detailIntent.putExtra(EXTRA_ACTION, MAP_ACTION_LOCATION);
        detailIntent.putExtra(EXTRA_START, id);
        startActivity(detailIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader()");
        String[] projection = {
                LOCATIONS_COLUMN_ID,
                LOCATIONS_COLUMN_LONGITUDE,
                LOCATIONS_COLUMN_LATITUDE,
                LOCATIONS_COLUMN_TIME_STRING,
                LOCATIONS_COLUMN_TIME
        };

        return new CursorLoader(
                getActivity(),
                LOCATIONS_CONTENT_URI,
                projection,
                null,
                null,
                LOCATIONS_COLUMN_TIME + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished()");
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset()");
        adapter.swapCursor(null);
    }

    private void fillData() {
        String[] from = new String[] {
                LOCATIONS_COLUMN_LATITUDE,
                LOCATIONS_COLUMN_LONGITUDE,
                LOCATIONS_COLUMN_TIME_STRING
        };

        int[] to = new int[] {
                R.id.latitudeTextView,
                R.id.longitudeTextView,
                R.id.timeTextView
        };

        //getLoaderManager().enableDebugLogging(true);
        getLoaderManager().initLoader(0, null, this);

        adapter = new SimpleCursorAdapter(getActivity(), R.layout.location_list_item, null, from, to, 0);

        setListAdapter(adapter);
    }
}
