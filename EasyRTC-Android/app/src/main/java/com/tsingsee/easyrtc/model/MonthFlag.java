package com.tsingsee.easyrtc.model;

import android.databinding.BaseObservable;

import com.google.gson.annotations.SerializedName;

public class MonthFlag extends BaseObservable {

    @SerializedName("flags")
    private String flags;

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }
}
