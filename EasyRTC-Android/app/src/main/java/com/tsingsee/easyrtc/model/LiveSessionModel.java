package com.tsingsee.easyrtc.model;

import android.databinding.BaseObservable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * 录像
 */
public class LiveSessionModel extends BaseObservable implements Serializable {

    // 应用名称
    @SerializedName("application")
    private String application;

    // 音频率
    @SerializedName("audioBitrate")
    private String audioBitrate;

    // HLS地址
    @SerializedName("hls")
    private String hls;

    // HTTP-FLV地址
    @SerializedName("httpFlv")
    private String httpFlv;

    // 推流会议室ID
    @SerializedName("id")
    private String id;

    // 会议室名称
    @SerializedName("name")
    private String name;

    // 是否在录像
    private boolean recording;

    // 推送码率
    @SerializedName("inBitrate")
    private int inBitrate;

    // 推送流量
    @SerializedName("inBytes")
    private String inBytes;

    // 输出码率
    @SerializedName("outBitrate")
    private String outBitrate;

    // 输出流量
    @SerializedName("outBytes")
    private String outBytes;

    // RTMP直播地址，前端需要将地址替换成域名
    @SerializedName("rtmp")
    private String rtmp;

    // RTSP直播地址，前端需要将地址替换成域名
    @SerializedName("rtsp")
    private String rtsp;

    // 直播时长
    @SerializedName("Time")
    private String time;

    // 开始时间, YYYYMMDDHHmmss
    @SerializedName("StartTime")
    private String startTime;

    // 音频码率
    @SerializedName("VideoBitrate")
    private String videoBitrate;

    @SerializedName("audioChannel")
    private String audioChannel;

    @SerializedName("audioCodec")
    private String audioCodec;

    @SerializedName("audioSampleRate")
    private String audioSampleRate;

    @SerializedName("audioSampleSize")
    private String audioSampleSize;

//    @SerializedName("NumOutputs")
//    private String numOutputs;
//
//    @SerializedName("PublisherIP")
//    private String publisherIP;
//
//    @SerializedName("VideoCodec")
//    private String videoCodec;
//
//    @SerializedName("VideoHeight")
//    private String videoHeight;
//
//    @SerializedName("VideoWidth")
//    private String videoWidth;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getAudioBitrate() {
        return audioBitrate;
    }

    public void setAudioBitrate(String audioBitrate) {
        this.audioBitrate = audioBitrate;
    }

    public String getHls() {
        return hls;
    }

    public void setHls(String hls) {
        this.hls = hls;
    }

    public String getHttpFlv() {
        return httpFlv;
    }

    public void setHttpFlv(String httpFlv) {
        this.httpFlv = httpFlv;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public int getInBitrate() {
        return inBitrate;
    }

    public void setInBitrate(int inBitrate) {
        this.inBitrate = inBitrate;
    }

    public String getInBytes() {
        return inBytes;
    }

    public void setInBytes(String inBytes) {
        this.inBytes = inBytes;
    }

    public String getOutBitrate() {
        return outBitrate;
    }

    public void setOutBitrate(String outBitrate) {
        this.outBitrate = outBitrate;
    }

    public String getOutBytes() {
        return outBytes;
    }

    public void setOutBytes(String outBytes) {
        this.outBytes = outBytes;
    }

    public String getRtmp() {
        return rtmp;
    }

    public void setRtmp(String rtmp) {
        this.rtmp = rtmp;
    }

    public String getRtsp() {
        return rtsp;
    }

    public void setRtsp(String rtsp) {
        this.rtsp = rtsp;
    }

    public String getTime() {
        if (TextUtils.isEmpty(time)) {
            return "0";
        }

        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getVideoBitrate() {
        return videoBitrate;
    }

    public void setVideoBitrate(String videoBitrate) {
        this.videoBitrate = videoBitrate;
    }

    public String getAudioChannel() {
        return audioChannel;
    }

    public void setAudioChannel(String audioChannel) {
        this.audioChannel = audioChannel;
    }

    public String getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(String audioCodec) {
        this.audioCodec = audioCodec;
    }

    public String getAudioSampleRate() {
        return audioSampleRate;
    }

    public void setAudioSampleRate(String audioSampleRate) {
        this.audioSampleRate = audioSampleRate;
    }

    public String getAudioSampleSize() {
        return audioSampleSize;
    }

    public void setAudioSampleSize(String audioSampleSize) {
        this.audioSampleSize = audioSampleSize;
    }
}
