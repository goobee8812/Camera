package com.magic.photo.photoviewlibrary.entity.event;

import java.util.List;
import java.util.TreeMap;

/**
 * description:
 * Created by luohaijun on 2016/11/17.
 */

public class ImageStatusEvent {

    public TreeMap<String, List<Boolean>> statusMap;
    public boolean canEdit;
    public int groupPosition;
    public int childPosition;

    public ImageStatusEvent(TreeMap<String, List<Boolean>> statusMap, boolean canEdit, int groupPosition, int childPosition) {
        this.statusMap = statusMap;
        this.canEdit = canEdit;
        this.groupPosition = groupPosition;
        this.childPosition = childPosition;
    }
}
