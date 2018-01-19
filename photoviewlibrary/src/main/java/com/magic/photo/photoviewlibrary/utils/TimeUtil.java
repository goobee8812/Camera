package com.magic.photo.photoviewlibrary.utils;

import android.annotation.SuppressLint;

/**
 * desc:
 * Created by FJTK_ZCQ on 2016/7/18.
 */
public class TimeUtil {

    @SuppressLint("DefaultLocale")
    public static String formatDuration(int duration) {
        float temp = ((float) duration) / 1000;
        duration = Math.round(temp);
        int minute = duration / 60;
        int hour = minute / 60;
        minute %= 60;
        int second = duration % 60;
        if (hour != 0)
            return String.format("%2d:%02d:%02d", hour, minute, second);
        else
            return String.format("%02d:%02d", minute, second);
    }
}
