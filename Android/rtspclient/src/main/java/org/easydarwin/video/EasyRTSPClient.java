package org.easydarwin.video;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.LoudnessEnhancer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;


import com.android.webrtc.audio.MobileAEC;
import com.squareup.otto.Bus;

import junit.framework.Assert;

import org.easydarwin.android.aio.AudioIO;
import org.easydarwin.audio.AudioCodec;
import org.easydarwin.util.CodecSpecificDataUtil;
import org.easydarwin.util.ParsableBitArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.easydarwin.util.CodecSpecificDataUtil.AUDIO_SPECIFIC_CONFIG_SAMPLING_RATE_TABLE;

/**
 * Created by John on 2016/3/17.
 */
public class EasyRTSPClient implements RTSPClient.RTSPSourceCallBack {

    private static final long LEAST_FRAME_INTERVAL = 10000l;

    /* 视频编码 */
    public static final int EASY_SDK_VIDEO_CODEC_H264 = 0x1C;		/* H264  */
    public static final int EASY_SDK_VIDEO_CODEC_MJPEG = 0x08;/* MJPEG */
    public static final int EASY_SDK_VIDEO_CODEC_MPEG4 = 0x0D;/* MPEG4 */

    /* 音频编码 */
    public static final int EASY_SDK_AUDIO_CODEC_AAC = 0x15002;		/* AAC */
    public static final int EASY_SDK_AUDIO_CODEC_G711U = 0x10006;		/* G711 ulaw*/
    public static final int EASY_SDK_AUDIO_CODEC_G711A = 0x10007;	/* G711 alaw*/
    public static final int EASY_SDK_AUDIO_CODEC_G726 = 0x1100B;	/* G726 */

    /**
     * 表示视频显示出来了
     */
    public static final int RESULT_VIDEO_DISPLAYED = 01;
    /**
     * 表示视频的尺寸获取到了。具体尺寸见 EXTRA_VIDEO_WIDTH、EXTRA_VIDEO_HEIGHT
     */
    public static final int RESULT_VIDEO_SIZE = 02;
    /**
     * 表示KEY的可用播放时间已用完
     */
    public static final int RESULT_TIMEOUT = 03;
    /**
     * 表示KEY的可用播放时间已用完
     */
    public static final int RESULT_EVENT = 04;
    public static final int RESULT_UNSUPPORTED_VIDEO = 05;
    public static final int RESULT_UNSUPPORTED_AUDIO = 06;
    public static final int RESULT_RECORD_BEGIN = 7;
    public static final int RESULT_RECORD_END = 8;

    private static final String TAG = EasyRTSPClient.class.getSimpleName();
    /**
     * 表示视频的宽度
     */
    public static final String EXTRA_VIDEO_WIDTH = "extra-video-width";
    /**
     * 表示视频的高度
     */
    public static final String EXTRA_VIDEO_HEIGHT = "extra-video-height";


    private final String mKey;
    private final SurfaceTexture mTexture;
    private final Bus bus;
    private Surface mSurface;
    private volatile Thread mThread, mAudioThread;
    private final ResultReceiver mRR;
    private RTSPClient mClient;
    private boolean mAudioEnable = true;
    private volatile long mReceivedDataLength;
    private String mRecordingPath;
    private Object mObject;
    private int mMuxerVideoTrack = -1, mMuxerAudioTrack = -1;
    private RTSPClient.MediaInfo mMediaInfo;
    private short mHeight = 0;
    short mWidth = 0;
    private ByteBuffer mCSD0;
    private ByteBuffer mCSD1;

//    private RtmpClient mRTMPClient = new RtmpClient();

    public boolean isRecording() {
        return !TextUtils.isEmpty(mRecordingPath);
    }

    private static class FrameInfoQueue extends PriorityQueue<RTSPClient.FrameInfo> {
        public static final int CAPACITY = 500;
        public static final int INITIAL_CAPACITY = 300;
        private long previewAudioStampUs, previewVideoStampUS;

        public FrameInfoQueue() {
            super(INITIAL_CAPACITY, new Comparator<RTSPClient.FrameInfo>() {

                @Override
                public int compare(RTSPClient.FrameInfo frameInfo, RTSPClient.FrameInfo t1) {
                    return (int) (frameInfo.stamp - t1.stamp);
                }
            });
        }

        final ReentrantLock lock = new ReentrantLock();
        final Condition notFull = lock.newCondition();
        final Condition notVideo = lock.newCondition();
        final Condition notAudio = lock.newCondition();

