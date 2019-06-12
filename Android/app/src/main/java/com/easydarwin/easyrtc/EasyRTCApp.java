package com.easydarwin.easyrtc;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

public class EasyRTCApp extends Application {
    public static Application context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        CrashReport.initCrashReport(getApplicationContext(), "479136a765", false);
    }
}
