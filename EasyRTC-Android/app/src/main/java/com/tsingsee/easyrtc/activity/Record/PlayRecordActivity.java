package com.tsingsee.easyrtc.activity.Record;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.MediaScannerConnection;
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
import com.tsingsee.easyrtc.activity.BaseActivity;
import com.tsingsee.easyrtc.databinding.ActivityPlayRecordBinding;
import com.tsingsee.easyrtc.model.Record;
import com.tsingsee.easyrtc.view.RecordControllerView2;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayRecordActivity extends BaseActivity {
    private static final String TAG = PlayRecordActivity.class.getSimpleName();

    private ActivityPlayRecordBinding mBinding;

    private RecordControllerView2 mediaController;
    private MediaScannerConnection mScanner;

    private GestureDetector detector;

    private Record record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_play_record);

        Intent intent = getIntent();
        record = (Record) intent.getSerializableExtra("record");

        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);

        if (BuildConfig.DEBUG) {
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);
        }

        mediaController = new RecordControllerView2(this, true);
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

        Glide.with(this).load(R.drawable.loading)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mBinding.gitIv);

        setListener();

        mBinding.videoView.setVideoPath(record.getHls());
        mBinding.videoView.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
                // 播放错误
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
                Toast.makeText(PlayRecordActivity.this,"播放完成", Toast.LENGTH_SHORT).show();
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
                }

                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
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
}