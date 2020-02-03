package com.tsingsee.easyrtc.model;

import android.text.TextUtils;

public class Account {

    public static final String PWD = "1111";

    private String userName;
    private String pwd;

    public String getUserName() {
        if (TextUtils.isEmpty(userName)) {
            return "1008";
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
}
