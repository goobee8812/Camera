package com.magic.photo.photoviewlibrary.entity;

import java.util.List;

/**
 * description:图片实体类
 * Created by luohaijun on 2016/3/30.
 */
public class PhotoInfo {

    private long imgDate;
    private String fileName;
    private List<String> urlList;
    private int type;//文件类型
    private String thumb;//缩略图
    private boolean select;//选中状态

    public List<String> getUrlList() {
        return urlList;
    }

    public void setUrlList(List<String> urlList) {
        this.urlList = urlList;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getImgDate() {
        return imgDate;
    }

    public void setImgDate(long imgDate) {
        this.imgDate = imgDate;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getThumb() {
        if (thumb == null) {
            return "";
        }
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }
}
