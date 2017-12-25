package com.magic.photo.photoviewlibrary.entity;

import java.io.Serializable;

import static android.R.attr.duration;

/**
 * @date 2017/6/27 11
 */
public class Image implements Serializable {
    public long id;
    public String path;
    public String size;
    public long createTime;
    public String mimeType;
    public String title;

    public Image(long id, String path, String size, long createTime, String mimeType, String title) {
        this.id = id;
        this.path = path;
        this.size = size;
        this.createTime = createTime;
        this.mimeType = mimeType;
        this.title = title;
    }

    @Override
    public String toString() {
        return "Image{" +
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
