package com.tsingsee.easyrtc.model;

import android.databinding.BaseObservable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Devices extends BaseObservable {

    @SerializedName("devices")
    private List<String> devices;

    public List<String> getDevices() {
        if (devices == null) {
            return new ArrayList<>();
        }
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
    }
}
