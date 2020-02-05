package com.tsingsee.easyrtc.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tsingsee.easyrtc.R;

import java.lang.ref.WeakReference;

import tv.danmaku.ijk.media.widget.media.IMediaController;

/**
 *
 */
public class RecordControllerView2 extends FrameLayout implements IMediaController, View.OnClickListener {
    private ImageButton control_btn;
    private TextView mCurrentTime;
    private TextView mEndTime;
    private SeekBar mProgress;
    private ImageView close_iv;
    private ImageView play_pause_iv;

    private MediaController.MediaPlayerControl mPlayer;
    private Context mContext;
    private View mAnchor;
    private View mRoot;

    private boolean mShowProgress;
    private boolean mShowing;
    private boolean mDragging;
    private int draggingProgress;

    private static final int sDefaultTimeout = 10000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;

    private Handler mHandler = new MessageHandler(this);

    private Runnable mSeekingPending;

    public RecordControllerView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = null;
        mContext = context;
    }

    public RecordControllerView2(Context context, boolean showProgress) {
        super(context);
        mContext = context;
        mShowProgress = showProgress;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (mRoot != null)
            initControllerView(mRoot);
    }

    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.media_record_controller2, null);

        initControllerView(mRoot);

        return mRoot;
    }

    private void initControllerView(View v) {
        control_btn = (ImageButton) mRoot.findViewById(R.id.control_btn);
        mCurrentTime = (TextView) mRoot.findViewById(R.id.start_tv);
        mEndTime = (TextView) mRoot.findViewById(R.id.end_tv);
        mProgress = (SeekBar) mRoot.findViewById(R.id.media_controller_progress);
        close_iv = (ImageView) mRoot.findViewById(R.id.close_iv);
        play_pause_iv = (ImageView) mRoot.findViewById(R.id.play_pause_iv);

        if (!mShowProgress) {
            mCurrentTime.setVisibility(View.GONE);
            mEndTime.setVisibility(View.GONE);
            mProgress.setVisibility(View.GONE);
        }

        play_pause_iv.setVisibility(View.GONE);
        control_btn.setOnClickListener(this);
        play_pause_iv.setOnClickListener(this);
        close_iv.setOnClickListener(this);
        mProgress.setOnSeekBarChangeListener(mSeekListener);
        mProgress.getThumb().setColorFilter(Color.parseColor("#58b9fb"), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public void onClick(View v) {
        if (RecordControllerView2.this.mPlayer instanceof VideoFullScreenMediaPlayerControl) {
            switch (v.getId()) {
                case R.id.control_btn:
                    pauseOrPlay();
                    break;
                case R.id.play_pause_iv:
                    pauseOrPlay();
                    break;
                case R.id.close_iv:
                    Activity activity = (Activity) mContext;
                    activity.finish();
                    break;
                default:
                    break;
            }
        }
    }

    private void pauseOrPlay() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            control_btn.setImageResource(R.mipmap.record_list_bottom_pause);
            play_pause_iv.setVisibility(View.VISIBLE);

            mHandler.removeMessages(SHOW_PROGRESS);
        } else {
            mPlayer.start();
            control_btn.setImageResource(R.mipmap.record_list_bottom_play);
            play_pause_iv.setVisibility(View.GONE);

            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    }

    @Override
    public void show() {
        show(sDefaultTimeout);
    }

    @Override
    public void showOnce(View view) {

    }

    @Override
    public void show(int timeout) {
        if (!mShowing && mAnchor != null) {
            setProgress();

            RelativeLayout.LayoutParams tlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tlp.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.video_view);
            tlp.addRule(RelativeLayout.ALIGN_TOP, R.id.video_view);

            if (mAnchor instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) mAnchor;
                vg.addView(this, tlp);
            }

            mShowing = true;
        }

        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    @Override
    public void setAnchorView(View view) {
        mAnchor = view;

        LayoutParams frameParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    @Override
    public void hide() {
        if (mAnchor == null) {
            return;
        }

        try {
            if (mAnchor instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) mAnchor;
                vg.removeView(this);
            }
            mHandler.removeMessages(SHOW_PROGRESS);
        } catch (IllegalArgumentException ex) {
            Log.w("MediaController", "already removed");
        }

        mShowing = false;
    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        mPlayer = player;
    }

    private String stringForTime(int timeMs) {
        int time = timeMs / 1000;

        String timeStr;
        int minute, second;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;

            second = time % 60;
            timeStr = unitFormat(minute) + ":" + unitFormat(second);
        }

        return timeStr;
    }

    public static String unitFormat(int i) {
        String retStr;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }

        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();

        if (mProgress != null) {
            if (duration > 0) {
                mProgress.setMax(duration);
                mProgress.setProgress(position);
            } else {
                mProgress.setMax(0);
                mProgress.setProgress(0);
            }

            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show(sDefaultTimeout);
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
            if (mPlayer == null) {
                return;
            }

            if (!fromUser) {
                return;
            }

            if (mSeekingPending != null) {
                removeCallbacks(mSeekingPending);
                mSeekingPending = null;
            }

            if (mPlayer.getDuration() <= 0)
                return;

            draggingProgress = progress;

            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime(progress));
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            mPlayer.seekTo(draggingProgress);
            setProgress();
            show(sDefaultTimeout);

            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }

        super.setEnabled(enabled);
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<RecordControllerView2> mView;

        MessageHandler(RecordControllerView2 view) {
            mView = new WeakReference<RecordControllerView2>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            RecordControllerView2 view = mView.get();
            if (view == null || view.mPlayer == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    view.hide();
                    break;
                case SHOW_PROGRESS:
                    pos = view.setProgress();
                    if (!view.mDragging && view.mShowing && view.mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }

                    break;
            }
        }
    }
}
