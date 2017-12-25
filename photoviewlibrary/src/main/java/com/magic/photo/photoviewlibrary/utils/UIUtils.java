package com.magic.photo.photoviewlibrary.utils;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * description:
 * Created by luohaijun on 2016/11/25.
 */

public class UIUtils {

    private static Context mContext;

    private static Toast mToast = null;
    private static long    mMainThreadId;
    private static Handler mHandler;

    public static void init(Context context) {
        mContext = context;
        mHandler = new Handler();
        mMainThreadId = android.os.Process.myTid();
    }

    public static Context getContext() {
        return mContext;
    }

    /**
     * 显示底部提示框
     *
     * @param message
     */
    public static void showToast(String message) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(message);
        }
        mToast.show();
    }

    /**
     * 安全的执行一个task
     *
     * @return
     */
    public static void postTaskSafely(Runnable task) {
        int curThreadId = android.os.Process.myTid();
        if (curThreadId == mMainThreadId) {
            task.run();
        } else {
            mHandler.post(task);
        }
    }
}
