package com.alangeorge.android.bloodhound;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import org.jetbrains.annotations.NotNull;

import static com.alangeorge.android.bloodhound.BloodHoundReceiver.BLOODHOUND_RECEIVER_ACTION;

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

}
