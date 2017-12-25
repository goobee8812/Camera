package com.magic.photo.photoviewlibrary.entity.event;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * description:
 * Created by luohaijun on 2016/9/23.
 */

public class ImageListEvent {
    public HashMap<String, ArrayList<String>> mGroupMap;

    public ImageListEvent(HashMap<String, ArrayList<String>> mGroupMap) {
        this.mGroupMap = mGroupMap;
    }
}
