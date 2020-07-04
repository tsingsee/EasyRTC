package com.tsingsee.easyrtc.model;

import android.databinding.BaseObservable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tsingsee.easyrtc.RTCApplication;
import com.tsingsee.easyrtc.tool.SharedHelper;

import java.io.Serializable;

public class Record extends BaseObservable implements Serializable {
    @SerializedName("start_time")
    private String startAt;     // 开始时间, YYYYMMDDHHmmss

    @SerializedName("hls")
    private String hls;         // 录像播放链接

    @SerializedName("duration")
    private double duration;    // 录像时长(秒)

    @SerializedName("snap")
    private String snap;

    private int startAtSecond;

    public String getHls() {
        if (hls.endsWith(".m3u8")) {
            if (hls.startsWith("http")) {
                return hls;
            }

////            SharedHelper helper = new SharedHelper(RTCApplication.getContext());
////            return helper.getURL() + hls;
//            return "https://demo.easyrtc.cn" + hls;

            SharedHelper helper = new SharedHelper(RTCApplication.getContext());
            Account account = helper.readAccount();
            String url = "http://" + account.getServerAddress() + ":" + account.getPort() + hls;
            return url;
        }

        return hls;
    }

    public void setHls(String hls) {
        this.hls = hls;
    }

    public String getSnap() {
        return snap;
    }

    public void setSnap(String snap) {
        this.snap = snap;
    }

    public String getStartAt() {
        if (TextUtils.isEmpty(startAt)) {
            return "";
        }
        
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getStartAtDesc() {
        if (startAt.length() == 14) {
            StringBuffer time = new StringBuffer();
            time.append(startAt);
            time.insert(4, "-");
            time.insert(7, "-");

            time.insert(10, " ");
            time.insert(13, ":");
            time.insert(16, ":");

            return time.toString();
        }

        return startAt;
    }

    public int getStartAtSecond() {
        startAtSecond = 0;

        if (getStartAt().length() == 14) {// 20180918000003
            int hour = Integer.parseInt(getStartAt().substring(8, 10));
            int minute = Integer.parseInt(getStartAt().substring(10, 12));
            int second = Integer.parseInt(getStartAt().substring(12, 14));

            startAtSecond = hour * 3600 + minute * 60 + second;
        }

        return startAtSecond;
    }

    public String getDurationDesc() {
        int time = (int) duration;

        String timeStr;

        int hour, minute, second;
        if (time <= 0) {
            return "00:00";
        } else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }

        return timeStr;
    }

    public String unitFormat(int i) {
        String retStr;

        if (i >= 0 && i < 10) {
            retStr = "0" + Integer.toString(i);
        } else {
            retStr = "" + i;
        }

        return retStr;
    }
}
