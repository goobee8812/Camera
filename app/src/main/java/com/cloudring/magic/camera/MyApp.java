package com.cloudring.magic.camera;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by BB on 2018/1/8.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CrashReport.initCrashReport(getApplicationContext(), "d5ce68976c", true);


        //设置该CrashHandler为程序的默认处理器
        UnCeHandler catchExcep = new UnCeHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(catchExcep);
    }
}
