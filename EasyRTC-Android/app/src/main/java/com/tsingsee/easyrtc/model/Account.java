package com.tsingsee.easyrtc.model;

import android.text.TextUtils;

public class Account {

    public static final String IP = "58.221.222.101"; //"demo.easyrtc.cn";
    public static final String PORT = "10080";
    public static final String NAME = "1008";
    public static final String PWD = "admin";

    private String serverAddress;
    private String port;
    private String userName;
    private String pwd;
    private String token;

    public String getUserName() {
        if (TextUtils.isEmpty(userName)) {
            return NAME;
        }

        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPwd() {
        if (TextUtils.isEmpty(pwd)) {
            return PWD;
        }

        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getServerAddress() {
        if (TextUtils.isEmpty(serverAddress)) {
            return IP;
        }

        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPort() {
        if (TextUtils.isEmpty(port)) {
            return PORT;
        }

        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
