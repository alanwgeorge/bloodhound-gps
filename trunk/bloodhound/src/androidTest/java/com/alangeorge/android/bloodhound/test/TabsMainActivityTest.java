package com.alangeorge.android.bloodhound.test;

import android.app.Fragment;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.alangeorge.android.bloodhound.LocationDiffFragment;
import com.alangeorge.android.bloodhound.LocationsFragment;
import com.alangeorge.android.bloodhound.R;
import com.alangeorge.android.bloodhound.TabsMainActivity;

public class TabsMainActivityTest extends ActivityInstrumentationTestCase2<TabsMainActivity> {
    private static final String TAG = "TabsMainActivityTest";

    private TabsMainActivity tabsMainActivity;


    public TabsMainActivityTest() {
        super(TabsMainActivity.class);
        Log.d(TAG, "TabsMainActivityTest()");
    }

    public TabsMainActivityTest(Class<TabsMainActivity> activityClass) {
        super(activityClass);
        Log.d(TAG, "TabsMainActivityTest(class)");
    }

    @Override
    public void setUp() throws Exception {
        Log.d(TAG, "setUp()");
        super.setUp();

        setActivityInitialTouchMode(false);

        tabsMainActivity = getActivity();
    }

    public void testFragmentTransition() {
        Log.d(TAG, "testFragmentTransition()");

        getInstrumentation().waitForIdleSync();

        View container = tabsMainActivity.findViewById(R.id.container);

        assertTrue(container instanceof FrameLayout);

        tabsMainActivity.getFragmentManager().beginTransaction().replace(R.id.container, new LocationDiffFragment()).commit();

        Fragment fragment = tabsMainActivity.getFragmentManager().findFragmentById(R.id.container);

        assertTrue("fragment == null", fragment != null);
        assertTrue("fragment not LocationsFragment", fragment instanceof LocationsFragment);

        tabsMainActivity.onNavigationItemSelected(1, 1);

        getInstrumentation().waitForIdleSync();

        fragment = tabsMainActivity.getFragmentManager().findFragmentById(R.id.container);

        assertTrue("fragment == null", fragment != null);
        assertTrue("fragment not LocationDiffFragment", fragment instanceof LocationDiffFragment);

        tabsMainActivity.onNavigationItemSelected(0, 0);

        getInstrumentation().waitForIdleSync();

        fragment = tabsMainActivity.getFragmentManager().findFragmentById(R.id.container);

        assertTrue("fragment == null", fragment != null);
        assertTrue("fragment not LocationsFragment", fragment instanceof LocationsFragment);

    }
}
