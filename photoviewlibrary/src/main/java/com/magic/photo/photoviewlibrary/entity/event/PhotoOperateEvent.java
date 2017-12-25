package com.magic.photo.photoviewlibrary.entity.event;

/**
 * description:
 * Created by luohaijun on 2016/9/29.
 */

public class PhotoOperateEvent {

    /**
     * 1、文件 2、文件夹
     */
    public int type;
    public int groupSize;
    public int selectSize;
    public boolean isShow;

    public PhotoOperateEvent(int type, int groupSize, int selectSize, boolean isShow) {
        this.type = type;
        this.groupSize = groupSize;
        this.selectSize = selectSize;
        this.isShow = isShow;
    }
}
