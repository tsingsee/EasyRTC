package com.tsingsee.easyrtc.activity.Record;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tsingsee.easyrtc.BuildConfig;
import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.RTCApplication;
import com.tsingsee.easyrtc.activity.BaseActivity;
import com.tsingsee.easyrtc.databinding.ActivityRecordTimeAxisBinding;
import com.tsingsee.easyrtc.http.BaseEntity2;
import com.tsingsee.easyrtc.http.BaseObserver2;
import com.tsingsee.easyrtc.http.RetrofitFactory;
import com.tsingsee.easyrtc.model.Record;
import com.tsingsee.easyrtc.model.RecordModel;
import com.tsingsee.easyrtc.tool.DateUtil;
import com.tsingsee.easyrtc.tool.DownLoadUtil;
import com.tsingsee.easyrtc.tool.SharedHelper;
import com.tsingsee.easyrtc.view.RecordControllerView;
import com.tsingsee.easyrtc.view.RullerView.TimeRange;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

/**
 * 录像播放，带时间轴
 * */
public class RecordTimeAxisActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {
    private static final String TAG = RecordTimeAxisActivity.class.getSimpleName();

    private static final int request_code = 8888;
    public static final int REQUEST_WRITE_STORAGE = 111;

    private ActivityRecordTimeAxisBinding mBinding;

    private RecordControllerView mediaController;
    private MediaScannerConnection mScanner;

    private GestureDetector detector;

