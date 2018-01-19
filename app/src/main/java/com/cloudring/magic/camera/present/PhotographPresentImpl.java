package com.cloudring.magic.camera.present;

import android.app.Activity;
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
    public void takePhoto(Camera camera, final ZXPhotographActivity activity) {

        /**
         * 拍照实例
         */
        final Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
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
//                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//
//                            final Bitmap modBm = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
//
//                            Canvas canvas = new Canvas(modBm);
//
//                            Paint paint = new Paint();
//                            Matrix matrix = new Matrix();
//                            matrix.setScale(-1, 1);//翻转
//                            matrix.postTranslate(bitmap.getWidth(), 0);
//
//                            canvas.drawBitmap(bitmap, matrix, paint);

                            FileOutputStream fos = new FileOutputStream(pictureName);
//                            modBm.compress(Bitmap.CompressFormat.PNG, 90, fos);
                            fos.write(data);
                            fos.flush();
                            fos.close();

//                            // 把文件插入到系统图库
//                            try {
//                                MediaStore.Images.Media.insertImage(activity.getContentResolver(), pictureName, pictureName, null);
//                            } catch (FileNotFoundException e) {
//                                e.printStackTrace();
//                            }
//                            // 通知图库更新
                            activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + pictureName)));
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.refreshPhotoOne();
                                }
                            });
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Log.d(TAG, "File not found: " + e.getMessage());
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Error accessing file: " + e.getMessage());
                        }
                    }
                }).start();
                if (camera != null){
                    camera.startPreview();//拍照完毕以后需要再次开启preview以保证拍照以后继续给surfaceView传递摄像数据
                }
            }
        };
        if (camera != null) {
            camera.autoFocus(new Camera.AutoFocusCallback() {

                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        camera.takePicture(null, null, mPictureCallback);
                    }else{

                    }
                }
            });

        }

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
    public void takePhotoDelay(final int delaySec, final Camera camera, final ZXPhotographActivity activity) {
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


}

