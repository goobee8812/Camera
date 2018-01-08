package com.cloudring.magic.camera.utils;

/**
 * 单例模式Toast.用于及时显示toast
 * ToastUtilKe, create() before use.
 *
 */


import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtilKe {
    private static Toast toast;
    private static Resources res;

    public static void init(Context context) {
        toast = Toast.makeText(context, "", Toast.LENGTH_LONG);
        toast.show();
        toast.cancel();
        res = context.getResources();
    }

    public static void showInCenter() {
        toast.setGravity(Gravity.CENTER, 0, 0);
    }

    public static void cancel() {
        toast.cancel();
    }

    public static void show(int resId) {
        show(res.getString(resId));
    }

    public static void show(String str) {
        if (toast == null) {
            throw new NullPointerException(
                    "Call Lutil.create(Context) or ToastUtilKe.create(Context) first !!!");
        }
        toast.setText(str);
        toast.show();
    }
}
