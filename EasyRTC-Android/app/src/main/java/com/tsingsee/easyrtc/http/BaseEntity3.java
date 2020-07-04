package com.tsingsee.easyrtc.http;

import com.google.gson.annotations.SerializedName;

/**
 * 服务器通用返回数据格式
 */
public class BaseEntity3<E> {
    @SerializedName("code")
    private int code;

    @SerializedName("msg")
    private E msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public E getMsg() {
        return msg;
    }

    public void setMsg(E msg) {
        this.msg = msg;
    }

    /**
     * 返回正确的状态码是0
     * */
    public boolean isSuccess() {
        return code == 200;
    }
}
