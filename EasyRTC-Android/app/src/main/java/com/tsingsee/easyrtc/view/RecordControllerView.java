package com.tsingsee.easyrtc.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.view.RullerView.HorizontalScaleScrollView;

import java.lang.ref.WeakReference;

import tv.danmaku.ijk.media.widget.media.IMediaController;

/**
 *
 */
public class RecordControllerView extends FrameLayout implements IMediaController, View.OnClickListener {
    private static final int sDefaultTimeout = 10000;

    private MediaController.MediaPlayerControl mPlayer;
    private Context mContext;
    private View mRoot;
    private View mAnchor;

    private boolean mShowing;
    private String dateDesc = "";

    private RelativeLayout normal_rl;
    private ImageButton back_live_btn;
    private ImageButton full_btn;
    private TextView download_tv;
    private TextView record_list_tv;
    private TextView pause_tv;
    private TextView delete_tv;
    public TextView date_tv;
    private ImageButton date_before;
    private ImageButton date_after;
    public HorizontalScaleScrollView time_hs;
    public HorizontalScaleScrollView time_hs2;

    public RelativeLayout fullscreen_rl;
    private ImageButton fullscreen_live_btn;
    private ImageButton fullscreen_list_btn;
    private ImageButton fullscreen_pause_btn;
    private ImageButton fullscreen_normal_btn;

    private RelativeLayout.LayoutParams thisFrameParams;

    public int startAtSecond;

