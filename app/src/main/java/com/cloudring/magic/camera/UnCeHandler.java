package com.cloudring.magic.camera;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.cloudring.magic.camera.utils.SpUtil;

import java.io.File;

/**
 * Created by BB on 2017/11/25.
 */

public class UnCeHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    public static final String TAG = "CatchExcep";
    private Context context;

    public UnCeHandler(Context context) {
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "error : ", e);
            }

            String videoPath = SpUtil.readString("videoPath");
            if (!TextUtils.isEmpty(videoPath)) {
                File file = new File(videoPath);
                //将合成的视频复制过来
                if (file.exists()) {
                    file.delete();
                    SpUtil.writeString("videoPath", "");
                }
            }
//            Intent intent = new Intent(application.getApplicationContext(), ZXPhotographActivity.class);
//            @SuppressLint("WrongConstant") PendingIntent restartIntent = PendingIntent.getActivity(
//                    application.getApplicationContext(), 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
//            //退出程序
//            AlarmManager mgr = (AlarmManager) application.getSystemService(Context.ALARM_SERVICE);
//            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
//                    restartIntent); // 1秒钟后重启应用
            System.exit(0);
        }
    }


    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }

        System.out.println(".......");
        ex.printStackTrace();
        //使用Toast来显示异常信息
//        new Thread(){
//            @Override
//            public void run() {
//                Looper.prepare();
//                Toast.makeText(application.getApplicationContext(), "很抱歉,程序出现异常,即将退出.",
//                        Toast.LENGTH_SHORT).show();
//                Looper.loop();
//            }
//        }.start();
        return true;
    }
}
