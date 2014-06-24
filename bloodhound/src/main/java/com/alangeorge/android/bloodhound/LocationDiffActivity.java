package com.alangeorge.android.bloodhound;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.Date;

import static com.alangeorge.android.bloodhound.BloodHoundReceiver.BLOODHOUND_RECEIVER_ACTION;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_ALL_COLUMNS;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_COLUMN_LATITUDE1;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_COLUMN_LATITUDE2;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_COLUMN_LONGITUDE1;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_COLUMN_LONGITUDE2;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_COLUMN_TIME2;


@SuppressWarnings("WeakerAccess")
public class LocationDiffActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "MainActivity";

    private LocationDiffCursorAdaptor adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);
        fillData();
        registerForContextMenu(getListView());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_start) {
            sendBroadcast(new Intent(BLOODHOUND_RECEIVER_ACTION));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        Log.d(TAG, "onListItemClick(" + position + ")");

        Intent detailIntent = new Intent(this, MapDetailActivity.class);

        Uri locationUri = Uri.parse(LocationContentProvider.LOCATIONS_CONTENT_URI + "/" + id);
        detailIntent.putExtra(LocationContentProvider.CONTENT_ITEM_TYPE, locationUri);
        startActivity(detailIntent);
    }

    //start LoaderManager.LoaderCallbacks<Cursor>
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader()");

        return new CursorLoader(this, LocationContentProvider.LOCATIONS_DIFF_CONTENT_URI, LOCATIONS_DIFF_ALL_COLUMNS, null, null, LOCATIONS_DIFF_COLUMN_TIME2 + " desc");
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
    //end LoaderManager.LoaderCallbacks<Cursor>

    private void fillData() {
        String[] from = new String[] {
                LOCATIONS_DIFF_COLUMN_LATITUDE1,
                LOCATIONS_DIFF_COLUMN_LONGITUDE1,
                LOCATIONS_DIFF_COLUMN_LATITUDE2,
                LOCATIONS_DIFF_COLUMN_LONGITUDE2,
                LOCATIONS_DIFF_COLUMN_TIME2
        };

        int[] to = new int[] {
                R.id.latitude1TextView,
                R.id.longitude1TextView,
                R.id.latitude2TextView,
                R.id.longitude2TextView,
                R.id.timeTextView
        };

//        getLoaderManager().enableDebugLogging(true);
        getLoaderManager().initLoader(0, null, this);
        
        adapter = new LocationDiffCursorAdaptor(this, R.layout.location_diff_list_item, null, from, to, 0);
        adapter.setViewBinder(new LocationDiffViewBinder());

        setListAdapter(adapter);
    }

    private class LocationDiffViewBinder implements SimpleCursorAdapter.ViewBinder {
        @SuppressWarnings("UnusedDeclaration")
        private static final String TAG = "LocationDiffViewBinder";

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
//            Log.d(TAG, "setViewValue(" + view + ", " + cursor + ", " + columnIndex + ")");

            // 10 is the second recorded location time stamp in long
            if (columnIndex == 10 && view instanceof TextView) {
                ((TextView) view).setText(new Date(cursor.getLong(10)).toString());
                return true;
            }

            return false;
        }
    }
}
