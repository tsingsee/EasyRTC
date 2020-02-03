package com.tsingsee.easyrtc.view;

import android.widget.MediaController;

public interface VideoFullScreenMediaPlayerControl extends MediaController.MediaPlayerControl {
    boolean isFullScreen();
    void toggleFullScreen();

    void closeAudio();
    void openAudio();

    void ptzcontrol(String command);

    boolean speedCtrlEnable();
    boolean recordEnable();
    boolean isRecording();
    void toggleRecord();

    float getSpeed();
    void setSpeed(float speed);

    void takePicture();
    void toggleMode();

    boolean isCompleted();

    // 返回直播界面
    void backLive();
    // 下载当然录像段
    void downloadRecord();
    // 去视图列表
    void recordList();
    // 删除当然录像段
    void deleteRecord();
    // 调整播放进度
    void scaleScroll(double scale);

    /**
     * 选择日期
     * value: 0弹出日历 -1前一天 1后一天
     * */
    void selectDate(int value);
}
