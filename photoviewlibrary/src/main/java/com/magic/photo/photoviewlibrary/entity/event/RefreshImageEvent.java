package com.magic.photo.photoviewlibrary.entity.event;

import java.util.List;

/**
 * description:
 * Created by luohaijun on 2016/11/17.
 */

public class RefreshImageEvent {

    public int type;
    public boolean toRefresh;
    public List<String> pathList;

    public RefreshImageEvent(int type, boolean toRefresh, List<String> pathList) {
        this.type = type;
        this.toRefresh = toRefresh;
        this.pathList = pathList;
    }
}
