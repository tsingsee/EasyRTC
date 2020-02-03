package com.tsingsee.easyrtc.http;

import com.google.gson.annotations.SerializedName;

/**
 * 服务器通用返回数据格式
 */
public class BaseEntity2<E> {
    @SerializedName("code")
    private int code;

    @SerializedName("msg")
    private String msg;

    @SerializedName("data")
    private E data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }

    /**
     * 返回正确的状态码是0
     * */
    public boolean isSuccess() {
        return code == 0;
    }
}
