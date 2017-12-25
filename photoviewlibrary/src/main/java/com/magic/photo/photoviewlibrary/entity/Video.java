package com.magic.photo.photoviewlibrary.entity;

import java.io.Serializable;

/**
 * 2017/6/24 15
 */
public class Video implements Serializable {
    public long   id;
    public String path;
    public String size;
    public long duration;
    public long   createTime;
    public String mimeType;
    public String title;

    public Video(long id, String path, String size, long duration, long createTime, String mimeType, String title) {
        this.id = id;
        this.path = path;
        this.size = size;
        this.duration = duration;
        this.createTime = createTime;
        this.mimeType = mimeType;
        this.title = title;
    }

    @Override
    public String toString() {
        return "Video{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", size='" + size + '\'' +
                ", duration='" + duration + '\'' +
                ", createTime=" + createTime +
                ", mimeType='" + mimeType + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
