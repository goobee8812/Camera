package com.magic.photo.photoviewlibrary.utils;

import java.util.Comparator;

/**
 * description:
 * Created by luohaijun on 2016/11/23.
 */

public class DateComparator implements Comparator<String> {
    @Override
    public int compare(String o, String t1) {

        long date1 = DateUtils.getLongTime(o, "yyyyMM");
        long date2 = DateUtils.getLongTime(t1, "yyyyMM");
        if (date1 < date2) {
            return 1;
        }
        return -1;
    }
}
