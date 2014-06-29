package com.alangeorge.android.bloodhound;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.jetbrains.annotations.NotNull;

import static com.alangeorge.android.bloodhound.BloodHoundReceiver.BLOODHOUND_RECEIVER_ACTION;
import static com.alangeorge.android.bloodhound.LocationContentProvider.LOCATIONS_CONTENT_URI;
import static com.alangeorge.android.bloodhound.MapDetailActivity.EXTRA_ACTION;
import static com.alangeorge.android.bloodhound.MapDetailActivity.EXTRA_START;
import static com.alangeorge.android.bloodhound.MapDetailActivity.MAP_ACTION_LOCATION;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_ID;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LATITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LONGITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME_STRING;

public class TabsMainActivity extends Activity implements ActionBar.OnNavigationListener {
    private static final String TAG = "TabsMainActivity";

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tabs_main);

        // Set up the action bar to show a dropdown list.
        @SuppressLint("AppCompatMethod")
        final ActionBar actionBar = getActionBar();
        //noinspection ConstantConditions
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(
                        actionBar.getThemedContext(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[]{
                                getString(R.string.title_section1),
                                getString(R.string.title_section2),
                        }
                ),
                this
        );
    }

    @Override
    public void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (!savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            return;
        }
        //noinspection ConstantConditions
        getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        // Serialize the current dropdown position.
        //noinspection ConstantConditions
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_start) {
            sendBroadcast(new Intent(BLOODHOUND_RECEIVER_ACTION));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        Fragment destination = null;

        switch (itemPosition) {
            case 0:
                destination = new LocationsFragment();
                break;
            case 1:
                destination = new LocationDiffFragment();
                break;
            default:
                throw new IllegalArgumentException("onNavigationItemSelected(" + itemPosition + "): invalid selection");
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.container, destination)
                .commit();

        return true;
    }

    public static class LocationsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

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

            return new CursorLoader(getActivity(), LOCATIONS_CONTENT_URI, projection, null, null, LOCATIONS_COLUMN_TIME + " desc");
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
}
