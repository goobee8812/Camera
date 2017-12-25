package com.magic.photo.photoviewlibrary.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * description:
 * Created by luohaijun on 2016/11/2.
 */

public class DateUtils {

    /**
     * @param time
     * @param pattern
     * @return
     */
    public static long getLongTime(String time, String pattern) {
        long currentTime = 0;
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            Date date = format.parse(time);
            currentTime = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return currentTime;
    }

    /**
     * @param timeStr long型时间 1453446769000
     * @param pattern
     * @return
     */
    public static String getFormaterTime(long timeStr, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date date = new Date(timeStr);
        return sdf.format(date);
    }

    public static String getFormaterTime(String timeStr, String pattern, String currentPattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            Date date = format.parse(timeStr);
            format = new SimpleDateFormat(currentPattern);
            String tempTime = format.format(date);
            return tempTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前时间
     *
     * @param pattern
     * @return
     */
    public static String getCurrentTime(String pattern) {
        String time = "";
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }
}
