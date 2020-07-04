package com.tsingsee.easyrtc.model;

import java.io.Serializable;

public class UploadBean implements Serializable {
    private String id;
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

