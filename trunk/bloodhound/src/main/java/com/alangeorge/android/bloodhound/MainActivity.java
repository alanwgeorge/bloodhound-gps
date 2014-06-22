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

import com.alangeorge.android.bloodhound.model.dao.LocationDao;

import static com.alangeorge.android.bloodhound.BloodHoundReceiver.BLOODHOUND_RECEIVER_ACTION;


@SuppressWarnings("WeakerAccess")
public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "MainActivity";

    private SimpleCursorAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        Uri locationUri = Uri.parse(LocationContentProvider.CONTENT_URI + "/" + id);
        detailIntent.putExtra(LocationContentProvider.CONTENT_ITEM_TYPE, locationUri);
        startActivity(detailIntent);
    }

    //start LoaderManager.LoaderCallbacks<Cursor>
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader()");
        String[] projection = {
                LocationDao.COLUMN_ID,
                LocationDao.COLUMN_LONGITUDE,
                LocationDao.COLUMN_LATITUDE,
                LocationDao.COLUMN_TIME_STRING,
                LocationDao.COLUMN_TIME
        };

        return new CursorLoader(this, LocationContentProvider.CONTENT_URI, projection, null, null, LocationDao.COLUMN_TIME + " desc");
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
                LocationDao.COLUMN_LATITUDE,
                LocationDao.COLUMN_LONGITUDE,
                LocationDao.COLUMN_TIME_STRING
        };

        int[] to = new int[] {
                R.id.latitudeTextView,
                R.id.longitudeTextView,
                R.id.timeTextView
        };

//        getLoaderManager().enableDebugLogging(true);
        getLoaderManager().initLoader(0, null, this);
        
        adapter = new SimpleCursorAdapter(this, R.layout.location_list_item, null, from, to, 0);

        setListAdapter(adapter);
    }
}
