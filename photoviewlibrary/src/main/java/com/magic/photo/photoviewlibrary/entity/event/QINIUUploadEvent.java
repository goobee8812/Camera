package com.magic.photo.photoviewlibrary.entity.event;

/**
 * description:
 * Created by luohaijun on 2016/10/26.
 */

public class QINIUUploadEvent {

    public int code;//错误代码
    public String message;//信息
    public double progress;//上传进度
    public int total;
    public int currentPosition;//当前上传位置

    public QINIUUploadEvent(int code, String message, double progress, int total, int currentPosition) {
        this.code = code;
        this.message = message;
        this.progress = progress;
        this.total = total;
        this.currentPosition = currentPosition;
    }
}
