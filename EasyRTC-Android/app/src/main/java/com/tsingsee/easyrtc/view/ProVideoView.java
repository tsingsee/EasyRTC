package com.tsingsee.easyrtc.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.tsingsee.easyrtc.activity.LiveActivity;
import com.tsingsee.easyrtc.activity.Record.RecordTimeAxisActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.widget.media.IjkVideoView;

public class ProVideoView extends IjkVideoView implements VideoFullScreenMediaPlayerControl {
    private String mRecordPath;

    public ProVideoView(Context context) {
        super(context);
    }

    public ProVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ProVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public static void setKey(String key) {
        Player_KEY = key;
    }

    @Override
    public boolean isFullScreen() {
        if (getContext() instanceof LiveActivity){
            LiveActivity pro = (LiveActivity) getContext();
            return pro.isLandscape();
        } else if (getContext() instanceof RecordTimeAxisActivity) {
            RecordTimeAxisActivity pro = (RecordTimeAxisActivity) getContext();
            return pro.isLandscape();
        }

        return false;
    }

    @Override
    public void toggleFullScreen() {
        if (getContext() instanceof LiveActivity){
//            LiveActivity pro = (LiveActivity) getContext();
//            pro.onChangeOrientation(null);
        } else if (getContext() instanceof RecordTimeAxisActivity){
            RecordTimeAxisActivity pro = (RecordTimeAxisActivity) getContext();
            pro.onChangeOrientation(null);
        }
    }

    @Override
    public boolean recordEnable() {
        Uri uri = mUri;
        if (uri == null)
            return false;
        if (uri.getScheme() == null)
            return false;

        return !uri.getScheme().equals("file");
    }

    @Override
    public boolean speedCtrlEnable() {
        Uri uri = mUri;
        if (uri == null)
            return false;
        if (uri.getScheme() == null)
            return true;

        return uri.getScheme().equals("file");
    }

    @Override
    public boolean isRecording() {
        if (mMediaPlayer == null){
            return false;
        }

        return !TextUtils.isEmpty(mRecordPath);
    }

    @Override
    public void reStart(){
        super.reStart();
        if (mRecordPath != null){
            toggleRecord();
            toggleRecord();
        }
    }

    @Override
    public void toggleRecord() {
        if (getContext() instanceof LiveActivity) {
            LiveActivity pro = (LiveActivity) getContext();
            if (ActivityCompat.checkSelfPermission(pro, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(pro, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, LiveActivity.REQUEST_WRITE_STORAGE +1);
                return;
            }
        } else if (getContext() instanceof RecordTimeAxisActivity) {
            RecordTimeAxisActivity pro = (RecordTimeAxisActivity) getContext();
            if (ActivityCompat.checkSelfPermission(pro, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(pro, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RecordTimeAxisActivity.REQUEST_WRITE_STORAGE +1);
                return;
            }
        }

        if (!isRecording()) {
            Uri uri = mUri;
            if (uri == null)
                return;

            mRecordPath = "record_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".mp4";
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            try {
                directory.mkdirs();
                startRecord(directory + "/" + mRecordPath, 30);
            }catch (Exception ex){
                ex.printStackTrace();
                mRecordPath = null;
            }
        } else {
            stopRecord();
        }
    }

    @Override
    public float getSpeed() {
        if (mMediaPlayer == null) {
            return 1.0f;
        }

        if (mMediaPlayer instanceof IjkMediaPlayer){
            IjkMediaPlayer player = (IjkMediaPlayer) mMediaPlayer;
            return player.getSpeed();
        }

        return 1.0f;
    }

    @Override
    public void setSpeed(float speed) {
        if (mMediaPlayer == null ){
            return ;
        }

        if (mMediaPlayer instanceof IjkMediaPlayer){
            IjkMediaPlayer player = (IjkMediaPlayer) mMediaPlayer;
            player.setSpeed(speed);
        }
    }

    @Override
    public void takePicture() {
        if (getContext() instanceof LiveActivity) {
            LiveActivity pro = (LiveActivity) getContext();
            pro.onTakePicture(null);
        } else if (getContext() instanceof RecordTimeAxisActivity) {
//            RecordTimeAxisActivity pro = (RecordTimeAxisActivity) getContext();
//            pro.onTakePicture(null);
        }
    }

    @Override
    public void toggleMode() {
        if (getContext() instanceof LiveActivity) {
            LiveActivity pro = (LiveActivity) getContext();
            pro.onChangePlayMode(null);
        } else if (getContext() instanceof RecordTimeAxisActivity) {
            RecordTimeAxisActivity pro = (RecordTimeAxisActivity) getContext();
            pro.onChangePlayMode(null);
        }
    }

    @Override
    public boolean isCompleted() {
        if (mMediaPlayer instanceof IjkMediaPlayer) {
            IjkMediaPlayer player = (IjkMediaPlayer) mMediaPlayer;
            return player.isCompleted();
        }
        return false;
    }

    @Override
    public void openAudio() {
//        setVolume(1,1);
    }

    @Override
    public void closeAudio() {
//        setVolume(0, 0);
    }

    @Override
    public void ptzcontrol(String command) {
        if (getContext() instanceof LiveActivity) {
//            LiveActivity pro = (LiveActivity) getContext();
//            pro.ptzcontrol(command);
        }
    }

    public void startRecord(String path, int seconds) {
        if (mMediaPlayer == null){
            return;
        }

        super.startRecord(path, seconds);
        mRecordPath = path;
    }

    public void stopRecord() {
        if (mMediaPlayer == null) {
            return;
        }

        super.stopRecord();
        mRecordPath = null;
    }

    // 返回直播界面
    @Override
    public void backLive() {
        if (getContext() instanceof RecordTimeAxisActivity) {
            RecordTimeAxisActivity pro = (RecordTimeAxisActivity) getContext();
            pro.backLive();
        }
    }

    // 下载当然录像段
    @Override
    public void downloadRecord() {
        if (getContext() instanceof RecordTimeAxisActivity) {
            RecordTimeAxisActivity pro = (RecordTimeAxisActivity) getContext();
            pro.downloadRecord();
        }
    }

    // 去视图列表
    @Override
    public void recordList() {
        if (getContext() instanceof RecordTimeAxisActivity) {
            RecordTimeAxisActivity pro = (RecordTimeAxisActivity) getContext();
            pro.recordList();
        }
    }

    // 删除当然录像段
    @Override
    public void deleteRecord() {
        if (getContext() instanceof RecordTimeAxisActivity) {
            RecordTimeAxisActivity pro = (RecordTimeAxisActivity) getContext();
            pro.deleteRecord();
        }
    }

    @Override
    public void selectDate(int value) {
        if (getContext() instanceof RecordTimeAxisActivity) {
            RecordTimeAxisActivity pro = (RecordTimeAxisActivity) getContext();
            pro.selectDate(value);
        }
    }

    @Override
    public void scaleScroll(double scale) {
        if (getContext() instanceof RecordTimeAxisActivity) {
            RecordTimeAxisActivity pro = (RecordTimeAxisActivity) getContext();
            pro.scaleScroll(scale);
        }
    }
}
