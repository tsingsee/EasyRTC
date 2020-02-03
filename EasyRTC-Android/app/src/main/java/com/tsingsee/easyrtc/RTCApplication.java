package com.tsingsee.easyrtc;

import android.app.Application;
import android.content.Context;

import com.tencent.bugly.crashreport.CrashReport;

public class RTCApplication extends Application {
    private static Application context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        CrashReport.initCrashReport(getApplicationContext(), "479136a765", false);
    }

    public static Context getContext() {
        return context;
    }
}
