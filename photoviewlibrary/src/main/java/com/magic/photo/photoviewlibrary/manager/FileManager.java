package com.magic.photo.photoviewlibrary.manager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.magic.photo.photoviewlibrary.entity.Image;
import com.magic.photo.photoviewlibrary.entity.PhotoImageBean;
import com.magic.photo.photoviewlibrary.entity.Video;
import com.magic.photo.photoviewlibrary.entity.event.ImageListEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * description:
 * Created by luohaijun on 2016/9/21.
 */
public class FileManager {

    private static FileManager mFileManager;

    public static final String[] VIDEO_COLUMN = {
            MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.TITLE
    };
    private static final String SELECTION = MediaStore.Video.Media.MIME_TYPE + "= ? "
            + " or " + MediaStore.Video.Media.MIME_TYPE + " = ? ";

    private static final String SELECTOR_IMAGE = MediaStore.Images.Media.MIME_TYPE + "=? or "
            + MediaStore.Images.Media.MIME_TYPE + "=? or "
            + MediaStore.Images.Media.MIME_TYPE + "=?";

    public static final String[] IMAGE_COLUMN = {
            MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.TITLE
    };

    private FileManager() {

    }

    public static FileManager getInstance() {
        if (mFileManager == null) {
            mFileManager = new FileManager();
        }
        return mFileManager;
    }


    /**
     * 获取本地图片
     */
    public void getLocalImages(final Context mContext) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                getImagesFromMedia(mContext);
            }
        }.start();
    }

    /**
     * 利用ContentProvider扫描手机中的图片
     */
    private void getImagesFromMedia(Context context) {
        HashMap<String, ArrayList<String>> mGruopMap = new HashMap<>();
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = context.getContentResolver();
        //只查询jpeg和png的图片
        Cursor mCursor = mContentResolver.query(mImageUri, null,
                MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);
        while (mCursor != null ? mCursor.moveToNext() : false) {
            //获取图片的路径
            String path = mCursor.getString(mCursor
                    .getColumnIndex(MediaStore.Images.Media.DATA));

            //获取该图片的父路径名
            String parentName = new File(path).getParentFile().getName();

            //根据父路径名将图片放入到mGruopMap中
            if (!mGruopMap.containsKey(parentName)) {
                ArrayList<String> chileList = new ArrayList<>();
                chileList.add(path);
                mGruopMap.put(parentName, chileList);
            } else {
                mGruopMap.get(parentName).add(path);
            }
        }
        mCursor.close();
        EventBus.getDefault().post(new ImageListEvent(mGruopMap));
    }

    /**
     * 组装分组界面GridView的数据源，因为我们扫描手机的时候将图片信息放在HashMap中
     * 所以需要遍历HashMap将数据组装成List
     *
     * @param mGruopMap
     * @return
     */
    public List<PhotoImageBean> subGroupOfImage(HashMap<String, ArrayList<String>> mGruopMap) {
        if (mGruopMap == null || mGruopMap.size() == 0) {
            return null;
        }
        List<PhotoImageBean> list = new ArrayList<>();

        Iterator<Map.Entry<String, ArrayList<String>>> it = mGruopMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ArrayList<String>> entry = it.next();
            PhotoImageBean mImageBean = new PhotoImageBean();
            String key = entry.getKey();
            List<String> value = entry.getValue();

            mImageBean.setFolderName(key);
            mImageBean.setImageCounts(value.size());
            mImageBean.setTopImagePath(value.get(0));//获取该组的第一张图片
            list.add(mImageBean);
        }
        return list;
    }

    public List<PhotoImageBean> getFolderList(HashMap<String, ArrayList<String>> mGruopMap, String path) {
        String folderName = "";
        List<PhotoImageBean> list = subGroupOfImage(mGruopMap);
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                folderName = file.getParentFile().getName();
            }
        }
        PhotoImageBean bean;
        for (int i = 0; i < list.size(); i++) {
            bean = list.get(i);
            if (bean.getFolderName().equals(folderName)) {
                list.remove(i);
                break;
            }
        }
        return list;
    }


    public List<Video> getVideosFromMedia(Context context) {
        List<Video> list = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_COLUMN, SELECTION, new String[]{"video/mp4", "video/3gp"}, "date_added desc");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(VIDEO_COLUMN[1]));
                if (!new File(path).exists()) {
                    continue;
                }
                if (path.contains("/storage/emulated/0/videofiles/")) {
                    continue;
                }
                long id = cursor.getLong(cursor.getColumnIndex(VIDEO_COLUMN[0]));
                String size = cursor.getString(cursor.getColumnIndex(VIDEO_COLUMN[2]));
                if (TextUtils.isEmpty(size) || Integer.parseInt(size) < 1024) {
                    continue;
                }
                long duration = cursor.getLong(cursor.getColumnIndex(VIDEO_COLUMN[3]));
                if (duration==0){
                    continue;
                }
                long createTime = cursor.getLong(cursor.getColumnIndex(VIDEO_COLUMN[4])) * 1000;
                String mimeType = cursor.getString(cursor.getColumnIndex(VIDEO_COLUMN[5]));
                String title = cursor.getString(cursor.getColumnIndex(VIDEO_COLUMN[6]));
                Video video = new Video(id, path, size, duration, createTime, mimeType, title);
                list.add(video);
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        return list;
    }


    public List<Image> getImageFormMedia(Context context) {
        List<Image> list = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_COLUMN, SELECTOR_IMAGE, new String[]{"image/jpeg", "image/png", "image/gif"}, "date_added desc");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(IMAGE_COLUMN[1]));
                if (!new File(path).exists()) {
                    continue;
                }
                long id = cursor.getLong(cursor.getColumnIndex(IMAGE_COLUMN[0]));
                String size = cursor.getString(cursor.getColumnIndex(IMAGE_COLUMN[2]));
                if (TextUtils.isEmpty(size) || Integer.parseInt(size) < 10240) {
                    System.out.println(size);
                    continue;
                }
                long createTime = cursor.getLong(cursor.getColumnIndex(IMAGE_COLUMN[3])) * 1000;
                String mimeType = cursor.getString(cursor.getColumnIndex(IMAGE_COLUMN[4]));
                String title = cursor.getString(cursor.getColumnIndex(IMAGE_COLUMN[5]));
                Image image = new Image(id, path, size, createTime, mimeType, title);
                list.add(image);
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

}
