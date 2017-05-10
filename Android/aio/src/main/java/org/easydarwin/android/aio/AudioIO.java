package org.easydarwin.android.aio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.LoudnessEnhancer;
import android.os.Build;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.webrtc.audio.MobileAEC;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class AudioIO {

    private static final String TAG = "AudioIO";
    private final Context mContext;
    private final boolean mStereo;
    private final int mSample;
    private final int mAudioFormat;
    private final Bus bus;
    private AudioEffect[] mAes = new AudioEffect[10];
    private AudioRecord mAudioRecoder;
    private MobileAEC aecm;


    private ByteBuffer buffer;
    private ShortBuffer nearendPCM;

    private PipedOutputStream os_farend;
    private PipedInputStream is_farend;
    private BlockingQueue<short[]> mFarendPCMs = new ArrayBlockingQueue<short[]>(100);

    private class AudioThread extends Thread {

        public AudioThread() {
            super("AudioIO");
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
            aecm = new MobileAEC(MobileAEC.SamplingFrequency.FS_8000Hz);
            int mode = PreferenceManager.getDefaultSharedPreferences(mContext).getInt("aec_mode", 1);
            try {
                aecm.setAecmMode(new MobileAEC.AggressiveMode(mode)).prepare();
                final int sampleRateInHz = mSample;
                // 10毫秒内的字节数
                final int unit_length = sampleRateInHz * 10 / 1000 * 2;
                if (bus != null) {
                    bus.register(this);
                    int CC = AudioFormat.CHANNEL_IN_MONO;
                    int minBufSize = AudioRecord.getMinBufferSize(sampleRateInHz, CC, mAudioFormat);
                    final int audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
                    // 初始化时，这个参数不是越小越好。这个参数(应该)是底层的音频buffer的尺寸，如果太小了，又读取不及时，可能会溢出，导致音质不好
                    minBufSize *= 1;
                    if (minBufSize < unit_length) {
                        minBufSize = unit_length;
                    }
                    mAudioRecoder = new AudioRecord(audioSource, sampleRateInHz, CC, mAudioFormat, minBufSize);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        int i = 0;
                        try {
                            AutomaticGainControl er = AutomaticGainControl.create(mAudioRecoder.getAudioSessionId());
                            if (er != null) {
                                er.setEnabled(true);
                                mAes[i++] = er;
                            }
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }
                    }
                    mAudioRecoder.startRecording();
                }
                int bufferSize = 320;
                int sizeInShorts = bufferSize / 2;
                buffer = ByteBuffer.allocate(bufferSize);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
//                short[] farendPCM = new short[sizeInShorts];

                nearendPCM = ShortBuffer.allocate(sizeInShorts);
                short[] nearendCanceled = new short[sizeInShorts];
                ByteBuffer bb = ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN);
                while (t != null) {
//                    int nmode = PreferenceManager.getDefaultSharedPreferences(mContext).getInt("aec_mode", 1);
//                    if (nmode != mode) {
//                        aecm.setAecmMode(new MobileAEC.AggressiveMode(nmode)).prepare();
//                        mode = nmode;
//                    }

                    long timeUS = System.nanoTime()/1000;
                    if (readNearendBuffer(nearendPCM)) {
                        bb.clear();
                        nearendPCM.clear();
                        bb.asShortBuffer().put(nearendPCM);
                        nearendPCM.clear();

                        short[] poll = mFarendPCMs.poll();
                        if (poll != null) {
                            aecm.farendBuffer(poll, sizeInShorts);
                            aecm.echoCancellation(nearendPCM.array(), null, nearendCanceled, (short) (sizeInShorts), (short) (20));
                            bb.clear();
                            bb.asShortBuffer().put(nearendCanceled);
                            bus.post(new NearendCanceled(bb.array(), timeUS));
                        } else {
                            bus.post(new Nearend(bb.array(), timeUS));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mAudioRecoder != null) {
                    mAudioRecoder.release();
                }
                if (bus != null){
                    bus.unregister(this);
                }
            }
        }

        @Subscribe
        public void onFarendPCM(ByteBuffer farend){
            short []farendPCM = new short[160];
            ShortBuffer buffer = farend.asShortBuffer();
            buffer.get(farendPCM);
            mFarendPCMs.offer(farendPCM);
        }

        private MobileAEC.AggressiveMode getMode() {
            try {
//                String[] arr = AddVideoOverlay.AddText.split("_");
                // 如果为偶数，不读远端了。
                int delay_level = 1;
                return new MobileAEC.AggressiveMode(delay_level);
            } catch (Exception ex) {
                return MobileAEC.AggressiveMode.HIGH;
            }
        }


        private int getDelay() {
            try {
//                String[] arr = AddVideoOverlay.AddText.split("_");
                // 如果为偶数，不读远端了。
//                int delay_level = Integer.parseInt(10);
                return 10;
            } catch (Exception ex) {
                return 1;
            }
        }

        private boolean readNearendBuffer(ShortBuffer pcm) {
            if (!pcm.hasRemaining()) {
                return true;
            }
            int remaining = pcm.remaining();
            int i = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                i = mAudioRecoder.read(pcm.array(), pcm.position(), remaining, AudioRecord.READ_NON_BLOCKING);
            }else{
                i = mAudioRecoder.read(pcm.array(), pcm.position(), remaining);
            }
            if (i < 0 || t == null) {
                pcm.clear();
                return false;
            }
            pcm.position(pcm.position() + i);
            return !pcm.hasRemaining();
        }

        private boolean fillFarendBuffer(ByteBuffer bufferPCM) throws IOException {
//            int state = mAudioTrack.getPlayState();
            int capacity = bufferPCM.remaining();
            int available = is_farend.available();
            boolean hasAvailable = available >= capacity;
            if (hasAvailable) {
                int i = is_farend.read(bufferPCM.array(), bufferPCM.position(), capacity);
                if (i < 0 || t == null) return false;
                bufferPCM.position(bufferPCM.position() + i);
                return !bufferPCM.hasRemaining();
            } else {
                return false;
            }
        }
    }

    private Thread t;
    private List<Thread> hosts = new ArrayList<>();

    public AudioIO(Context context, Bus bus, int sample, boolean stereo) {
        this(context, bus, sample, stereo, AudioFormat.ENCODING_PCM_16BIT);
    }

    public AudioIO(Context context, Bus bus, int sample, boolean stereo, int audioFormat) {
        mContext = context.getApplicationContext();
        mSample = sample;
        mStereo = stereo;
        mAudioFormat = audioFormat;
        this.bus = bus;
    }

    public synchronized void start() throws IOException {
        if (hosts.contains(Thread.currentThread())){
            return;
        }
        hosts.add(Thread.currentThread());
        if (t != null && t.isAlive()) {
            return;
        }
        is_farend = new PipedInputStream(16000);
        os_farend = new PipedOutputStream(is_farend);
        t = new AudioThread();
        t.start();
    }

    public synchronized void release() throws IOException, InterruptedException {
        if (!hosts.contains(Thread.currentThread())){
            return;
        }
        hosts.remove(Thread.currentThread());
        if (hosts.isEmpty()) {
            if (is_farend != null) {
                is_farend.close();
            }
            if (os_farend != null)
                os_farend.close();
            Thread t = this.t;
            this.t = null;
            if (t != null) {
                t.interrupt();
                t.join();
            }
        }
    }

    public void pumpAudio(short[] pcm, int offset, int length) throws InterruptedException, IOException {
        ByteBuffer bb = ByteBuffer.allocate(length * 2).order(ByteOrder.LITTLE_ENDIAN);
        ShortBuffer sb = bb.asShortBuffer();
        sb.put(pcm, offset, length);

        os_farend.write(bb.array());
    }


    public static void save(byte[] buffer, String path, boolean append) {
        if (true) return;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path, append);
            fos.write(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Nearend {
        public final byte[] buffer;
        public final long timeUS;

        public Nearend(byte[] buffer, long timeUS) {
            this.buffer = buffer;
            this.timeUS = timeUS;
        }
    }

    public static class NearendCanceled extends Nearend {
        public NearendCanceled(byte[] buffer, long timeUS) {
            super(buffer, timeUS);
        }
    }

}
