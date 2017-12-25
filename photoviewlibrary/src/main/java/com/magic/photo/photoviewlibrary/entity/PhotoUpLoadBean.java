package com.magic.photo.photoviewlibrary.entity;

import com.magic.photo.photoviewlibrary.utils.GsonUtils;

import java.util.List;

/**
 * description:
 * Created by luohaijun on 2016/10/26.
 */

public class PhotoUpLoadBean {

    String userId;
    String imageUrls;
    String location;

    public PhotoUpLoadBean(String userId, List<String> pathList, String uploadAddress) {
        this.userId = userId;
        this.imageUrls = getPath(pathList);
        this.location = uploadAddress;
    }

    private String getPath(List<String> pathList) {
        String path = "";
        path= GsonUtils.toJson(pathList);
        return path;
    }

}
