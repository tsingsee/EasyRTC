package com.tsingsee.easyrtc.model;

import android.databinding.BaseObservable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * 录像
 * */
public class LiveSessionModel extends BaseObservable implements Serializable {
    @SerializedName("SessionCount")
    private String sessionCount;

    @SerializedName("Sessions")
    private Sessions sessions;

    public String getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(String sessionCount) {
        this.sessionCount = sessionCount;
    }

    public Sessions getSessions() {
        return sessions;
    }

    public void setSessions(Sessions sessions) {
        this.sessions = sessions;
    }

    public static class Sessions {
        @SerializedName("Sessions")
        private List<Session> sessions;

        public List<Session> getSessions() {
            return sessions;
        }

        public void setSessions(List<Session> sessions) {
            this.sessions = sessions;
        }
    }

    public static class Session implements Serializable {
        @SerializedName("Application")
        private String application;

        @SerializedName("AudioBitrate")
        private String audioBitrate;

        @SerializedName("AudioChannel")
        private String audioChannel;

        @SerializedName("AudioCodec")
        private String audioCodec;

        @SerializedName("AudioSampleRate")
        private String audioSampleRate;

        @SerializedName("AudioSampleSize")
        private String audioSampleSize;

        @SerializedName("HLS")
        private String hls;

        @SerializedName("HTTP-FLV")
        private String httpFlv;

        @SerializedName("Id")
        private String id;

        @SerializedName("InBitrate")
        private int inBitrate;

        @SerializedName("InBytes")
        private String inBytes;

        @SerializedName("NumOutputs")
        private String numOutputs;

        @SerializedName("OutBitrate")
        private String outBitrate;

        @SerializedName("OutBytes")
        private String outBytes;

        @SerializedName("PublisherIP")
        private String publisherIP;

        @SerializedName("RTMP")
        private String rtmp;

        @SerializedName("RTSP")
        private String rtsp;

        @SerializedName("StartTime")
        private String startTime;

        @SerializedName("Time")
        private String time;

        @SerializedName("VideoBitrate")
        private String videoBitrate;

        @SerializedName("VideoCodec")
        private String videoCodec;

        @SerializedName("VideoHeight")
        private String videoHeight;

        @SerializedName("VideoWidth")
        private String videoWidth;

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
            if (TextUtils.isEmpty(id)) {
                return "";
            }

            return id;
        }

        public void setId(String id) {
            this.id = id;
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

        public String getNumOutputs() {
            return numOutputs;
        }

        public void setNumOutputs(String numOutputs) {
            this.numOutputs = numOutputs;
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

        public String getPublisherIP() {
            return publisherIP;
        }

        public void setPublisherIP(String publisherIP) {
            this.publisherIP = publisherIP;
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

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getVideoBitrate() {
            return videoBitrate;
        }

        public void setVideoBitrate(String videoBitrate) {
            this.videoBitrate = videoBitrate;
        }

        public String getVideoCodec() {
            return videoCodec;
        }

        public void setVideoCodec(String videoCodec) {
            this.videoCodec = videoCodec;
        }

        public String getVideoHeight() {
            return videoHeight;
        }

        public void setVideoHeight(String videoHeight) {
            this.videoHeight = videoHeight;
        }

        public String getVideoWidth() {
            return videoWidth;
        }

        public void setVideoWidth(String videoWidth) {
            this.videoWidth = videoWidth;
        }
    }
}
