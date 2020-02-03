package com.tsingsee.easyrtc.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.media.MediaScannerConnection;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tsingsee.easyrtc.BuildConfig;
import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.databinding.ActivityLiveBinding;
import com.tsingsee.easyrtc.view.RecordControllerView2;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class LiveActivity extends BaseActivity {
    private static final String TAG = LiveActivity.class.getSimpleName();

    public static final int REQUEST_WRITE_STORAGE = 111;

    private ActivityLiveBinding mBinding;

    private RecordControllerView2 mediaController;
    private MediaScannerConnection mScanner;

    private GestureDetector detector;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_live);

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
//        url = "rtmp://202.69.69.180:443/webcast/bshdlive-pc";

        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);

        if (BuildConfig.DEBUG) {
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);
        }

        mediaController = new RecordControllerView2(this, false);
        mediaController.setMediaPlayer(mBinding.videoView);
        mBinding.videoView.setMediaController(mediaController);

        Runnable mSpeedCalcTask = new Runnable() {
            private long mReceivedBytes;

            @Override
            public void run() {
                long l = mBinding.videoView.getReceivedBytes();
                long received = l - mReceivedBytes;
                mReceivedBytes = l;
                mBinding.loadingSpeed.setText(String.format("%3.01fKB/s", received * 1.0f / 1024));

                if (mBinding.progress.getVisibility() == View.VISIBLE){
                    mBinding.videoView.postDelayed(this,1000);
                }
            }
        };
        mBinding.videoView.post(mSpeedCalcTask);

        Glide.with(this).load(R.mipmap.loading)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mBinding.gitIv);

        setListener();

        mBinding.progress.setVisibility(View.VISIBLE);
        mBinding.videoView.setVideoPath(url);
        mBinding.videoView.start();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mBinding.videoView.stopPlayback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mScanner != null) {
            mScanner.disconnect();
            mScanner = null;
        }
    }

    private void setListener() {
        mBinding.videoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int arg1, int arg2) {
                switch (arg1) {
                    case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        mBinding.progress.setVisibility(View.GONE);
//                        hideControl();
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        //mTextView.append("\nMEDIA_INFO_BUFFERING_START");
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        //mTextView.append("\nMEDIA_INFO_BUFFERING_END");
                        break;
                    case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                        //mTextView.append("\nMEDIA_INFO_NETWORK_BANDWIDTH: " + arg2);
                        break;
                    case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                        //mTextView.append("\nMEDIA_INFO_BAD_INTERLEAVING");
                        break;
                    case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                        //mTextView.append("\nMEDIA_INFO_NOT_SEEKABLE");
                        break;
                    case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                        //mTextView.append("\nMEDIA_INFO_METADATA_UPDATE");
                        break;
                    case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                        //mTextView.append("\nMEDIA_INFO_UNSUPPORTED_SUBTITLE");
                        break;
                    case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                        //mTextView.append("\nMEDIA_INFO_SUBTITLE_TIMED_OUT");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                        break;
                    case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                        //mTextView.append("\nMEDIA_INFO_AUDIO_RENDERING_START");
                        break;
                }

                return false;
            }
        });

        mBinding.videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
//                mBinding.msgTxt.append("\n播放错误");
//                mBinding.progress.setVisibility(View.GONE);
                mBinding.videoView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBinding.videoView.reStart();
                    }
                }, 200);

                return true;
            }
        });

        mBinding.videoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                mBinding.progress.setVisibility(View.GONE);
                Toast.makeText(LiveActivity.this,"播放完成", Toast.LENGTH_SHORT).show();
            }
        });

        mBinding.videoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                Log.i(TAG, String.format("\nonPrepared"));
            }

        });

        GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (mBinding.videoView.isInPlaybackState()) {
                    mBinding.videoView.toggleMediaControlsVisibility();
                    return true;
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
//                onChangeOritation(null);
                return true;
            }
        };

        detector = new GestureDetector(this, listener);
        mBinding.videoView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);

                return true;
            }
        });
    }

    public boolean isLandscape() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == ORIENTATION_LANDSCAPE;
    }

//    public void onChangeOrientation(View view) {
//        if (isLandscape()) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            mBinding.liveToolbar.setVisibility(View.VISIBLE);
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏
//
//            mediaController.switchGoneImageButton();
////            if (live != null && live.getDeviceType() != null && live.getDeviceType().equals("ONVIF")) {
////                mediaController.setVisibleBottomLayout();
////            } else {
////                mediaController.setGoneBottomLayout();
////            }
//        } else {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//            mBinding.liveToolbar.setVisibility(View.GONE);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏
//
////            if (live != null && live.getDeviceType() != null && live.getDeviceType().equals("ONVIF")) {
////                mediaController.setVisibleImageButton(View.VISIBLE);
////            } else {
////                mediaController.setVisibleImageButton(View.INVISIBLE);
////            }
//            mediaController.setGoneBottomLayout();
//        }
//    }

    public void onChangePlayMode(View view) {
        int mMode = mBinding.videoView.toggleAspectRatio();
    }

    public void onTakePicture(View view) {
        if (mBinding.videoView.isInPlaybackState()){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            }else{
                doTakePicture();
            }
        }
    }

    private void doTakePicture() {
//        File file = new File(RTCApplication.sPicturePath);
//        file.mkdirs();
//        file = new File(file, "pic_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg");
//        final String picture = mBinding.videoView.takePicture(file.getPath());
//        if (!TextUtils.isEmpty(picture)){
//            Toast.makeText(this,"图片已保存在\"相册/EasyPlayer\"文件夹下",Toast.LENGTH_SHORT).show();
//            if (mScanner == null) {
//                MediaScannerConnection connection = new MediaScannerConnection(this,
//                        new MediaScannerConnection.MediaScannerConnectionClient() {
//                            public void onMediaScannerConnected() {
//                                mScanner.scanFile(picture, "image/jpeg");
//                            }
//
//                            public void onScanCompleted(String path1, Uri uri) {
//
//                            }
//                        });
//                try {
//                    connection.connect();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                mScanner = connection;
//            } else {
//                mScanner.scanFile(picture, "image/jpeg");
//            }
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_WRITE_STORAGE == requestCode){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doTakePicture();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
