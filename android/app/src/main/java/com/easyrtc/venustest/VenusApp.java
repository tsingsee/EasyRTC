package com.easyrtc.venustest;

import android.app.Application;

public class VenusApp extends Application {
    public static Application context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
