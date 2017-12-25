package com.magic.photo.photoviewlibrary.listener;

import java.util.List;

/**
 * description:
 * Created by luohaijun on 2016/10/20.
 */

public interface ImageRequestListener {

    /**
     * 请求七牛信息
     *
     * @param userId
     * @param token
     * @param funType 业务类型： 1商品图片，2其他
     */
    public void requestQINIUConfig(String userId, String token, int funType);

    /**
     * 图片上传
     */
    public void imageUpload(String userId, List<String> imgPath, String address, CallbackInfo info);

    /**
     * 图片获取
     */
    public void requestImages(String userId, String pageNumber, String pageSize, String startTime, String endTime, CallbackInfo info);

    /**
     * 图片删除
     *
     * @param id
     * @param imageUrls 删除的图片地址集
     */
    public void deleteIamges(String id, String imageUrls, CallbackInfo info);
}
