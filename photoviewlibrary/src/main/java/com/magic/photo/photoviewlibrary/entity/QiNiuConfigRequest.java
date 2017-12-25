package com.magic.photo.photoviewlibrary.entity;

/**
 * description:
 * Created by luohaijun on 2016/10/23.
 */

public class QiNiuConfigRequest {

    public QiNiuConfigBoby data;

    public QiNiuConfigRequest(String funType, String userId) {
        data = new QiNiuConfigBoby();
        data.funType = funType;
        data.userId = userId;
    }

    public class QiNiuConfigBoby {

        public String funType;
        public String userId;
    }
}
