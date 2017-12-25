package com.magic.photo.photoviewlibrary.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * Created by luohaijun on 2016/11/2.
 */

public class StringUtils {

    /**
     * 去除文件路径，保留名称
     *
     * @param mList
     * @param path
     * @return
     */
    public static List<String> getDisposeList(List<String> mList, String path) {
        List<String> tempList = new ArrayList<>();
        String tempPath = "";
        for (int i = 0; i < mList.size() - 1; i++) {
            tempPath = disposeString(mList.get(i));
            tempList.add(path + tempPath);
        }
        return tempList;
    }

    private static String disposeString(String str) {
        if (str.contains("/")) {
            str = str.substring(str.lastIndexOf("/") + 1);
        }
        return str;
    }
}
