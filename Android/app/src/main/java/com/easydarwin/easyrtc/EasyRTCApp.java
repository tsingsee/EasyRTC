package com.easydarwin.easyrtc;

import android.app.Application;

public class EasyRTCApp extends Application {
    public static Application context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
