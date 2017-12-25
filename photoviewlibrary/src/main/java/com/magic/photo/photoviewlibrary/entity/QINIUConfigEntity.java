package com.magic.photo.photoviewlibrary.entity;

/**
 * description:七牛配置信息实体类
 * Created by luohaijun on 2016/10/24.
 */

public class QINIUConfigEntity {

    private String accessKey;
    private String secretKey;
    private String accessUrl;
    private String token;

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