    private RecordModel recordModel;
    private Record selectRecord;
    private Date selectDate;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_record_time_axis);

        setSupportActionBar(mBinding.liveToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mBinding.liveToolbar.setOnMenuItemClickListener(this);
        // 左边的小箭头（注意需要在setSupportActionBar(toolbar)之后才有效果）
        mBinding.liveToolbar.setNavigationIcon(R.drawable.back);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        mBinding.toolbarTv.setText("录像(" + id + ")-时间轴视图");

        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);

        if (BuildConfig.DEBUG) {
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);
        }

        mediaController = new RecordControllerView(this);
        mediaController.setMediaPlayer(mBinding.videoView);
        mBinding.videoView.setMediaController(mediaController);

        // 按日查询通道录像
        selectDate = new Date();
        queryDaily();

        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(mBinding.videoView.getLayoutParams());
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        Runnable mSpeedCalcTask = new Runnable() {
            private long mReceivedBytes;

            @Override
            public void run() {
                long l = mBinding.videoView.getReceivedBytes();
                long received = l - mReceivedBytes;
                mReceivedBytes = l;
                mBinding.loadingSpeed.setText(String.format("%3.01fKB/s", received * 1.0f / 1024));

                if (mBinding.progress.getVisibility() == View.VISIBLE) {
                    mBinding.videoView.postDelayed(this,1000);
                }
            }
        };
        mBinding.videoView.post(mSpeedCalcTask);

        Glide.with(this).load(R.drawable.loading)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mBinding.gitIv);

        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (recordModel != null && recordModel.getList() != null && recordModel.getList().size() > 0) {
            mBinding.progress.setVisibility(View.VISIBLE);

            selectRecord = recordModel.getList().get(0);
            mediaController.startAtSecond = selectRecord.getStartAtSecond();

            mBinding.videoView.setVideoURI(Uri.parse(selectRecord.getHls()));
            mBinding.videoView.toggleRender();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return false;
    }

    // 返回的功能
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDateTv() {
        String period = "  " + DateUtil.getDateStr(selectDate, "yyyy-MM-dd");
        if (mediaController != null && mediaController.date_tv != null) {
            mediaController.date_tv.setText(period);
            mediaController.setDateDesc(period);
        }

        if (recordModel != null) {
            List<TimeRange> ranges = new ArrayList<>();
            for (Record item : recordModel.getList()) {
                TimeRange r = new TimeRange();
                r.start = item.getStartAtSecond();
                r.duration = (int) item.getDuration();

                ranges.add(r);
            }

            mediaController.time_hs.setRanges(ranges);
            mediaController.time_hs2.setRanges(ranges);
        }
    }

    // 获取摄像机url
    private void queryDaily() {
        mBinding.progress.setVisibility(View.VISIBLE);

        showDateTv();
        String period = DateUtil.getDateStr(selectDate, "yyyyMMdd");

        Observable<BaseEntity2<RecordModel>> observable = RetrofitFactory.getRetrofitService2().queryDaily(id, period);
        observable.compose(compose(this.<BaseEntity2<RecordModel>> bindToLifecycle()))
                .subscribe(new BaseObserver2<RecordModel>(this, dialog, null, false) {
                    @Override
                    protected void onHandleSuccess(RecordModel record) {
                        recordModel = record;

                        if (record.getList() != null && record.getList().size() > 0) {
                            boolean start = false;
                            if (selectRecord == null) {
                                start = true;
                            }

                            selectRecord = record.getList().get(0);
                            mediaController.startAtSecond = selectRecord.getStartAtSecond();

                            if (start) {
                                mBinding.videoView.setVideoPath(selectRecord.getHls());
                                mBinding.videoView.start();
                            } else {
                                mBinding.videoView.setVideoURI(Uri.parse(selectRecord.getHls()));
                                mBinding.videoView.toggleRender();
                            }
                        } else {
                            mBinding.progress.setVisibility(View.GONE);
                            Toast.makeText(RecordTimeAxisActivity.this, "没有录像", Toast.LENGTH_SHORT).show();

                            mBinding.renderContainer.setTag(new Integer(1111));
                            mediaController.setAnchorView(mBinding.renderContainer);
                            mediaController.show();

                            mBinding.videoView.pause();
                        }
                    }

                    @Override
                    protected void loginSuccess() {
                        queryDaily();
                    }
                });
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
                        showDateTv();
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
                Toast.makeText(RecordTimeAxisActivity.this,"播放完成", Toast.LENGTH_SHORT).show();
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
                    if (mediaController.fullscreen_rl.getVisibility() == View.VISIBLE) {
                        mBinding.videoView.toggleMediaControlsVisibility();
                    }
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

    public void onChangeOrientation(View view) {
        if (isLandscape()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mBinding.liveToolbar.setVisibility(View.VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // 显示状态栏
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mBinding.liveToolbar.setVisibility(View.GONE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // 隐藏状态栏
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void onChangePlayMode(View view) {
        int mMode = mBinding.videoView.toggleAspectRatio();
    }

    // 返回直播界面
    public void backLive() {
        finish();
    }

    // 下载当然录像段
    public void downloadRecord() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确定下载该时段的录像吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            SharedHelper helper = new SharedHelper(RTCApplication.getContext());
                            String url = helper.getURL() + "/api/v1/record/download/" + id + "/" + selectRecord.getStartAt();

                            DownLoadUtil downLoadUtil = new DownLoadUtil();
                            downLoadUtil.initDownload(RecordTimeAxisActivity.this);
                            downLoadUtil.download(url, selectRecord.getStartAt());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 去视图列表
    public void recordList() {
        Intent intent = new Intent(this, RecordListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("id", id);
        bundle.putSerializable("selectDate", selectDate);
        intent.putExtras(bundle);//发送数据
        startActivity(intent);
    }

    // 删除当然录像段
    public void deleteRecord() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确定删除该时段的录像吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeRecord(selectRecord);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 调整播放进度
    public void scaleScroll(double scale) {
        int count = recordModel.getList().size();
        for (int i = (count - 1); i >= 0; i--) {
            Record record = recordModel.getList().get(i);
            double pos = scale - record.getStartAtSecond();

            if (pos >= 0 && pos < record.getDuration()) {
                if (record.getHls().equals(selectRecord.getHls())) {
                    mBinding.videoView.seekTo((int)pos * 1000);
                } else {
                    mBinding.progress.setVisibility(View.VISIBLE);

                    selectRecord = record;
                    mediaController.startAtSecond = selectRecord.getStartAtSecond();

                    mBinding.videoView.setVideoURI(Uri.parse(selectRecord.getHls()));
                    mBinding.videoView.toggleRender();
                    mBinding.videoView.seekTo((int)pos * 1000);
                }

                mediaController.mDragging = false;
                return;
            }
        }

        mediaController.pauseVideo();
    }

    private void removeRecord(final Record record) {
        if (record == null) {
            return;
        }

        showHub("删除中");

        Observable<BaseEntity2<String>> observable = RetrofitFactory.getRetrofitService2().removeRecord(id, record.getStartAt());
        observable.compose(compose(this.<BaseEntity2<String>> bindToLifecycle()))
                .subscribe(new BaseObserver2<String>(this, dialog, null, false) {
                    @Override
                    protected void onHandleSuccess(String res) {
                        hideHub();

                        // 删除完成后 刷新数据
                        queryDaily();
                    }

                    @Override
                    protected void loginSuccess() {
                        removeRecord(record);
                    }
                });
    }

    /**
     * 选择日期
     * value: 0弹出日历 -1前一天 1后一天
     * */
    public void selectDate(int value) {
        if (value == 0) {
            Intent intent = new Intent(RecordTimeAxisActivity.this, CalendarActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("id", id);
            bundle.putSerializable("selectDate", selectDate);
            intent.putExtras(bundle);//发送数据
            startActivityForResult(intent, request_code);
        } else {
            if (value < 0) {
                selectDate = DateUtil.getSpecifiedDay(selectDate, -1);
            } else {
                selectDate = DateUtil.getSpecifiedDay(selectDate, +1);
            }

            queryDaily();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == request_code && resultCode == RESULT_OK) {
            selectDate = (Date) intent.getSerializableExtra("selectDate");

            queryDaily();
        }
    }
}