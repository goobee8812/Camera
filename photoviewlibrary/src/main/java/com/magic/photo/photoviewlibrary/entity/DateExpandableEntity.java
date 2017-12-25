package com.magic.photo.photoviewlibrary.entity;

import java.util.List;
import java.util.Map;

/**
 * description:相册-日期列表
 * Created by luohaijun on 2016/11/23.
 */

public class DateExpandableEntity {

    private List<String> groupList;

    private Map<String, List<String>> childList;

    public List<String> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<String> groupList) {
        this.groupList = groupList;
    }

    public Map<String, List<String>> getChildList() {
        return childList;
    }

    public void setChildList(Map<String, List<String>> childList) {
        this.childList = childList;
    }
}
