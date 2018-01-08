package com.cloudring.magic.camera.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by 羊柯 on 2017/5/31.
 */

public class ScanPhoto {
    static final String TAG = "ScanPhoto";
    private static ArrayList<PhotoEntity> allPhotoArrayList;

    private static ScanPhoto instance;
    private Context context;
    private ScanPhoto.LookUpPhotosCallback completeCallback;

    private ScanPhoto(Context context) {
        this.context = context;
    }

    public static ScanPhoto getInstance(Context context) {
        if (instance == null) {
            synchronized (ScanPhoto.class) {
                if (instance == null) {
                    instance = new ScanPhoto(context);
                }
            }
        }
        return instance;
    }


    /**
     * 从系统相册里面取出图片的uri
     * 并且为entity存入图片的时间戳和url属性
     */
    public void getPhotoFromLocalStorage() {

        new MyAlxMultiTask().executeDependSDK();
    }

    private class MyAlxMultiTask extends AlxMultiTask<Void, Void, ArrayList<PhotoEntity>> {

        public MyAlxMultiTask() {

        }

        @Override
        protected ArrayList<PhotoEntity> doInBackground(Void... voids) {
            allPhotoArrayList = new ArrayList<>();
            String getImage = " limit ";
            String defaultSize = " limit 10";
            String getSize = defaultSize;

            Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = context.getContentResolver();//得到内容处理者实例
            String sortOrder = MediaStore.Images.Media.DATE_ADDED + " desc";//设置拍摄日期为倒序
//                Log.i("Alex", "准备查找图片");
            // 只查询jpeg和png的图片
            Cursor mCursor = mContentResolver.query(mImageUri, new String[]{MediaStore.Images.Media.DATA},
                    MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?", new String[]{"image/jpeg", "image/png"}, sortOrder + getSize);
            if (mCursor == null) return allPhotoArrayList;
            int size = mCursor.getCount();
//                Log.i("Alex", "查到的size是" + size);
            if (size == 0) {
                return allPhotoArrayList;
            }
            for (int i = 0; i < size; i++) {//遍历全部图片
                mCursor.moveToPosition(i);
                String path = mCursor.getString(0);// 获取图片的路径
                String dataTime = pictureSortTime(path);//将所有图片的时间数据放到整型数组里
                PhotoEntity entity = new PhotoEntity();
                entity.url = path;//将图片的uri放到对象里去
                entity.time = dataTime;
                Date timeDate = TimeUtil.dataOne(dataTime);
                entity.timeDate = timeDate;
                Calendar calendar = Calendar.getInstance();
                if (timeDate != null) {
                    calendar.setTime(timeDate);
                    entity.calendar = calendar;
                }
                allPhotoArrayList.add(entity);
                break;
            }
            mCursor.close();
            return allPhotoArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<PhotoEntity> photoArrayList) {
            super.onPostExecute(photoArrayList);
            if (photoArrayList == null) {
                return;
            }
            if (completeCallback != null) {
                completeCallback.onSuccess(photoArrayList);
            }

        }
    }

    public void setOnLookUpPhotosCallback(LookUpPhotosCallback completeCallback) {
        this.completeCallback = completeCallback;
    }

    /**
     * 查询照片成功的回调函数
     */
    public interface LookUpPhotosCallback {
        void onSuccess(ArrayList<PhotoEntity> photoArrayList);
    }

    /**
     * @param path 照片本地地址
     * @return 照片的时间属性
     */
    private static String pictureSortTime(String path) {
        String dataTime;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            dataTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            if (dataTime != null) {
                return dataTime;
            } else {//发现有获取不到datatime的情况,因为截图到的图片并没有存放日期
                dataTime = path.substring(path.length() - 23, path.length() - 19) + ":"//年
                        + path.substring(path.length() - 18, path.length() - 16) + ":"//月
                        + path.substring(path.length() - 15, path.length() - 13) + " "//日
                        + path.substring(path.length() - 12, path.length() - 10) + ":"//时
                        + path.substring(path.length() - 9, path.length() - 7) + ":"//分
                        + path.substring(path.length() - 6, path.length() - 4);//秒
//                Log.i("Test", "pictureSortTime:强行拼接的时间 " + dataTime);
                return dataTime;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("ScanPhoto", "pictureSortTime: 获取照片时间时发生异常");
        return null;
    }


}
