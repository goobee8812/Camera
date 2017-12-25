package com.magic.photo.photoviewlibrary.entity;

/**
 * description:请求云相册参数实体类
 * Created by luohaijun on 2016/10/31.
 */

public class FamilyPhotoRequest {

    String userId;
    RequestParams data;

    /**
     * @param userId
     * @param pageNumber
     * @param pageSize
     * @param startTime  2016-10-01 00:00:00
     * @param endTime    2016-10-31 00:00:00
     */
    public FamilyPhotoRequest(String userId, String pageNumber, String pageSize, String startTime, String endTime) {
        this.userId = userId;
        data = new RequestParams();
        data.pageNumber = pageNumber;
        data.pageSize = pageSize;
        data.startTime = startTime;
        data.endTime = endTime;
    }

    private class RequestParams {

        public String pageNumber;
        public String pageSize;
        public String startTime;
        public String endTime;
    }
}
