package com.cloudring.magic.camera.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import java.io.File;

public class PackageUtils {
    protected static final String TAG = PackageUtils.class.getSimpleName();

    /**
     * 安装apk应用
     */
    public static void installAPK(Context context, File apkFile) {
        if (apkFile.isFile()) {
            String fileName = apkFile.getName();
            String postfix = fileName.substring(fileName.length() - 4, fileName.length());
            if (postfix.toLowerCase().equals(".apk")) {
                String cmd = "chmod 755 " + apkFile.getAbsolutePath();
                try {
                    Runtime.getRuntime().exec(cmd);
                } catch (Exception e) {
                }
                Uri uri = Uri.fromFile(apkFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                context.startActivity(intent);
            }
        } else if (apkFile.isDirectory()) {
            File[] files = apkFile.listFiles();
            int fileCount = files.length;
            for (int i = 0; i < fileCount; i++) {
                installAPK(context, files[i]);
            }
        }
    }

    /**
     * 安装apk应用
     */
    public static void installDirApk(Context context, String filePath) {
        File file = new File(filePath);
        installAPK(context, file);
    }

    /**
     * 卸载apk文件
     */
    public static void uninstallPackage(Context context, Uri packageUri) {
        Intent intent = new Intent(Intent.ACTION_DELETE, packageUri);
        context.startActivity(intent);
    }

    /**
     * 根据包得到应用信息
     */
    public static ApplicationInfo getApplicationInfoByName(Context context, String packageName) {
        if (null == packageName || "".equals(packageName)) {
            return null;
        }
        try {
            return context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * 通过报名获取包信息
     */
    public static PackageInfo getPackageInfoByName(Context context, String packageName) {
        if (null == packageName || "".equals(packageName)) {
            return null;
        }
        try {
            return context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * 判断apk包是否安装
     */
    public static boolean isApkIntalled(Context context, String packageName) {
        return null != getApplicationInfoByName(context, packageName);
    }

    /**
     * 开启当前点击的应用
     */
    public static void startApplication(Context context, String packageName) {
        if (isApkIntalled(context, packageName)) {
            PackageManager packageManager = context.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(packageName);
            context.startActivity(intent);
        }
    }

    public static void startApplicationOfActivity(Context context, String apkName, String activityName,String bar1) {
        Log.e(TAG, "startApplicationOfActivity: 传递的参数："+apkName+"  "+activityName+"   "+bar1);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(new ComponentName(apkName, activityName));
        intent.putExtra("type", bar1);
        context.startActivity(intent);

    }
}
