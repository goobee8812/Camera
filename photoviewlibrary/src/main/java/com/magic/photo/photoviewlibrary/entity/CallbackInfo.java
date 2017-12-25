package com.magic.photo.photoviewlibrary.entity;

/**
 * description:服务器返回信息
 * Created by luohaijun on 2016/10/24.
 */

public class CallbackInfo<T> {

    private String code = "-1";
    private String message;
    private T data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
