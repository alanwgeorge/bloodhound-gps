package com.alangeorge.android.bloodhound;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

class ServiceThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(@SuppressWarnings("NullableProblems") Runnable runnable) {
        ThreadFactory wrappedFactory = Executors.defaultThreadFactory();

        Thread thread = wrappedFactory.newThread(runnable);

        thread.setName("BloodHoundServiceThread");

        return thread;
    }
}