        @Override
        public int size() {
            lock.lock();
            try {
                return super.size();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void clear() {
            lock.lock();
            try {
                int size = super.size();
                super.clear();
                int k = size;
                for (; k > 0 && lock.hasWaiters(notFull); k--)
                    notFull.signal();
            } finally {
                lock.unlock();
            }
        }

        public void put(RTSPClient.FrameInfo x) throws InterruptedException {
            lock.lockInterruptibly();
            try {
                int size;
                while ((size = super.size()) == CAPACITY) {
                    Log.v(TAG, "queue full:" + CAPACITY);
                    notFull.await();
                }
                offer(x);
//                Log.d(TAG, String.format("queue size : " + size));
                // 这里是乱序的。并非只有空的queue才丢到首位。因此不能做限制 if (size == 0)
                {

                    if (x.audio) {
                        notAudio.signal();
                    } else {
                        notVideo.signal();
                    }
                }

            } finally {
                lock.unlock();
            }
        }

        public RTSPClient.FrameInfo takeVideoFrame() throws InterruptedException {
            lock.lockInterruptibly();
            try {
                while (true) {
                    RTSPClient.FrameInfo x = peek();
                    if (x == null) {
                        notVideo.await();
                    } else {
                        if (!x.audio) {
                            remove();
                            notFull.signal();
                            notAudio.signal();
                            Log.d(TAG, "video " + x.stamp + " take[" + (x.stamp - previewVideoStampUS) + "]");
                            previewVideoStampUS = x.stamp;
                            if (previewVideoStampUS < previewAudioStampUs) {
                                Log.w(TAG, String.format("previewVideoStampUS < previewAudioStampUs!%d,%d", previewVideoStampUS, previewAudioStampUs));
                            }
                            return x;
                        } else {
                            notVideo.await();
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        public RTSPClient.FrameInfo takeVideoFrame(long ms) throws InterruptedException {
            lock.lockInterruptibly();
            try {
                while (true) {
                    RTSPClient.FrameInfo x = peek();
                    if (x == null) {
                        if (!notVideo.await(ms, TimeUnit.MILLISECONDS)) return null;
                    } else {
                        if (!x.audio) {
                            remove();
                            notFull.signal();
                            notAudio.signal();
                            Log.d(TAG, "video " + x.stamp + " take[" + (x.stamp - previewVideoStampUS) + "]");
                            previewVideoStampUS = x.stamp;
                            if (previewVideoStampUS < previewAudioStampUs) {
                                Log.w(TAG, String.format("previewVideoStampUS < previewAudioStampUs!%d,%d", previewVideoStampUS, previewAudioStampUs));
                            }
                            return x;
                        } else {
                            notVideo.await();
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        public RTSPClient.FrameInfo takeAudioFrame() throws InterruptedException {
            lock.lockInterruptibly();
            try {
                while (true) {
                    RTSPClient.FrameInfo x = peek();
                    if (x == null) {
                        notAudio.await();
                    } else {
                        if (x.audio) {
                            remove();
                            notFull.signal();
                            notVideo.signal();
                            Log.d(TAG, "audio " + x.stamp + " take[" + (x.stamp - previewAudioStampUs) + "]");
                            previewAudioStampUs = x.stamp;
                            if (previewAudioStampUs < previewVideoStampUS) {
                                Log.w(TAG, String.format("previewAudioStampUs < previewVideoStampUS!%d,%d", previewAudioStampUs, previewVideoStampUS));
                            }
                            return x;
                        } else {
                            notAudio.await();
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private FrameInfoQueue mQueue = new FrameInfoQueue();


    private final Context mContext;
    /**
     * 最新的视频时间戳
     */
    private long mNewestStample,mNewestVideoUS,mNewestAudioUS;
    private boolean mWaitingKeyFrame;
    private boolean mTimeout;
    private boolean mNotSupportedVideoCB, mNotSupportedAudioCB;

    /**
     * 创建SDK对象
     *
     * @param context 上下文对象
     * @param key     SDK key
     * @param surface 显示视频用的surface
     */
    public EasyRTSPClient(Context context, String key, SurfaceTexture surface, Bus bus, ResultReceiver receiver) {
        mTexture = surface;
        mSurface = new Surface(surface);
        mContext = context;
        mKey = key;
        mRR = receiver;
        this.bus = bus;
    }


    /**
     * 启动播放
     *
     * @param url
     * @param type
     * @param mediaType
     * @param user
     * @param pwd
     * @return
     */
    public int start(final String url, int type, int mediaType, String user, String pwd) {
        return start(url, type, mediaType, user, pwd, null);
    }

    /**
     * 启动播放
     *
     * @param url
     * @param type
     * @param mediaType
     * @param user
     * @param pwd
     * @return
     */
    public int start(final String url, int type, int mediaType, String user, String pwd, String recordPath) {
        if (url == null) {
            throw new NullPointerException("url is null");
        }
        mNewestStample = 0;
        mWaitingKeyFrame = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("waiting_i_frame", true);
        mWidth = mHeight = 0;
        mQueue.clear();
        startCodec();
        startAudio();
        mTimeout = false;
        mNotSupportedVideoCB = mNotSupportedAudioCB = false;
        mReceivedDataLength = 0;
        mClient = new RTSPClient(mContext, mKey);
        int channel = mClient.registerCallback(this);
        mRecordingPath = recordPath;
        Log.i(TAG, String.format("playing url:\n%s\n", url));
        return mClient.openStream(channel, url, type, mediaType, user, pwd);
    }

    /**
     * 终止播放
     */
    public void stop() {
        Thread t = mThread;
        mThread = null;
        t.interrupt();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        t = mAudioThread;
        mAudioThread = null;
        t.interrupt();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stopRecord();


        mQueue.clear();
        if (mClient != null) {
            mClient.unrigisterCallback(this);
            mClient.closeStream();
            try {
                mClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mQueue.clear();
        mClient = null;
        mNewestStample = 0;
    }

    private void startAudio() {
        mAudioThread = new Thread("AUDIO_CONSUMER") {

            public AudioManager.OnAudioFocusChangeListener l;
            public Object[] mAes = new Object[10];
            public AudioTrack mAudioTrack;

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                {
                    RTSPClient.FrameInfo frameInfo;
                    long handle = 0;

                    try {
                        initAudioTrack();

                        frameInfo = mQueue.takeAudioFrame();
                        long previewStampUs = frameInfo.stamp;
                        handle = AudioCodec.create(frameInfo.codec, frameInfo.sample_rate, frameInfo.channels, 16);

                        short[] tmp = new short[8000];
                        // 半秒钟的数据缓存
                        byte[] mBufferReuse = new byte[16000];
                        int[] outLen = new int[1];
                        while (mAudioThread != null) {
                            if (frameInfo == null) {
                                frameInfo = mQueue.takeAudioFrame();
                                if (frameInfo != null) {
                                    previewStampUs = frameInfo.stamp;
                                }
                            }
                            outLen[0] = mBufferReuse.length;
                            long ms = SystemClock.currentThreadTimeMillis();
                            int nRet = AudioCodec.decode((int) handle, frameInfo.buffer, 0, frameInfo.length, mBufferReuse, outLen);
                            if (nRet == 0) {

//                                mAudioTrack.write(mBufferReuse, 0, outLen[0]);

//                                ByteBuffer buffer = ByteBuffer.wrap(mBufferReuse);
//                                buffer.order(ByteOrder.LITTLE_ENDIAN);
//                                ShortBuffer sb = buffer.asShortBuffer();
//                                sb.get(tmp);
//                                aio.pumpAudio(tmp, 0, outLen[0]/2);
//
                                ByteBuffer buffer = ByteBuffer.wrap(mBufferReuse, 0, 0);
                                buffer.order(ByteOrder.LITTLE_ENDIAN);
                                do {
                                    buffer.limit(buffer.limit() + 320);
                                    if (buffer.position() >= outLen[0]) {
                                        break;
                                    }
                                    long begin = System.currentTimeMillis();
                                    int r;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        r = mAudioTrack.write(buffer.array(), buffer.position(), buffer.remaining(), AudioTrack.WRITE_NON_BLOCKING);
                                    } else {
                                        r = mAudioTrack.write(buffer.array(), buffer.position(), buffer.remaining());
                                    }
                                    Log.d(TAG, String.format("mAudioTrack write ret:%d,spend:%d", r, System.currentTimeMillis() - begin));
                                    if (bus != null) {
                                        bus.post(buffer);
                                    }
                                    buffer.position(buffer.limit());
                                } while (mAudioTrack != null);
                            }
                            frameInfo = null;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        if (handle != 0) {
                            AudioCodec.close((int) handle);
                        }
                        deleteAudioTrack();
                    }
                }
            }

            private void deleteAudioTrack() {
                for (Object ae : mAes) {
                    if (ae != null) {
                        AudioEffect aet = (AudioEffect) ae;
                        aet.release();
                    }
                }
                mAudioTrack.release();
                AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                am.abandonAudioFocus(l);
            }

            private void initAudioTrack() {
                AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                l = new AudioManager.OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int focusChange) {
                        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                            AudioTrack audioTrack = mAudioTrack;
                            if (audioTrack != null) {
                                audioTrack.setStereoVolume(1.0f, 1.0f);
                                if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
                                    audioTrack.flush();
                                    audioTrack.play();
                                }
                            }
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            AudioTrack audioTrack = mAudioTrack;
                            if (audioTrack != null) {
                                if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                                    audioTrack.pause();
                                }
                            }
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                            AudioTrack audioTrack = mAudioTrack;
                            if (audioTrack != null) {
                                audioTrack.setStereoVolume(0.5f, 0.5f);
                            }
                        }
                    }
                };

                int requestCode = am.requestAudioFocus(l, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);

                if (requestCode != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    Log.w(TAG, String.format("requestAudioFocus result:%d", requestCode));
                }

                final int sampleRateInHz = 8000;
                final int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
                final int bfSize = (int) (AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, AudioFormat.ENCODING_PCM_16BIT));
                am.setSpeakerphoneOn(true);
                // 10毫秒内的字节数
                final int unit_length = sampleRateInHz * 10 / 1000 * 2;

                mAudioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRateInHz, channelConfig,
                        AudioFormat.ENCODING_PCM_16BIT, bfSize * 4, AudioTrack.MODE_STREAM);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    int i = 0;
                    try {
                        EnvironmentalReverb er = new EnvironmentalReverb(0, mAudioTrack.getAudioSessionId());
                        if (er != null) {
                            er.setEnabled(true);
                            mAes[i++] = er;
                        }
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                    try {
                        LoudnessEnhancer le = new LoudnessEnhancer(mAudioTrack.getAudioSessionId());
                        le.setEnabled(true);
                        mAes[i++] = le;
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                }
                mAudioTrack.play();
            }
        };

        mAudioThread.start();
    }


    private static void save2path(byte[] buffer, int offset, int length, String path, boolean append) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path, append);
            fos.write(buffer, offset, length);
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

    private static int getXPS(byte[] data, int offset, int length, byte[] dataOut, int[] outLen, int type) {
        int i;
        int pos0;
        int pos1;
        pos0 = -1;
        for (i = offset; i < length - 4; i++) {
            if ((0 == data[i]) && (0 == data[i + 1]) && (1 == data[i + 2]) && (type == (0x0F & data[i + 3]))) {
                pos0 = i;
                break;
            }
        }
        if (-1 == pos0) {
            return -1;
        }
        pos1 = -1;
        for (i = pos0 + 4; i < length - 4; i++) {
            if ((0 == data[i]) && (0 == data[i + 1]) && (1 == data[i + 2])) {
                pos1 = i;
                break;
            }
        }
        if (-1 == pos1 || pos1 == 0) {
            return -2;
        }
        if (data[pos1 - 1] == 0) {
            pos1 -= 1;
        }
        if (pos1 - pos0 + 1 > outLen[0]) {
            return -3; // 输入缓冲区太小
        }
        dataOut[0] = 0;
        System.arraycopy(data, pos0, dataOut, 1, pos1 - pos0);
        // memcpy(pXPS+1, pES+pos0, pos1-pos0);
        // *pMaxXPSLen = pos1-pos0+1;
        outLen[0] = pos1 - pos0 + 1;
        return pos1;
    }

    private static boolean codecMatch(String mimeType, MediaCodecInfo codecInfo) {
        String[] types = codecInfo.getSupportedTypes();
        for (String type : types) {
            if (type.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }

    private static String codecName() {
        ArrayList<String> array = new ArrayList<>();
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i1 = 0; i1 < numCodecs; i1++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i1);
            if (codecInfo.isEncoder()) {
                continue;
            }

            if (codecMatch("video/avc", codecInfo)) {
                String name = codecInfo.getName();
                Log.d("DECODER", String.format("decoder:%s", name));
                array.add(name);
            }
        }
//        if (array.remove("OMX.qcom.video.decoder.avc")) {
//            array.add("OMX.qcom.video.decoder.avc");
//        }
//        if (array.remove("OMX.amlogic.avc.decoder.awesome")) {
//            array.add("OMX.amlogic.avc.decoder.awesome");
//        }
        if (array.isEmpty()) {
            return "";
        }
        return array.get(0);
    }

    private void startCodec() {
        final int delayUS = PreferenceManager.getDefaultSharedPreferences(mContext).getInt("delayUs", 0);
        mThread = new Thread("VIDEO_CONSUMER") {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                MediaCodec mCodec = null;
                VideoCodec.VideoDecoderLite mDecoder = null;
                try {
                    boolean pushBlankBuffersOnStop = true;

                    int index = 0;
                    long previewStampUs = 0l;
                    long previewTickUs = 0l;

                    long previewStampUs1 = 0;
                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    while (mThread != null) {
                        RTSPClient.FrameInfo frameInfo;
                        if (mCodec == null && mDecoder == null) {
                            frameInfo = mQueue.takeVideoFrame();
                            try {

                                MediaFormat format = MediaFormat.createVideoFormat("video/avc", mWidth, mHeight);
                                format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
                                format.setInteger(MediaFormat.KEY_PUSH_BLANK_BUFFERS_ON_STOP, pushBlankBuffersOnStop ? 1 : 0);
                                if (mCSD0 != null) {
                                    format.setByteBuffer("csd-0", mCSD0);
                                } else {
                                    throw new InvalidParameterException("csd-0 is invalid.");
                                }
                                if (mCSD1 != null) {
                                    format.setByteBuffer("csd-1", mCSD1);
                                } else {
                                    throw new InvalidParameterException("csd-1 is invalid.");
                                }
                                MediaCodec codec = MediaCodec.createDecoderByType("video/avc");
                                Log.i(TAG, String.format("config codec:%s", format));
                                codec.configure(format, mSurface, null, 0);
                                codec.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                                codec.start();
                                mCodec = codec;
                            } catch (Throwable e) {
                                Log.e(TAG, String.format("init codec error due to %s", e.getMessage()));
                                e.fillInStackTrace();
                                final VideoCodec.VideoDecoderLite decoder = new VideoCodec.VideoDecoderLite();
                                decoder.create(frameInfo.width, frameInfo.height, mSurface);
                                mDecoder = decoder;
                            }
                            previewTickUs = mTexture.getTimestamp();
//                            index = mCodec.dequeueInputBuffer(0);
//                            if (index >= 0) {
//                                ByteBuffer buffer = mCodec.getInputBuffers()[index];
//                                buffer.clear();
//                                mCSD0.clear();
//                                mCSD1.clear();
//                                buffer.put(mCSD0.array(), 0, mCSD0.remaining());
//                                buffer.put(mCSD1.array(), 0, mCSD1.remaining());
//                                mCodec.queueInputBuffer(index, 0, buffer.position(), 0, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
//                            }
                        } else {
                            frameInfo = mQueue.takeVideoFrame(5);
                        }
                        if (frameInfo != null) {
                            Log.d(TAG, "video " + frameInfo.stamp + " take[" + (frameInfo.stamp - previewStampUs1) + "]");
                            previewStampUs1 = frameInfo.stamp;
                        }

                        if (mDecoder != null) {
                            long decodeBegin = System.currentTimeMillis();
                            mDecoder.decodeAndSnapAndDisplay(frameInfo);
                            long decodeSpend = System.currentTimeMillis() - decodeBegin;

                            boolean firstFrame = previewStampUs == 0l;
                            if (firstFrame) {
                                Log.i(TAG, String.format("POST VIDEO_DISPLAYED!!!"));
                                ResultReceiver rr = mRR;
                                if (rr != null) rr.send(RESULT_VIDEO_DISPLAYED, null);
                            }
                            long current = frameInfo.stamp;

                            if (previewStampUs != 0l) {
                                long sleepTime = current - previewStampUs - decodeSpend * 1000;
                                if (sleepTime > 0) {
                                    long cache = mNewestVideoUS - frameInfo.stamp;
                                    sleepTime = fixSleepTime(sleepTime * 1000, cache, 0);
                                    if (sleepTime > 0) {
                                        Thread.sleep(sleepTime / 1000);
                                    }
                                }
                            }
                            previewStampUs = current;
                        } else {
                            do {
                                if (frameInfo != null) {
                                    byte[] pBuf = frameInfo.buffer;
                                    pumpSample(frameInfo);
                                    index = mCodec.dequeueInputBuffer(10);
                                    if (index >= 0) {
                                        ByteBuffer buffer = mCodec.getInputBuffers()[index];
                                        buffer.clear();
                                        if (pBuf.length > buffer.remaining()) {
                                            mCodec.queueInputBuffer(index, 0, 0, frameInfo.stamp, 0);
                                        } else {
                                            buffer.put(pBuf, frameInfo.offset, frameInfo.length);
                                            mCodec.queueInputBuffer(index, 0, buffer.position(), frameInfo.stamp, 0);
                                        }
                                        frameInfo = null;
                                    }
                                }
                                index = mCodec.dequeueOutputBuffer(info, 10); //
                                switch (index) {
                                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                                        Log.i(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                                        break;
                                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                                        Log.i(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
                                        break;
                                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                                        // 输出为空
                                        break;
                                    default:
                                        // 输出队列不为空
                                        // -1表示为第一帧数据
                                        long newSleepUs = -1;
                                        boolean firstTime = previewStampUs == 0l;
                                        if (!firstTime) {
                                            long sleepUs = (info.presentationTimeUs - previewStampUs);
                                            if (sleepUs > 1000000) {
                                                // 时间戳异常，可能服务器丢帧了。
                                                newSleepUs = 0l;
                                            } else {
                                                long cache = mNewestVideoUS - info.presentationTimeUs;
                                                newSleepUs = fixSleepTime(sleepUs, cache, 200000);
                                                Log.d(TAG, String.format("sleepUs:%d,newSleepUs:%d,Cache:%d", sleepUs, newSleepUs, cache));
                                            }
                                        }
                                        previewStampUs = info.presentationTimeUs;

                                        if (false && Build.VERSION.SDK_INT >= 21) {
                                            Log.d(TAG, String.format("releaseoutputbuffer:%d,stampUs:%d", index, previewStampUs));
                                            mCodec.releaseOutputBuffer(index, previewStampUs);
                                        } else {
                                            if (newSleepUs < 0) {
                                                newSleepUs = 0;
                                            }
                                            Thread.sleep(newSleepUs / 1000);
                                            mCodec.releaseOutputBuffer(index, true);
                                        }
                                        if (firstTime) {
                                            Log.i(TAG, String.format("POST VIDEO_DISPLAYED!!!"));
                                            ResultReceiver rr = mRR;
                                            if (rr != null) rr.send(RESULT_VIDEO_DISPLAYED, null);
                                        }
                                }
                            } while (frameInfo != null || index < MediaCodec.INFO_TRY_AGAIN_LATER);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (mCodec != null) {
                        mCodec.stop();
                        mCodec.release();
                    }
                    if (mDecoder != null) {
                        mDecoder.close();
                    }
                }
            }
        };
        mThread.start();
    }

    private static final long fixSleepTime(long sleepTimeUs, long totalTimestampDifferUs, long delayUs) {
        double dValue = ((double) (delayUs - totalTimestampDifferUs)) / 1000000d;
        double radio = Math.exp(dValue);
        final double r = sleepTimeUs * radio + 0.5f;
        return (long) r;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public synchronized void startRecord(String path) {
        if (mMediaInfo == null || mWidth == 0 || mHeight == 0 || mCSD0 == null || mCSD1 == null)
            return;
        mRecordingPath = path;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                MediaMuxer muxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                MediaFormat format = new MediaFormat();
                format.setInteger(MediaFormat.KEY_WIDTH, mWidth);
                format.setInteger(MediaFormat.KEY_HEIGHT, mHeight);
                mCSD0.clear();
                format.setByteBuffer("csd-0", mCSD0);
                mCSD1.clear();
                format.setByteBuffer("csd-1", mCSD1);
                format.setString(MediaFormat.KEY_MIME, "video/avc");


                Log.i(TAG, String.format("addTrack video track :%s", format));
                mMuxerVideoTrack = muxer.addTrack(format);


                format = new MediaFormat();
                int sampleRateIndex = getSampleIndex(mMediaInfo.sample);
                int channelConfig = mMediaInfo.channel;
                if (mMediaInfo.audioCodec == EASY_SDK_AUDIO_CODEC_AAC) {
                    int audioObjectType = 2;
                    byte[] audioSpecificConfig = CodecSpecificDataUtil.buildAacAudioSpecificConfig(audioObjectType, sampleRateIndex, channelConfig);
                    Pair<Integer, Integer> audioParams = CodecSpecificDataUtil.parseAacAudioSpecificConfig(audioSpecificConfig);
//                                format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
                    format.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC);
                    format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, audioParams.second);
                    format.setInteger(MediaFormat.KEY_SAMPLE_RATE, audioParams.first);

                    List<byte[]> bytes = Collections.singletonList(audioSpecificConfig);
                    for (int j = 0; j < bytes.size(); j++) {
                        format.setByteBuffer("csd-" + j, ByteBuffer.wrap(bytes.get(j)));
                    }

                    Log.i(TAG, String.format("addTrack audio track :%s", format));
                    mMuxerAudioTrack = muxer.addTrack(format);
                } else if (mMediaInfo.audioCodec == EASY_SDK_AUDIO_CODEC_G711U) {

//                    format.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_G711_MLAW);
//                    format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, mMediaInfo.channel);
//                    format.setInteger(MediaFormat.KEY_SAMPLE_RATE, mMediaInfo.sample);
//                    Log.i(TAG, String.format("addTrack audio track :%s", format));
//                    mMuxerAudioTrack = muxer.addTrack(format);
                } else if (mMediaInfo.audioCodec == EASY_SDK_AUDIO_CODEC_G711A) {

//                    format.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_G711_ALAW);
//                    format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, mMediaInfo.channel);
//                    format.setInteger(MediaFormat.KEY_SAMPLE_RATE, mMediaInfo.sample);
//                    Log.i(TAG, String.format("addTrack audio track :%s", format));
//                    mMuxerAudioTrack = muxer.addTrack(format);
                }


                muxer.start();
                mObject = muxer;

//                ByteBuffer inputBuffer = ByteBuffer.allocate(bufferSize);
//                boolean finished = false;
//                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//                while(!finished) {
//                    // getInputBuffer() will fill the inputBuffer with one frame of encoded
//                    // sample from either MediaCodec or MediaExtractor, set isAudioSample to
//                    // true when the sample is audio data, set up all the fields of bufferInfo,
//                    // and return true if there are no more samples.
//                    finished = getInputBuffer(inputBuffer, isAudioSample, bufferInfo);
//                    if (!finished) {
//                        int currentTrackIndex = isAudioSample ? audioTrackIndex : videoTrackIndex;
//                        muxer.writeSampleData(currentTrackIndex, inputBuffer, bufferInfo);
//                    }
//                };
//                muxer.stop();
//                muxer.release();

                ResultReceiver rr = mRR;
                if (rr != null) {
                    rr.send(RESULT_RECORD_BEGIN, null);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static int getSampleIndex(int sample) {
        for (int i = 0; i < AUDIO_SPECIFIC_CONFIG_SAMPLING_RATE_TABLE.length; i++) {
            if (sample == AUDIO_SPECIFIC_CONFIG_SAMPLING_RATE_TABLE[i]) {
                return i;
            }
        }
        return -1;
    }

    private synchronized void pumpSample(RTSPClient.FrameInfo frameInfo) {
        if (mObject == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            MediaMuxer muxer = (MediaMuxer) mObject;
            MediaCodec.BufferInfo bi = new MediaCodec.BufferInfo();
            bi.offset = frameInfo.offset;
            bi.size = frameInfo.length;
            ByteBuffer buffer = ByteBuffer.wrap(frameInfo.buffer, bi.offset, bi.size);
            bi.presentationTimeUs = frameInfo.stamp;
            try {
                if (frameInfo.audio) {
                    bi.offset += 7;
                    bi.size -= 7;
                    if (mMuxerAudioTrack > -1)
                        muxer.writeSampleData(mMuxerAudioTrack, buffer, bi);
                } else if (mMuxerVideoTrack > -1) {
                    if (frameInfo.type != 1) {
                        bi.flags = 0;
                    } else {
                        bi.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                    }
                    muxer.writeSampleData(mMuxerVideoTrack, buffer, bi);
                }
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
    }

    public synchronized void stopRecord() {
        mMuxerAudioTrack = mMuxerVideoTrack = -1;
        mRecordingPath = null;
        if (mObject == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            MediaMuxer muxer = (MediaMuxer) mObject;
            try {
                muxer.release();
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
        mObject = null;

        ResultReceiver rr = mRR;
        if (rr != null) {
            rr.send(RESULT_RECORD_END, null);
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onRTSPSourceCallBack(int _channelId, int _channelPtr, int _frameType, RTSPClient.FrameInfo frameInfo) {
        Thread.currentThread().setName("PRODUCER_THREAD");
        if (frameInfo != null) {
            mReceivedDataLength += frameInfo.length;
        }
        if (_frameType == RTSPClient.EASY_SDK_VIDEO_FRAME_FLAG) {
            if (frameInfo.codec != EASY_SDK_VIDEO_CODEC_H264) {
                ResultReceiver rr = mRR;
                if (!mNotSupportedVideoCB && rr != null) {
                    mNotSupportedVideoCB = true;
                    rr.send(RESULT_UNSUPPORTED_VIDEO, null);
                }
                return;
            }
//            save2path(frameInfo.buffer, 0, frameInfo.length, "/sdcard/264.h264", true);
            if (frameInfo.width == 0 || frameInfo.height == 0) {
                return;
            }

            if (frameInfo.length >= 4) {
                if (frameInfo.buffer[0] == 0 && frameInfo.buffer[1] == 0 && frameInfo.buffer[2] == 0 && frameInfo.buffer[3] == 1) {
                    if (frameInfo.length >= 8) {
                        if (frameInfo.buffer[4] == 0 && frameInfo.buffer[5] == 0 && frameInfo.buffer[6] == 0 && frameInfo.buffer[7] == 1) {
                            frameInfo.offset += 4;
                            frameInfo.length -= 4;
                        }
                    }
                }
            }

//            int offset = frameInfo.offset;
//            byte nal_unit_type = (byte) (frameInfo.buffer[offset + 4] & (byte) 0x1F);
//            if (nal_unit_type == 7 || nal_unit_type == 5) {
//                Log.i(TAG,String.format("recv I frame"));
//            }

            if (frameInfo.type == 1) {
                Log.i(TAG, String.format("recv I frame"));
            }

//            boolean firstFrame = mNewestStample == 0;
            mNewestStample = frameInfo.stamp;
            if (frameInfo.audio){
                mNewestAudioUS = frameInfo.stamp;
            }else{
                mNewestVideoUS = frameInfo.stamp;
            }
            frameInfo.audio = false;
            if (mWaitingKeyFrame) {

                ResultReceiver rr = mRR;
                Bundle bundle = new Bundle();
                bundle.putInt(EXTRA_VIDEO_WIDTH, frameInfo.width);
                bundle.putInt(EXTRA_VIDEO_HEIGHT, frameInfo.height);
                mWidth = frameInfo.width;
                mHeight = frameInfo.height;


                Log.i(TAG, String.format("width:%d,height:%d", mWidth, mHeight));

                byte[] dataOut = new byte[128];
                int[] outLen = new int[]{128};
                int result = getXPS(frameInfo.buffer, 0, frameInfo.buffer.length, dataOut, outLen, 7);
                if (result >= 0) {
                    ByteBuffer csd0 = ByteBuffer.allocate(outLen[0]);
                    csd0.put(dataOut, 0, outLen[0]);
                    csd0.clear();
                    mCSD0 = csd0;
                    Log.i(TAG, String.format("CSD-0 searched"));
                }
                outLen[0] = 128;
                result = getXPS(frameInfo.buffer, 0, frameInfo.buffer.length, dataOut, outLen, 8);
                if (result >= 0) {
                    ByteBuffer csd1 = ByteBuffer.allocate(outLen[0]);
                    csd1.put(dataOut, 0, outLen[0]);
                    csd1.clear();
                    mCSD1 = csd1;
                    Log.i(TAG, String.format("CSD-1 searched"));
                }

                if (false) {
                    int off = (result - frameInfo.offset);
                    frameInfo.offset += off;
                    frameInfo.length -= off;
                }
                Log.i(TAG, String.format("RESULT_VIDEO_SIZE:%d*%d", frameInfo.width, frameInfo.height));
                if (rr != null) rr.send(RESULT_VIDEO_SIZE, bundle);

                if (frameInfo.type != 1) {
                    Log.w(TAG, String.format("discard p frame."));
                    return;
                }
                mWaitingKeyFrame = false;
                synchronized (this) {
                    if (!TextUtils.isEmpty(mRecordingPath) && mObject == null) {
                        startRecord(mRecordingPath);
                    }
                }
            }
//            Log.d(TAG, String.format("queue size :%d", mQueue.size()));
            try {
                mQueue.put(frameInfo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (_frameType == RTSPClient.EASY_SDK_AUDIO_FRAME_FLAG) {
            mNewestStample = frameInfo.stamp;
            frameInfo.audio = true;
            if (mWaitingKeyFrame) {
                return;
            }

            if (true) {
                if (frameInfo.codec != EASY_SDK_AUDIO_CODEC_AAC &&
                        frameInfo.codec != EASY_SDK_AUDIO_CODEC_G711A &&
                        frameInfo.codec != EASY_SDK_AUDIO_CODEC_G711U &&
                        frameInfo.codec != EASY_SDK_AUDIO_CODEC_G726) {
                    ResultReceiver rr = mRR;
                    if (!mNotSupportedAudioCB && rr != null) {
                        mNotSupportedAudioCB = true;
                        if (rr != null) {
                            rr.send(RESULT_UNSUPPORTED_AUDIO, null);
                        }
                    }
                    return;
                }

            }
//            Log.d(TAG, String.format("queue size :%d", mQueue.size()));
            try {
                mQueue.put(frameInfo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (_frameType == 0) {
            // time out...
            if (!mTimeout) {
                mTimeout = true;

                ResultReceiver rr = mRR;
                if (rr != null) rr.send(RESULT_TIMEOUT, null);
            }
        } else if (_frameType == RTSPClient.EASY_SDK_EVENT_FRAME_FLAG) {
            ResultReceiver rr = mRR;
            Bundle resultData = new Bundle();
            resultData.putString("event-msg", new String(frameInfo.buffer));
            if (rr != null) rr.send(RESULT_EVENT, null);
        }
    }

    @Override
    public void onMediaInfoCallBack(int _channelId, RTSPClient.MediaInfo mi) {
        mMediaInfo = mi;
        Log.i(TAG, String.format("MediaInfo fetchd\n%s", mi));
    }

    @Override
    public void onEvent(int channel, int err, int info) {
        ResultReceiver rr = mRR;
        Bundle resultData = new Bundle();
        /*
            int state = 0;
        int err = EasyRTSP_GetErrCode(fRTSPHandle);
		// EasyRTSPClient开始进行连接，建立EasyRTSPClient连接线程
		if (NULL == _pBuf && NULL == _frameInfo)
		{
			LOGD("Recv Event: Connecting...");
			state = 1;
		}

		// EasyRTSPClient RTSPClient连接错误，错误码通过EasyRTSP_GetErrCode()接口获取，比如404
		else if (NULL != _frameInfo && _frameInfo->codec == EASY_SDK_EVENT_CODEC_ERROR)
		{
			LOGD("Recv Event: Error:%d ...\n", err);
			state = 2;
		}

		// EasyRTSPClient连接线程退出，此时上层应该停止相关调用，复位连接按钮等状态
		else if (NULL != _frameInfo && _frameInfo->codec == EASY_SDK_EVENT_CODEC_EXIT)
		{
			LOGD("Recv Event: Exit,Error:%d ...", err);
			state = 3;
		}

        * */
        switch (info) {
            case 1:
                resultData.putString("event-msg", "连接中...");
                break;
            case 2:
                resultData.putInt("errorcode", err);
                resultData.putString("event-msg", String.format("错误：%d", err));
                break;
            case 3:
                resultData.putInt("errorcode", err);
                resultData.putString("event-msg", String.format("线程退出。%d", err));
                break;
        }
        if (rr != null) rr.send(RESULT_EVENT, resultData);
    }
}