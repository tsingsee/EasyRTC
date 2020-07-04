package com.tsingsee.easyrtc.model;

import android.databinding.BaseObservable;

import com.google.gson.annotations.SerializedName;

public class ServiceInfo extends BaseObservable {

    // 硬件平台
    @SerializedName("Hardware")
    private String hardware;

    // 机器码
    @SerializedName("RequestKey")
    private String requestKey;

    // 接口版本
    @SerializedName("InterfaceVersion")
    private String interfaceVersion;

    // 数量
    @SerializedName("LiveCount")
    private String liveCount;

    // 产品类型
    @SerializedName("ProductType")
    private String productType;

    // 运行时间
    @SerializedName("RunningTime")
    private String runningTime;

    // 服务
    @SerializedName("Server")
    private String server;

    // 有效
    @SerializedName("Validity")
    private String validity;

    // 虚拟数量
    @SerializedName("VirtualLiveCount")
    private String virtualLiveCount;

    // 版权
    @SerializedName("Copyright")
    private String copyright;

    // 版本
    @SerializedName("Version")
    private String version;

    // 创建时间
    @SerializedName("Build")
    private String build;

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

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }
}