    public RecordControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = null;
        mContext = context;
    }

    public RecordControllerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RecordControllerView(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (mRoot != null)
            initControllerView(mRoot);
    }

    /**
     * Создает вьюху которая будет находится поверх вашего VideoView или другого контролла
     */
    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.media_record_controller, null);

        normal_rl = (RelativeLayout) mRoot.findViewById(R.id.normal_rl);
        back_live_btn = (ImageButton) mRoot.findViewById(R.id.back_live_btn);
        full_btn = (ImageButton) mRoot.findViewById(R.id.full_btn);
        download_tv = (TextView) mRoot.findViewById(R.id.download_tv);
        record_list_tv = (TextView) mRoot.findViewById(R.id.record_list_tv);
        pause_tv = (TextView) mRoot.findViewById(R.id.pause_tv);
        delete_tv = (TextView) mRoot.findViewById(R.id.delete_tv);
        date_tv = (TextView) mRoot.findViewById(R.id.date_tv);
        date_before = (ImageButton) mRoot.findViewById(R.id.date_before);
        date_after = (ImageButton) mRoot.findViewById(R.id.date_after);
        time_hs = (HorizontalScaleScrollView) mRoot.findViewById(R.id.time_hs);
        time_hs2 = (HorizontalScaleScrollView) mRoot.findViewById(R.id.time_hs2);

        fullscreen_rl = (RelativeLayout) mRoot.findViewById(R.id.fullscreen_rl);
        fullscreen_live_btn = (ImageButton) mRoot.findViewById(R.id.fullscreen_live_btn);
        fullscreen_list_btn = (ImageButton) mRoot.findViewById(R.id.fullscreen_list_btn);
        fullscreen_pause_btn = (ImageButton) mRoot.findViewById(R.id.fullscreen_pause_btn);
        fullscreen_normal_btn = (ImageButton) mRoot.findViewById(R.id.fullscreen_normal_btn);

        date_tv.setText("  " + dateDesc);

        initControllerView(mRoot);

        showScreen();

        return mRoot;
    }

    private void initControllerView(View v) {
        full_btn.setOnClickListener(this);
        back_live_btn.setOnClickListener(this);
        download_tv.setOnClickListener(this);
        record_list_tv.setOnClickListener(this);
        pause_tv.setOnClickListener(this);
        delete_tv.setOnClickListener(this);
        date_tv.setOnClickListener(this);
        date_before.setOnClickListener(this);
        date_after.setOnClickListener(this);

        fullscreen_live_btn.setOnClickListener(this);
        fullscreen_list_btn.setOnClickListener(this);
        fullscreen_pause_btn.setOnClickListener(this);
        fullscreen_normal_btn.setOnClickListener(this);

        time_hs.setOnScrollListener(scrollListener);
        time_hs2.setOnScrollListener(scrollListener);
    }

    private HorizontalScaleScrollView.OnScrollListener scrollListener  = new HorizontalScaleScrollView.OnScrollListener() {
        @Override
        public void onScaleScroll(double scale) {
            // 当前的进度
        }

        @Override
        public void touchBegin() {
            mDragging = true;
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        @Override
        public void touchEnd(double scale) {
            if (RecordControllerView.this.mPlayer instanceof VideoFullScreenMediaPlayerControl) {
                VideoFullScreenMediaPlayerControl mPlayer = (VideoFullScreenMediaPlayerControl) RecordControllerView.this.mPlayer;
                mPlayer.scaleScroll(scale);
            }

            setProgress();
            show(sDefaultTimeout);

            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    @Override
    public void onClick(View v) {
        if (RecordControllerView.this.mPlayer instanceof VideoFullScreenMediaPlayerControl) {
            VideoFullScreenMediaPlayerControl mPlayer = (VideoFullScreenMediaPlayerControl) RecordControllerView.this.mPlayer;
            switch (v.getId()) {
                case R.id.back_live_btn:
                    mPlayer.backLive();
                    break;
                case R.id.download_tv:
                    mPlayer.downloadRecord();
                    break;
                case R.id.record_list_tv:
                    mPlayer.recordList();
                    break;
                case R.id.pause_tv:
                    pauseOrPlay();
                    break;
                case R.id.delete_tv:
                    mPlayer.deleteRecord();
                    break;
                case R.id.date_tv:
                    mPlayer.selectDate(0);
                    break;
                case R.id.date_before:
                    mPlayer.selectDate(-1);
                    break;
                case R.id.date_after:
                    mPlayer.selectDate(1);
                    break;
                case R.id.fullscreen_live_btn:
                    mPlayer.backLive();
                    break;
                case R.id.fullscreen_list_btn:
                    mPlayer.recordList();
                    break;
                case R.id.fullscreen_pause_btn:
                    pauseOrPlay();
                    break;
                case R.id.fullscreen_normal_btn:
                    toggleFullScreen();
                    break;
                case R.id.full_btn:
                    toggleFullScreen();
                    break;
                default:
                    break;
            }
        }
    }

    private void pauseOrPlay() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();

            pause_tv.setTextColor(mContext.getResources().getColor(R.color.color_blue));
            pause_tv.setCompoundDrawablesWithIntrinsicBounds(null, mContext.getResources().getDrawable(R.mipmap.time_pause_on), null, null);
            fullscreen_pause_btn.setImageResource(R.mipmap.horizontal_play);

            mHandler.removeMessages(SHOW_PROGRESS);
        } else {
            mPlayer.start();

            pause_tv.setTextColor(mContext.getResources().getColor(R.color.color_73));
            pause_tv.setCompoundDrawablesWithIntrinsicBounds(null, mContext.getResources().getDrawable(R.mipmap.time_pause), null, null);
            fullscreen_pause_btn.setImageResource(R.mipmap.horizontal_pause);

            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    }

    public void pauseVideo() {
        if (!mPlayer.isPlaying()) {
            return;
        }

        mPlayer.pause();

        pause_tv.setTextColor(mContext.getResources().getColor(R.color.color_blue));
        pause_tv.setCompoundDrawablesWithIntrinsicBounds(null, mContext.getResources().getDrawable(R.mipmap.time_pause_on), null, null);
        fullscreen_pause_btn.setImageResource(R.mipmap.horizontal_play);

        mHandler.removeMessages(SHOW_PROGRESS);
    }

    private void toggleFullScreen() {
        if (mPlayer == null) {
            return;
        }

        if (RecordControllerView.this.mPlayer instanceof VideoFullScreenMediaPlayerControl) {
            VideoFullScreenMediaPlayerControl player = (VideoFullScreenMediaPlayerControl) RecordControllerView.this.mPlayer;
            player.toggleFullScreen();

            if (player.isFullScreen()) {
                setShowNormalRL();
            } else {
                setShowFullscreenRL();
            }
        }
    }

    private void showScreen() {
        if (mPlayer == null) {
            return;
        }

        if (RecordControllerView.this.mPlayer instanceof VideoFullScreenMediaPlayerControl) {
            VideoFullScreenMediaPlayerControl player = (VideoFullScreenMediaPlayerControl) RecordControllerView.this.mPlayer;

            if (player.isFullScreen()) {
                setShowFullscreenRL();
            } else {
                setShowNormalRL();
            }
        }
    }

    @Override
    public void hide() {
        if (mAnchor == null) {
            return;
        }

        VideoFullScreenMediaPlayerControl player = (VideoFullScreenMediaPlayerControl) this.mPlayer;
        if (!player.isFullScreen()) {
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
    public boolean isShowing() {
        return mShowing;
    }

    @Override
    public void setAnchorView(View view) {

        // TODO mBinding.renderContainer 没有录像时，加到了这里
        if (mAnchor != null) {
            ViewGroup vg = (ViewGroup) mAnchor;
            vg.removeView(this);
        }

        mAnchor = view;

        removeAllViews();
        View v = makeControllerView();

        RelativeLayout.LayoutParams frameParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(v, frameParams);
    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        mPlayer = player;
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
            Integer tag = (Integer) mAnchor.getTag();
            if (tag != null && tag == 1111) {
                thisFrameParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 960);
                thisFrameParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

                ViewGroup vg = (ViewGroup) mAnchor;
                vg.addView(this, thisFrameParams);
            } else {
                if (mAnchor instanceof ViewGroup) {
                    thisFrameParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    if (RecordControllerView.this.mPlayer instanceof VideoFullScreenMediaPlayerControl) {
                        VideoFullScreenMediaPlayerControl player = (VideoFullScreenMediaPlayerControl) RecordControllerView.this.mPlayer;
                        if (player.isFullScreen()) {
                            thisFrameParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.video_view);
                        } else {
                            thisFrameParams.addRule(RelativeLayout.BELOW, R.id.video_view);
                            thisFrameParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        }
                    }

                    ViewGroup vg = (ViewGroup) mAnchor;
                    vg.addView(this, thisFrameParams);
                }
            }

            mHandler.sendEmptyMessage(SHOW_PROGRESS);
            Message msg = mHandler.obtainMessage(FADE_OUT);
            if (timeout != 0) {
                mHandler.removeMessages(FADE_OUT);
                mHandler.sendMessageDelayed(msg, timeout);
            }

            mShowing = true;
        }
    }

    /**
     * 显示横屏的控件
     * */
    public void setShowFullscreenRL() {
        fullscreen_rl.setVisibility(View.VISIBLE);
        normal_rl.setVisibility(View.GONE);

        if (thisFrameParams != null) {
            thisFrameParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.video_view);
        }
    }

    /**
     * 显示竖屏的控件
     * */
    public void setShowNormalRL() {
        fullscreen_rl.setVisibility(View.GONE);
        normal_rl.setVisibility(View.VISIBLE);

        if (thisFrameParams != null) {
            thisFrameParams.addRule(RelativeLayout.BELOW, R.id.video_view);
            thisFrameParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        }
    }

    public void setDateDesc(String dateDesc) {
        this.dateDesc = dateDesc;

        date_tv.setText("  " + dateDesc);
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }

        int position = mPlayer.getCurrentPosition() / 1000;// 毫秒转成秒
        Log.d("position", "position-->> " + position);

        if (time_hs != null) {
            time_hs.setCurScale(startAtSecond + position);
        }
        if (time_hs2 != null) {
            time_hs2.setCurScale(startAtSecond + position);
        }

        return position;
    }

    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;

    public boolean mDragging;
    private Handler mHandler = new RecordControllerView.MessageHandler(this);

    private static class MessageHandler extends Handler {
        private final WeakReference<RecordControllerView> mView;

        MessageHandler(RecordControllerView view) {
            mView = new WeakReference<RecordControllerView>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            RecordControllerView view = mView.get();
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
//                    if (!view.mDragging && view.mShowing && view.mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
//                        sendMessageDelayed(msg, 500);
//                    }
                    break;
            }
        }
    }
}
