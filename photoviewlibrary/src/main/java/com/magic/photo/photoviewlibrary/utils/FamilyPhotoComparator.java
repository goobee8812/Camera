package com.magic.photo.photoviewlibrary.utils;

import com.magic.photo.photoviewlibrary.entity.FamilyPhotoInfo;

import java.util.Comparator;

/**
 * description:家庭云相册列表排序
 * Created by luohaijun on 2016/11/2.
 */

public class FamilyPhotoComparator implements Comparator<FamilyPhotoInfo> {
    @Override
    public int compare(FamilyPhotoInfo lhs, FamilyPhotoInfo rhs) {
        long lhs_time = DateUtils.getLongTime(lhs.getUploadTime(), "yyyy-MM-dd HH:mm:ss");
        long rhs_time = DateUtils.getLongTime(rhs.getUploadTime(), "yyyy-MM-dd HH:mm:ss");
        if (lhs_time < rhs_time) {
            return 1;
        }
        return -1;
    }
}
