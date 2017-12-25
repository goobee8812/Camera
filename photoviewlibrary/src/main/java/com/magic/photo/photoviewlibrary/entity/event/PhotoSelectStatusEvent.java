package com.magic.photo.photoviewlibrary.entity.event;

import java.util.List;

/**
 * description:相册文件选中状态
 * Created by luohaijun on 2016/10/9.
 */

public class PhotoSelectStatusEvent {

    public List<Boolean> statusList;

    public PhotoSelectStatusEvent(List<Boolean> statusList) {
        this.statusList = statusList;
    }
}
