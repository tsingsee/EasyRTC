package org.easydarwin.easyrtc;

import android.app.Application;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import org.easydarwin.android.aio.AudioIO;

/**
 * Created by john on 2017/4/8.
 */

public class TheApp extends Application {
    public static AudioIO aio;
    public static Bus bus;
    @Override
    public void onCreate() {
        super.onCreate();
        bus = new Bus(ThreadEnforcer.ANY);
        aio = new AudioIO(this, bus, 8000, false);
    }
}
