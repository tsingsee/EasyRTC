package org.easydarwin.easyrtc;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import org.easydarwin.android.aio.AudioIO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Created by john on 2017/4/9.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class AudioIOTest {
    AudioIO aio;
    Bus bus;
    private int state;

    @Before
    public void setUp() throws Exception {
        bus = new Bus(ThreadEnforcer.ANY);
        aio = new AudioIO(InstrumentationRegistry.getTargetContext(), bus, 8000, false);
        new File("/sdcard/nearendCanceled.pcm").delete();
        new File("/sdcard/nearend.pcm").delete();
    }

    @Subscribe
    public void onAudio(AudioIO.Nearend nearend) {
        if (state == 1)
            try {
                FileOutputStream fos = new FileOutputStream("/sdcard/nearend.pcm", true);
                fos.write(nearend.buffer);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Subscribe
    public void onAudio(AudioIO.NearendCanceled nearendCanceled) {
        if (state == 2)
            try {
                FileOutputStream fos = new FileOutputStream("/sdcard/nearendCanceled.pcm", true);
                fos.write(nearendCanceled.buffer);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Test
    public void pumpAudio() throws IOException, InterruptedException {
        aio.start();
        bus.register(this);
        state = 1;
        // 1、采集一段本地声音。
        Thread.sleep(20000);
        state = 2;
        // 2、将上面采集的声音播放一下。并继续采集本地声音。
        playAudio("/sdcard/nearend.pcm");
        state = 0;
        // 3、将消除后的声音播放一下。
        playAudio("/sdcard/nearendCanceled.pcm");
        bus.unregister(this);
        aio.release();
    }

    private void playAudio(String path) throws IOException, InterruptedException {
        ByteBuffer buffer = ByteBuffer.allocate(160);
        short[] pcm = new short[80];
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        FileInputStream fis = new FileInputStream(path);
        do {
            int i = fis.read(buffer.array(), buffer.position(), buffer.remaining());
            if (i < 0) {
                fis.close();
                break;
            }
            buffer.position(buffer.position() + i);

            buffer.flip();
            ShortBuffer shortBuffer = buffer.asShortBuffer();
            shortBuffer.clear();
            int remaining = shortBuffer.remaining();
            shortBuffer.get(pcm, 0, remaining);
            aio.pumpAudio(pcm, 0, remaining);
            buffer.clear();
        } while (true);
    }

    @After
    public void tearDown() throws Exception {
    }

}