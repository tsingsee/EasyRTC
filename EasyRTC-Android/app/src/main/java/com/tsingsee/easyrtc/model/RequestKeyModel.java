package com.tsingsee.easyrtc.model;

import android.databinding.BaseObservable;

import com.google.gson.annotations.SerializedName;

public class RequestKeyModel extends BaseObservable {

    @SerializedName("RequestKey")
    private String requestKey;

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }
}
