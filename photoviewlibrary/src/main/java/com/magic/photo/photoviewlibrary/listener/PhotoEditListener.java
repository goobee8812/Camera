package com.magic.photo.photoviewlibrary.listener;

/**
 * description:
 * Created by luohaijun on 2016/11/29.
 */

public interface PhotoEditListener {

    /**
     * 全选
     */
    void onSelectAll(boolean isAll);

    /**
     * 删除
     */
    void onFileDelete();

    /**
     * 删除
     */
    void onChildFileDelete();

    /**
     * 重命名
     */
    void onFileRename();

    /**
     * 移动
     */
    void onFileRemove();

    /**
     * 分享
     */
    void onFileShared();


}
