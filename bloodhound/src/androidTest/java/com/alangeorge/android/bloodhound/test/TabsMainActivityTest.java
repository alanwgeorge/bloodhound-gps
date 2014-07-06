package com.alangeorge.android.bloodhound.test;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

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

    public void testPreConditions() {
        Log.d(TAG, "testPreConditions()");
//        assertTrue(mSpinner.getOnItemSelectedListener() != null);
//        assertTrue(mPlanetData != null);
//        assertEquals(mPlanetData.getCount(),ADAPTER_COUNT);
    }
}
