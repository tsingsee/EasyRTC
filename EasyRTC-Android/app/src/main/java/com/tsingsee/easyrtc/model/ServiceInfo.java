package com.tsingsee.easyrtc.model;

import android.databinding.BaseObservable;

import com.google.gson.annotations.SerializedName;

public class ServiceInfo extends BaseObservable {

    @SerializedName("Hardware")
    private String hardware;

    @SerializedName("InterfaceVersion")
    private String interfaceVersion;

    @SerializedName("LiveCount")
    private String liveCount;

    @SerializedName("ProductType")
    private String productType;

    @SerializedName("RunningTime")
    private String runningTime;

    @SerializedName("Server")
    private String server;

    @SerializedName("Validity")
    private String validity;

    @SerializedName("VirtualLiveCount")
    private String virtualLiveCount;

    public String getHardware() {
        return hardware;
    }

    public void setHardware(String hardware) {
        this.hardware = hardware;
    }

    public String getInterfaceVersion() {
        return interfaceVersion;
    }

    public void setInterfaceVersion(String interfaceVersion) {
        this.interfaceVersion = interfaceVersion;
    }

    public String getLiveCount() {
        return liveCount;
    }

    public void setLiveCount(String liveCount) {
        this.liveCount = liveCount;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(String runningTime) {
        this.runningTime = runningTime;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getValidity() {
        return validity;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    public String getVirtualLiveCount() {
        return virtualLiveCount;
    }

    public void setVirtualLiveCount(String virtualLiveCount) {
        this.virtualLiveCount = virtualLiveCount;
    }
}
