package com.alangeorge.android.bloodhound.test;

import android.support.v4.app.Fragment;
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

        tabsMainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.container, new LocationDiffFragment()).commit();

        Fragment fragment = tabsMainActivity.getSupportFragmentManager().findFragmentById(R.id.container);

        assertTrue("fragment == null", fragment != null);
        assertTrue("fragment not LocationsFragment", fragment instanceof LocationsFragment);

        tabsMainActivity.onNavigationItemSelected(1, 1);

        getInstrumentation().waitForIdleSync();

        fragment = tabsMainActivity.getSupportFragmentManager().findFragmentById(R.id.container);

        assertTrue("fragment == null", fragment != null);
        assertTrue("fragment not LocationDiffFragment", fragment instanceof LocationDiffFragment);

        tabsMainActivity.onNavigationItemSelected(0, 0);

        getInstrumentation().waitForIdleSync();

        fragment = tabsMainActivity.getSupportFragmentManager().findFragmentById(R.id.container);

        assertTrue("fragment == null", fragment != null);
        assertTrue("fragment not LocationsFragment", fragment instanceof LocationsFragment);

    }

    public void testSaveInstance() {
        Log.d(TAG, "testSaveInstance()");

        tabsMainActivity.onNavigationItemSelected(1, 1);
        getInstrumentation().waitForIdleSync();

        Fragment fragment = tabsMainActivity.getSupportFragmentManager().findFragmentById(R.id.container);

        assertTrue("fragment == null", fragment != null);
        assertTrue("fragment not LocationDiffFragment", fragment instanceof LocationDiffFragment);

        Log.d(TAG, "calling finish() 1");
        tabsMainActivity.finish();
        tabsMainActivity = getActivity();
        getInstrumentation().waitForIdleSync();

       fragment = tabsMainActivity.getSupportFragmentManager().findFragmentById(R.id.container);

        assertTrue("fragment == null", fragment != null);
        assertTrue("fragment not LocationDiffFragment", fragment instanceof LocationDiffFragment);

        getInstrumentation().waitForIdleSync();
        tabsMainActivity.onNavigationItemSelected(0, 0);
        getInstrumentation().waitForIdleSync();

        Log.d(TAG, "calling finish() 2");
        tabsMainActivity.finish();
        getInstrumentation().waitForIdleSync();
        tabsMainActivity = getActivity();

        fragment = tabsMainActivity.getSupportFragmentManager().findFragmentById(R.id.container);

        assertTrue("fragment == null", fragment != null);
        assertTrue("fragment not LocationsFragment", fragment instanceof LocationsFragment);
    }

    @Override
    protected void tearDown() throws Exception {
        Log.d(TAG, "tearDown()");
        super.tearDown();
    }
}
