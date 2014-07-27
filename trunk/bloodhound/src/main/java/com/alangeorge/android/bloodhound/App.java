package com.alangeorge.android.bloodhound;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    public static Context context;

    /**
     * Here we make a statically scoped public ApplicationContext available.
     * Currently this is used from the {@link com.alangeorge.android.bloodhound.BloodHoundService} to gain access to
     * the Applications ContentProvider
     */
    @Override public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}