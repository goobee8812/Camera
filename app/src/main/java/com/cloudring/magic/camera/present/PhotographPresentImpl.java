package com.cloudring.magic.camera.present;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.format.DateFormat;
import android.util.Log;

import com.cloudring.magic.camera.CustomRecordActivity;
import com.cloudring.magic.camera.MyApp;
import com.cloudring.magic.camera.PhotographActivity;
import com.cloudring.magic.camera.ZXPhotographActivity;
import com.cloudring.magic.camera.utils.ToastUtilKe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class PhotographPresentImpl implements PhotographPresent {
    public static final String TAG = "PhotographPresentImpl";
    public static boolean takePhotoLock = false;
    public static boolean isPhotoStopThread = false;


    @Override
    public void takePhoto(Camera camera, final PhotographActivity activity) {
        /**
         * 拍照实例
         */
        Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                final String pictureDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Camera";
                if (pictureDir == null) {
                    Log.d(TAG, "Error creating media file, check storage permissions!");
                    return;
                }
                File f = new File(pictureDir);
                if (!f.exists()) {
                    f.mkdir();
                }
                final String pictureName = pictureDir + File.separator + DateFormat.format("yyyyMMddHHmmss", new Date()).toString() + ".png";

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FileOutputStream fos = new FileOutputStream(pictureName);
                            fos.write(data);
                            fos.close();
                            // 把文件插入到系统图库
                            try {
                                MediaStore.Images.Media.insertImage(activity.getContentResolver(),
                                        pictureDir, pictureName, null);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            // 通知图库更新
                            activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + pictureName)));
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.refreshPhotoOne(activity.ivPhotoAlbum, new File(pictureName));
                                    activity.setResult(1001);
                                }
                            });
                        } catch (FileNotFoundException e) {
                            Log.d(TAG, "File not found: " + e.getMessage());
                        } catch (IOException e) {
                            Log.d(TAG, "Error accessing file: " + e.getMessage());
                        }
                    }
                }).start();
                camera.startPreview();//拍照完毕以后需要再次开启preview以保证拍照以后继续给surfaceView传递摄像数据
            }
        };
        camera.takePicture(null, null, mPictureCallback);
    }

    @Override
    public void takePhoto(Camera camera, final ZXPhotographActivity activity, final SaveCallback callback) {

        /**
         * 拍照实例
         */
        final Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, final Camera camera) {
                final String pictureDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Camera";
                File f = new File(pictureDir);
                if (!f.exists()) {
                    f.mkdir();
                }
                final String pictureName = pictureDir + File.separator + DateFormat.format("yyyyMMddHHmmss", new Date()).toString() + ".png";
                try {
                    FileOutputStream fos = new FileOutputStream(pictureName);
                    fos.write(data);
                    fos.flush();
                    fos.close();
                    camera.startPreview();//拍照完毕以后需要再次开启preview以保证拍照以后继续给surfaceView传递摄像数据
                    // 通知图库更新
                    updateMediaStore(pictureName);
                    callback.success(pictureName);
                } catch (Exception e) {
                    e.printStackTrace();
                    camera.startPreview();
                    callback.onError(e);
                }
            }
        };
        camera.takePicture(null, null, mPictureCallback);
    }


    public void updateMediaStore(String path) {
        ContentResolver cr = MyApp.getContext().getContentResolver();
        ContentValues values = new ContentValues();
        String type = "image/png";
        if (type.startsWith("audio/")) {
            values.put(MediaStore.Audio.Media.DATA, path);
            cr.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
            // LocalResource.getInstance().getPhoneMusics();
        } else if (type.startsWith("video/")) {
            values.put(MediaStore.Video.Media.DATA, path);
            cr.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            // LocalResource.getInstance().getPhoneVideos();
        } else if (type.startsWith("image/")) {
            values.put(MediaStore.Images.Media.DATA, path);
            cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            //LocalResource.getInstance().getPhoneImages();
        } else if (type.startsWith("text/") || type.equals("application/msword") || type.equals("application/vnd.ms-excel")) {
            values.put("_data", path);
            cr.insert(MediaStore.Files.getContentUri("external"), values);
            //LocalResource.getInstance().getPhoneDocuments();
        }
    }


    /**
     * 根据文件后缀名获得对应的MIME类型。
     *
     * @param path
     */
    public String getMIMEType(String path) {
        String type = "*/*";
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = path.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        String[][] MIME_MapTable =
                {
                        {".3gp", "video/3gpp"},
                        {".apk", "application/vnd.android.package-archive"},
                        {".asf", "video/x-ms-asf"},
                        {".avi", "video/x-msvideo"},
                        {".bin", "application/octet-stream"},
                        {".bmp", "image/bmp"},
                        {".c", "text/plain"},
                        {".class", "application/octet-stream"},
                        {".conf", "text/plain"},
                        {".cpp", "text/plain"},
                        {".doc", "application/msword"},
                        {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
                        {".xls", "application/vnd.ms-excel"},
                        {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
                        {".exe", "application/octet-stream"},
                        {".gif", "image/gif"},
                        {".gtar", "application/x-gtar"},
                        {".gz", "application/x-gzip"},
                        {".h", "text/plain"},
                        {".htm", "text/html"},
                        {".html", "text/html"},
                        {".jar", "application/java-archive"},
                        {".java", "text/plain"},
                        {".jpeg", "image/jpeg"},
                        {".jpg", "image/jpeg"},
                        {".js", "application/x-javascript"},
                        {".log", "text/plain"},
                        {".m3u", "audio/x-mpegurl"},
                        {".m4a", "audio/mp4a-latm"},
                        {".m4b", "audio/mp4a-latm"},
                        {".m4p", "audio/mp4a-latm"},
                        {".m4u", "video/vnd.mpegurl"},
                        {".m4v", "video/x-m4v"},
                        {".mov", "video/quicktime"},
                        {".mp2", "audio/x-mpeg"},
                        {".mp3", "audio/x-mpeg"},
                        {".mp4", "video/mp4"},
                        {".mpc", "application/vnd.mpohun.certificate"},
                        {".mpe", "video/mpeg"},
                        {".mpeg", "video/mpeg"},
                        {".mpg", "video/mpeg"},
                        {".mpg4", "video/mp4"},
                        {".mpga", "audio/mpeg"},
                        {".msg", "application/vnd.ms-outlook"},
                        {".ogg", "audio/ogg"},
                        {".pdf", "application/pdf"},
                        {".png", "image/png"},
                        {".pps", "application/vnd.ms-powerpoint"},
                        {".ppt", "application/vnd.ms-powerpoint"},
                        {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
                        {".prop", "text/plain"},
                        {".rc", "text/plain"},
                        {".rmvb", "audio/x-pn-realaudio"},
                        {".rtf", "application/rtf"},
                        {".sh", "text/plain"},
                        {".tar", "application/x-tar"},
                        {".tgz", "application/x-compressed"},
                        {".txt", "text/plain"},
                        {".wav", "audio/x-wav"},
                        {".wma", "audio/x-ms-wma"},
                        {".wmv", "audio/x-ms-wmv"},
                        {".wps", "application/vnd.ms-works"},
                        {".xml", "text/plain"},
                        {".z", "application/x-compress"},
                        {".zip", "application/x-zip-compressed"},
                        {"", "*/*"}
                };
        /* 获取文件的后缀名 */
        String end = path.substring(dotIndex).toLowerCase();
        if (end == "") return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0; i < MIME_MapTable.length; i++) { //MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;

    }


    @Override
    public void getSystemPhoto(Activity activity) {
        File outputImage = new File(activity.getExternalCacheDir(), "output_image.jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Uri imageUri;
        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(activity,
                    "com.gyq.cameraalbumtest.fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activity.startActivityForResult(intent, PhotographActivity.TAKE_PHOTO);
    }


    @Override
    public void reCording(Activity activity) {
        Intent intent = new Intent(activity, CustomRecordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @Override
    public void takePhotoDelay(final int delaySec, final Camera camera, final PhotographActivity activity) {
        final int[] time = {delaySec};
        final Timer timer = new Timer();
        ToastUtilKe.init(activity);
        if (!takePhotoLock) {//如果拍摄没有重复开启线程
            ToastUtilKe.show("倒计时" + delaySec);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!isPhotoStopThread) {//如果此时线程没有因为异常停止就继续
                        time[0]--;
                        if (time[0] != 0) {//倒计时时间没到的时候使用Toast显示
                            activity.runOnUiThread(new Runnable() {  //在UI线程调用Toast
                                @Override
                                public void run() {
                                    ToastUtilKe.show("倒计时" + time[0]);
                                }
                            });
                        }
                        if (time[0] == 0) {//如果现在已经达到了设定好的延迟时间
                            takePhoto(camera, activity);
                            takePhotoLock = false;
                            timer.cancel();
                        }
                    } else {
                        takePhotoLock = false;
                        timer.cancel();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtilKe.cancel();
                            }
                        });
                    }
                }
            }, 1000, 1000);
        }
    }

    @Override
    public void takePhotoDelay(final int delaySec, final Camera camera, final ZXPhotographActivity activity, final SaveCallback callback) {
        final int[] time = {delaySec};
        final Timer timer = new Timer();
        ToastUtilKe.init(activity);
        if (!takePhotoLock) {//如果拍摄没有重复开启线程
            ToastUtilKe.show("倒计时" + delaySec);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!isPhotoStopThread) {//如果此时线程没有因为异常停止就继续
                        time[0]--;
                        if (time[0] != 0) {//倒计时时间没到的时候使用Toast显示
                            activity.runOnUiThread(new Runnable() {  //在UI线程调用Toast
                                @Override
                                public void run() {
                                    ToastUtilKe.show("倒计时" + time[0]);
                                }
                            });
                        }
                        if (time[0] == 0) {//如果现在已经达到了设定好的延迟时间
                            takePhoto(camera, activity, callback);
                            takePhotoLock = false;
                            timer.cancel();
                        }
                    } else {
                        takePhotoLock = false;
                        timer.cancel();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtilKe.cancel();
                            }
                        });
                    }
                }
            }, 1000, 1000);
        }
    }


}

