package org.easydarwin.push;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.view.SurfaceHolder;

import org.easydarwin.android.aio.AudioIO;
import org.easydarwin.easyrtc.BuildConfig;
import org.easydarwin.muxer.EasyMuxer;
import org.easydarwin.sw.JNIUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar;

public class MediaStream {

    private static final boolean VERBOSE = BuildConfig.DEBUG;
    EasyPusher mEasyPusher;
    static final String TAG = "EasyPusher";
    final int width = 320, height = 240;
    int framerate, bitrate;
    int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    MediaCodec mMediaCodec;
    WeakReference<SurfaceHolder> mSurfaceHolderRef;
    Camera mCamera;
    boolean pushStream = false;//是否要推送数据
    AudioStream audioStream;
    AudioIO aio;
    private boolean isCameraBack = true;
    private Context mApplicationContext;
    Thread pushThread;
    boolean codecAvailable = false;
    private Consumer mConsumer;
    private EasyMuxer mMuxer;
    private int mTrackIndex;
    private int colorFormat;
    private final Handler cameraHandler;
    private ByteBuffer[] inputBuffers;
    private ByteBuffer[] outputBuffers;
    private EasyPusher.OnInitPusherCallback mCallback;
    private String mId;
    private String mPort;
    private String mIP;

    public MediaStream(Context context, SurfaceHolder holder, AudioIO aio) throws IOException {
        mApplicationContext = context;
        this.aio = aio;
        mSurfaceHolderRef = new WeakReference(holder);
        HandlerThread camera_thread = new HandlerThread("camera_thread");
        camera_thread.start();
        cameraHandler = new Handler(camera_thread.getLooper());
        cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    configMediaCodec();
                    createCamera();
                    startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 初始化编码器
     */
    private void configMediaCodec() throws IOException {
        framerate = 20;
        bitrate = (int) (width * height * framerate * 2 * 0.07);
        mMediaCodec = MediaCodec.createEncoderByType("video/avc");
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        colorFormat = getColorFormat(mMediaCodec.getCodecInfo());
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();

        inputBuffers = mMediaCodec.getInputBuffers();
        outputBuffers = mMediaCodec.getOutputBuffers();
    }

//    public void setCallback(EasyPusher.OnInitPusherCallback callback) {
//        mEasyPusher.setOnInitPusherCallback(callback);
//    }

    private void initPusher(String ip, String port, String id, final EasyPusher.OnInitPusherCallback callback) {
        mEasyPusher = new EasyPusher();
        EasyPusher.OnInitPusherCallback wrapper = new EasyPusher.OnInitPusherCallback() {
            @Override
            public void onCallback(int code) {
                if (callback != null) {
                    callback.onCallback(code);
                }
            }
        };
        mEasyPusher.initPush(ip, port, String.format("%s.sdp", id), mApplicationContext, wrapper);
    }


    /**
     * 重新开始
     */
    public void reStartStream() {
        if (mCamera == null) return;
        stopPreview();
        destroyCamera();
        createCamera();
        startPreview();
    }

    public static int[] determineMaximumSupportedFramerate(Camera.Parameters parameters) {
        int[] maxFps = new int[]{0, 0};
        List<int[]> supportedFpsRanges = parameters.getSupportedPreviewFpsRange();
        for (Iterator<int[]> it = supportedFpsRanges.iterator(); it.hasNext(); ) {
            int[] interval = it.next();
            if (interval[1] > maxFps[1] || (interval[0] > maxFps[0] && interval[1] == maxFps[1])) {
                maxFps = interval;
            }
        }
        return maxFps;
    }


    public static final boolean useSWCodec() {
        return Build.VERSION.SDK_INT >= 23;
    }


    public boolean createCamera() {
        try {
            if (Camera.getNumberOfCameras() < 2){
                mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
            mCamera = Camera.open(mCameraId);

            Camera.Parameters parameters = mCamera.getParameters();
            int[] max = determineMaximumSupportedFramerate(parameters);
            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, camInfo);
            int cameraRotationOffset = camInfo.orientation;
            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
                cameraRotationOffset += 180;
            int rotate = (360 + cameraRotationOffset) % 360;
            parameters.setRotation(rotate);

            parameters.setPreviewFormat(colorFormat == COLOR_FormatYUV420SemiPlanar || colorFormat == COLOR_TI_FormatYUV420PackedSemiPlanar ? ImageFormat.NV21 : ImageFormat.YV12);
            parameters.setPreviewSize(width, height);
            parameters.setPreviewFrameRate(20);
            mCamera.setParameters(parameters);
            SurfaceHolder holder = mSurfaceHolderRef.get();
            if (holder != null) {
                mCamera.setPreviewDisplay(holder);
            }
            return true;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stack = sw.toString();
            destroyCamera();
            e.printStackTrace();
            return false;
        }
    }

    public void autoFocus() {
        cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCamera != null) mCamera.autoFocus(null);
            }
        });
    }

    class Consumer extends Thread {
        byte[] mPpsSps = new byte[0];
        int keyFrmHelperCount = 0;
        private long timeStamp = System.currentTimeMillis();

        public Consumer() {
            super("Consumer");
        }

        @Override
        public void run() {

            initPusher(mIP, mPort, mId, mCallback);
            audioStream = new AudioStream(mEasyPusher, mMuxer, aio);
            audioStream.startRecord();


            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            byte[] h264 = new byte[width * height * 3 / 2];
            int outputBufferIndex = 0;
            do {
                try {
                    outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                    if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // no output available yet
                    } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        // not expected for an encoder
                    } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        EasyMuxer muxer = MediaStream.this.mMuxer;
                        if (muxer != null) {
                            // should happen before receiving buffers, and should only happen once
                            MediaFormat newFormat = mMediaCodec.getOutputFormat();
                            muxer.addTrack(newFormat, true);
                        }
                    } else if (outputBufferIndex < 0) {
                        // let's ignore it
                    } else {

                        ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];

                        EasyMuxer muxer = MediaStream.this.mMuxer;
                        if (muxer != null) {
                            muxer.pumpStream(outputBuffer, bufferInfo, true);
                        }
//                        String data0 = String.format("%x %x %x %x %x %x %x %x %x %x ", outData[0], outData[1], outData[2], outData[3], outData[4], outData[5], outData[6], outData[7], outData[8], outData[9]);
//                        Log.e("out_data", data0);
                        //记录pps和sps
                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.size + bufferInfo.offset);
                        int sps_pps_header_len = 0;
                        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) {
                                Log.i(TAG, "key video frame");
                                if (h264.length < bufferInfo.size) {
                                    h264 = new byte[bufferInfo.size];
                                }
                                outputBuffer.get(h264, 0, bufferInfo.size);
                                mEasyPusher.push(h264, 0, bufferInfo.size, bufferInfo.presentationTimeUs / 1000, 1);

                                if (BuildConfig.DEBUG)
                                    Log.i(TAG, String.format("push video stamp:%d", bufferInfo.presentationTimeUs / 1000));
                                mPpsSps = new byte[0];
                                Thread.sleep(30);
                            } else {
                                byte[] outData = new byte[bufferInfo.size];
                                outputBuffer.get(outData);
                                mPpsSps = outData;
                            }
                            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                            continue;
                        }
                        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) {
                            Log.i(TAG, "key video frame");
                            //在关键帧前面加上pps和sps数据
                            int length = mPpsSps.length + bufferInfo.size;
                            if (h264.length < length) {
                                h264 = new byte[length];
                            }
                            System.arraycopy(mPpsSps, 0, h264, 0, mPpsSps.length);
                            outputBuffer.get(h264, mPpsSps.length, bufferInfo.size);
                            mEasyPusher.push(h264, 0, length, bufferInfo.presentationTimeUs / 1000, 1);
                            if (BuildConfig.DEBUG)
                                Log.i(TAG, String.format("push video stamp:%d", bufferInfo.presentationTimeUs / 1000));
                            Thread.sleep(30);
                        } else {
                            int length = bufferInfo.size;
                            if (h264.length < length) {
                                h264 = new byte[length];
                            }
                            outputBuffer.get(h264, 0, length);
                            mEasyPusher.push(h264, 0, bufferInfo.size, bufferInfo.presentationTimeUs / 1000, 1);
                            if (BuildConfig.DEBUG)
                                Log.i(TAG, String.format("push video stamp:%d", bufferInfo.presentationTimeUs / 1000));
                            Thread.sleep(30);
                        }
                        mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    }
                }catch (InterruptedException ex){
                    ex.printStackTrace();
                }
            }
            while (mConsumer != null);
        }
    }

    private void save2file(byte[] data, String path) {
        if (true) return;
        try {
            FileOutputStream fos = new FileOutputStream(path, true);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 根据Unicode编码完美的判断中文汉字和符号
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    private int getTxtPixelLength(String txt, boolean zoomed) {
        int length = 0;
        int fontWidth = zoomed ? 16 : 8;
        for (int i = 0; i < txt.length(); i++) {
            length += isChinese(txt.charAt(i)) ? fontWidth * 2 : fontWidth;
        }
        return length;
    }

    /**
     * 开启预览
     */
    public synchronized void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
            try {
                mCamera.autoFocus(null);
            } catch (Exception e) {
                //忽略异常
                Log.i(TAG, "auto foucus fail");
            }

            int previewFormat = mCamera.getParameters().getPreviewFormat();
            Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
            int size = previewSize.width * previewSize.height * ImageFormat.getBitsPerPixel(previewFormat) / 8;
            mCamera.addCallbackBuffer(new byte[size]);
            mCamera.setPreviewCallbackWithBuffer(previewCallback);
        }
    }

    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        Camera.Size previewSize;
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (data == null) {
                return;
            }
            if (previewSize == null){
                previewSize  = mCamera.getParameters().getPreviewSize();
            }
            int bufferIndex;
            do {
                JNIUtil.nV21To420SP(data, previewSize.width, previewSize.height);
                bufferIndex = mMediaCodec.dequeueInputBuffer(10000);
                if (bufferIndex >= 0) {
                    inputBuffers[bufferIndex].clear();
                    inputBuffers[bufferIndex].put(data);
                    mMediaCodec.queueInputBuffer(bufferIndex, 0, inputBuffers[bufferIndex].position(), System.nanoTime() / 1000 , 0);
                } else {
                    Log.e(TAG, "No buffer available !");
                }
            } while (bufferIndex < 0 && mConsumer != null);
            mCamera.addCallbackBuffer(data);
        }

    };


    /**
     * 旋转YUV格式数据
     *
     * @param src    YUV数据
     * @param format 0，420P；1，420SP
     * @param width  宽度
     * @param height 高度
     * @param degree 旋转度数
     */
    private static void yuvRotate(byte[] src, int format, int width, int height, int degree) {
        int offset = 0;
        if (format == 0) {
            JNIUtil.rotateMatrix(src, offset, width, height, degree);
            offset += (width * height);
            JNIUtil.rotateMatrix(src, offset, width / 2, height / 2, degree);
            offset += width * height / 4;
            JNIUtil.rotateMatrix(src, offset, width / 2, height / 2, degree);
        } else if (format == 1) {
            JNIUtil.rotateMatrix(src, offset, width, height, degree);
            offset += width * height;
            JNIUtil.rotateShortMatrix(src, offset, width / 2, height / 2, degree);
        }
    }

    /**
     * 停止预览
     */
    public synchronized void stopPreview() {
        Thread t = mConsumer;

        if (t != null) {
            mConsumer = null;
            t.interrupt();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
        }
        if (audioStream != null) {
            audioStream.stop();
        }
        if (mMediaCodec != null) {
            stopMediaCodec();
        }

        if (mMuxer != null) {
            mMuxer.release();
            mMuxer = null;
        }
    }

    public Camera getCamera() {
        return mCamera;
    }


    /**
     * 切换前后摄像头
     */
    public void switchCamera() {
        int cameraCount = 0;
        if (isCameraBack) {
            isCameraBack = false;
        } else {
            isCameraBack = true;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                //现在是后置，变更为前置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    mCamera.stopPreview();//停掉原来摄像头的预览
                    mCamera.release();//释放资源
                    mCamera = null;//取消原来摄像头
                    mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                    createCamera();
                    startPreview();
                    break;
                }
            } else {
                //现在是前置， 变更为后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    mCamera.stopPreview();//停掉原来摄像头的预览
                    mCamera.release();//释放资源
                    mCamera = null;//取消原来摄像头
                    mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    createCamera();
                    startPreview();
                    break;
                }
            }
        }
    }

    private String recordPath = Environment.getExternalStorageDirectory().getPath();

    /**
     * 销毁Camera
     */
    public synchronized void destroyCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            try {
                mCamera.release();
            } catch (Exception e) {

            }
            mCamera = null;
        }
    }


    public static int getColorFormat(MediaCodecInfo codecInfo) {
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType("video/avc");
        int[] cf = new int[capabilities.colorFormats.length];
        System.arraycopy(capabilities.colorFormats, 0, cf, 0, cf.length);
        List<Integer> sets = new ArrayList<>();
        for (int i = 0; i < cf.length; i++) {
            sets.add(cf[i]);
        }
        if (sets.contains(COLOR_FormatYUV420SemiPlanar)) {
            return COLOR_FormatYUV420SemiPlanar;
        } else if (sets.contains(COLOR_FormatYUV420Planar)) {
            return COLOR_FormatYUV420Planar;
        } else if (sets.contains(COLOR_FormatYUV420PackedPlanar)) {
            return COLOR_FormatYUV420PackedPlanar;
        }
        return 0;
    }

    /**
     * 停止编码并释放编码资源占用
     */
    private void stopMediaCodec() {
        if (mMediaCodec != null) {
            codecAvailable = false;
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }

    public boolean isStreaming() {
        return mConsumer != null;
    }

    public void startStream(String ip, String port, String id, final EasyPusher.OnInitPusherCallback callback) {
        mIP = ip;
        mPort = port;
        mId = id;
        mCallback = new EasyPusher.OnInitPusherCallback() {
            public int code = 1000;
            Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void onCallback(final int code) {
                if (this.code != code) {
                    this.code = code;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onCallback(code);
                        }
                    });
                }
            }
        };

        mConsumer = new Consumer();
        cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                mConsumer.start();
            }
        });
    }


    public void stopStream() {
        cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mConsumer != null) {
                    Consumer consumer = mConsumer;
                    mConsumer = null;
                    consumer.interrupt();
                    try {
                        consumer.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    public void destroyStream() {
        pushStream = false;
        if (pushThread != null) {
            pushThread.interrupt();
        }
        destroyCamera();
        stopMediaCodec();
        mEasyPusher.stop();
    }

    public void setSurfaceHolder(SurfaceHolder holder) {
        mSurfaceHolderRef = new WeakReference<SurfaceHolder>(holder);
    }

    public void release() {
        stopStream();
        stopPreview();
        destroyCamera();
        stopMediaCodec();
        cameraHandler.getLooper().quitSafely();
    }
}
