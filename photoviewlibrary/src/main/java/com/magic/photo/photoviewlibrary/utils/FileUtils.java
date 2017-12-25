package com.magic.photo.photoviewlibrary.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.entity.DateExpandableEntity;
import com.magic.photo.photoviewlibrary.entity.PhotoInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.magic.photo.photoviewlibrary.utils.DateUtils.getFormaterTime;


/**
 * description:
 * Created by luohaijun on 2016/9/21.
 */
public class FileUtils {

    /**
     * 递归删除文件和文件夹
     *
     * @param file 要删除的根目录
     */
    public static void RecursionDeleteFile(File file, Context context) {
        if (file.isFile()) {
            file.delete();
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);
//            updateMediaScan(context,file.getAbsolutePath());
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                RecursionDeleteFile(f, context);
            }
            file.delete();
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);
        }
    }

    /**
     * 保存图片
     *
     * @param mBitmap
     * @param fileName
     * @return 文件路径
     */
    public static String onSaveBitmap(final Bitmap mBitmap, final String fileName) {
        String photoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/" + fileName;
        //创建文件对象，用来存储新的图像文件
        File file = new File(photoPath);
        //创建文件
        try {
            file.createNewFile();
            //定义文件输出流
            FileOutputStream fout = new FileOutputStream(file);
            //将bitmap存储为jpg格式的图片
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
            fout.flush();//刷新文件流
            fout.close();
            mBitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file == null ? null : file.getAbsolutePath();
    }

    /**
     * 从数据库中获取图片地址
     *
     * @param uri
     * @param mContext
     * @return
     */
    public static String getPathFromCursor(Uri uri, Context mContext) {
        String path;
        String[] filePathColumns = {MediaStore.Images.Media.DATA};
        Cursor c = mContext.getContentResolver().query(uri, filePathColumns, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        int columnIndex = c.getColumnIndex(filePathColumns[0]);
        path = c.getString(columnIndex);
        c.close();
        return path;
    }

    /**
     * 创建目录
     *
     * @param path
     */
    public static void createDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 根据指定路径，创建父目录及文件
     *
     * @param filePath
     * @return File 如果创建失败的话，返回null
     */
    public static File createFile(String filePath) {
        return createFile(filePath, "755");
    }

    /**
     * 创建文件，并修改读写权限
     *
     * @param filePath
     * @param mode
     * @return
     */
    public static File createFile(String filePath, String mode) {
        File desFile = null;
        try {
            String desDir = filePath.substring(0, filePath.lastIndexOf(File.separator));
            File dir = new File(desDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            chmodFile(dir.getAbsolutePath(), mode);
            desFile = new File(filePath);
            if (!desFile.exists()) {
                desFile.createNewFile();
            }
            chmodFile(desFile.getAbsolutePath(), mode);
        } catch (Exception e) {
        }
        return desFile;
    }

    /**
     * 修改文件读写权限
     *
     * @param fileAbsPath
     * @param mode
     */
    public static void chmodFile(String fileAbsPath, String mode) {
        String cmd = "chmod " + mode + " " + fileAbsPath;
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
        }
    }

    /**
     * 剪切文件，将文件拷贝到目标目录，再将源文件删除
     *
     * @param sourcePath
     * @param targetPath
     */
    public static boolean cutFile(Context mContext, String sourcePath, String targetPath) {
        boolean isSuccessful = copyFile(sourcePath, targetPath);
        MediaScannerConnection.scanFile(mContext, new String[]{targetPath}, null, null);
        if (isSuccessful) {
            // 拷贝成功则删除源文件
            return deleteFile(mContext, sourcePath);
        }
        return false;
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param file 要删除的根目录
     */
    public static void recursionDeleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            MediaScannerConnection.scanFile(UIUtils.getContext(), new String[]{file.getAbsolutePath()}, null, null);
//            updateMediaScan(context,file.getAbsolutePath());
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                recursionDeleteFile(f);
            }
            file.delete();
            MediaScannerConnection.scanFile(UIUtils.getContext(), new String[]{file.getAbsolutePath()}, null, null);
        }
    }

    /**
     * 删除文件
     *
     * @param path
     * @return
     */
    public static boolean deleteFile(Context mContext, String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (!file.exists()) {
                return false;
            }
            try {
                file.delete();
                MediaScannerConnection.scanFile(mContext, new String[]{file.getAbsolutePath()}, null, null);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 拷贝文件，通过返回值判断是否拷贝成功
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @return
     */
    public static boolean copyFile(String sourcePath, String targetPath) {
        boolean isOK = false;
        if (!TextUtils.isEmpty(sourcePath) && !TextUtils.isEmpty(targetPath)) {
            File sourcefile = new File(sourcePath);
            File targetFile = new File(targetPath);
            if (!sourcefile.exists()) {
                return false;
            }
            if (sourcefile.isDirectory()) {
                isOK = copyDir(sourcefile, targetFile);
            } else if (sourcefile.isFile()) {
                if (!targetFile.exists()) {
                    createFile(targetPath);
                }
                FileOutputStream outputStream = null;
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(sourcefile);
                    outputStream = new FileOutputStream(targetFile);
                    byte[] bs = new byte[1024];
                    int len;
                    while ((len = inputStream.read(bs)) != -1) {
                        outputStream.write(bs, 0, len);
                    }
                    isOK = true;
                } catch (Exception e) {
                    isOK = false;
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }

            return isOK;
        }
        return false;
    }

    /**
     * 拷贝目录
     *
     * @param sourceFile
     * @param targetFile
     * @return
     */
    public static boolean copyDir(File sourceFile, File targetFile) {
        if (sourceFile == null || targetFile == null) {
            return false;
        }
        if (!sourceFile.exists()) {
            return false;
        }
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        // 获取目录下所有文件和文件夹的列表
        File[] files = sourceFile.listFiles();
        if (files == null || files.length < 1) {
            return false;
        }
        File file;
        StringBuffer buffer = new StringBuffer();
        boolean isSuccessful;
        // 遍历目录下的所有文件文件夹，分别处理
        for (int i = 0; i < files.length; i++) {
            file = files[i];
            buffer.setLength(0);
            buffer.append(targetFile.getAbsolutePath()).append(File.separator).append(file.getName());
            if (file.isFile()) {
                // 文件直接调用拷贝文件方法
                isSuccessful = copyFile(file.getAbsolutePath(), buffer.toString());
                MediaScannerConnection.scanFile(UIUtils.getContext(), new String[]{buffer.toString()}, null, null);
                if (!isSuccessful) {
                    return false;
                }
            } else if (file.isDirectory()) {
                // 目录再次调用拷贝目录方法
                copyDir(file, new File(buffer.toString()));
            }

        }
        return true;
    }

    /**
     * 重命名
     */
    public static boolean renameTo(String oldPath, String newPath) {
        File file = new File(oldPath);
        boolean bool;
        if (!file.exists()) {
            return false;
        }
        File newFile = new File(newPath);
        if (newFile.exists()) {
            return false;
        }
        bool = file.renameTo(new File(newPath));
        return bool;
    }

    /**
     * 重命名(新建文件夹，将文件复制到新文件夹，然后删除旧文件夹)
     *
     * @param oldPath
     * @param newName
     * @return
     */
    public static boolean renameFile(String oldPath, String newName) {
        boolean bool = false;
        final File file = new File(oldPath);
        final String modifyFilePath = file.getParent() + java.io.File.separator;
        final String newFilePath = modifyFilePath + newName;
        final File newFile = new File(newFilePath);
        if (newFile.exists()) {
            UIUtils.postTaskSafely(new Runnable() {
                @Override
                public void run() {
                    UIUtils.showToast(UIUtils.getContext().getString(R.string.name_exist));
                }
            });
            return bool;
        } else {
            bool = copyDir(file, newFile);
            if (bool) {
                recursionDeleteFile(file);
            }
        }
        return bool;
    }

    /**
     * 获取转换后list
     *
     * @param pathList
     * @return
     */
    public static List<PhotoInfo> getCurrentList(List<String> pathList) {
        if (pathList == null) {
            return null;
        }
        List<PhotoInfo> imgList = new ArrayList<>();

        PhotoInfo imageBean;
        for (String path : pathList) {
            //获取该图片的父路径名
            long time = getFileLastmodified(path);
            String timeStr = getFormaterTime(time, "yyyyMM");

            imageBean = containsDate(imgList, timeStr);
            if (imageBean == null) {
                imageBean = new PhotoInfo();
                List<String> chileList = new ArrayList<>();
                chileList.add(path);
                imageBean.setImgDate(time);
                imageBean.setUrlList(chileList);
                imgList.add(imageBean);
            } else {
                imageBean.setImgDate(time);
                imageBean.getUrlList().add(path);
            }
        }
        Collections.sort(imgList, new FileTimeComparator());//按时间排序
        return imgList;
    }

    public static DateExpandableEntity getDateList(List<String> imgList) {
        if (imgList == null) {
            return null;
        }
        DateExpandableEntity expandableEntity = new DateExpandableEntity();
        List<String> groupList = new ArrayList<>();
        Map<String, List<String>> childMap = new LinkedHashMap<>();
        String timeStr = "";
        for (String path : imgList) {
            long time = getFileLastmodified(path);
            String tempTime = DateUtils.getFormaterTime(time, "yyyy");
            if (!tempTime.equals(timeStr)) {
                groupList.add(DateUtils.getFormaterTime(time, "yyyyMM"));
                childMap.put(DateUtils.getFormaterTime(time, "yyyyMM"), getChildList(time, imgList));
                timeStr = tempTime;
            }
        }
        groupList = removeRepeatList(groupList);
        Collections.sort(groupList, new DateComparator());
        expandableEntity.setGroupList(groupList);
        expandableEntity.setChildList(childMap);
        return expandableEntity;
    }

    private static List<String> getChildList(long date, List<String> mList) {
        List<String> tempList = new ArrayList<>();
        String tempDate = DateUtils.getFormaterTime(date, "yyyy");
        for (String path : mList) {
            long d = getFileLastmodified(path);
            String temp = DateUtils.getFormaterTime(d, "yyyy");
            if (temp.equals(tempDate)) {
                tempList.add(DateUtils.getFormaterTime(d, "yyyyMM"));
            }
        }
        tempList = removeRepeatList(tempList);
        Collections.sort(tempList, new DateComparator());
        return tempList;
    }

    public static long getFileLastmodified(String path) {
        long temp = 0;
        File file = new File(path);
        if (file.exists()) {
            return file.lastModified();
        }
        return temp;
    }

    private static PhotoInfo containsDate(List<PhotoInfo> photoImageBeanList, String time) {
        if (photoImageBeanList == null || photoImageBeanList.size() == 0) {
            return null;
        }
        for (int i = 0; i < photoImageBeanList.size(); i++) {
            String timeStr = getFormaterTime(photoImageBeanList.get(i).getImgDate(), "yyyyMM");
            if (timeStr.equals(time)) {
                return photoImageBeanList.get(i);
            }
        }
        return null;
    }

    /**
     * 按时间排序list
     */
    private static class FileTimeComparator implements Comparator<PhotoInfo> {

        @Override
        public int compare(PhotoInfo lhs, PhotoInfo rhs) {
            if (lhs.getImgDate() < rhs.getImgDate()) {
                return 1;
            } else if (lhs.getImgDate() > rhs.getImgDate()) {
                return -1;
            }
            return 0;
        }
    }

    public static List<String> removeRepeatList(List<String> mList) {
        if (mList == null) {
            return null;
        }
        Set<String> set = new LinkedHashSet();
        set.addAll(mList);
        mList.clear();
        mList.addAll(set);
        return mList;
    }

    /**
     * 判断图片list是否有空,为空则移除这条记录
     *
     * @param infoList
     * @return
     */
    public static List<PhotoInfo> judgeNullList(List<PhotoInfo> infoList) {
        for (PhotoInfo photoInfo : infoList) {
            List<String> urlList = photoInfo == null ? null : photoInfo.getUrlList();
            if (urlList == null || urlList.size() == 0) {
                infoList.remove(photoInfo);
            }
        }
        return infoList;
    }
}
