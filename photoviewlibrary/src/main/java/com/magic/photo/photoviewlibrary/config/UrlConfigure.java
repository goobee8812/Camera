package com.magic.photo.photoviewlibrary.config;

/**
 * description:
 * Created by luohaijun on 2016/10/20.
 */

public class UrlConfigure {

    /**
     * http://192.168.1.189:8080/cloudring-shop-mobile-web/qiniu/1.0/getConfig
     * <p>
     * http://fge.cloudring.net:8888/cloudring-shop-mobile-web/qiniu/1.0/getConfig
     */
    private static final String BASE_URL  = "http://www.cloudring.net/";//正式服务器
//    private static final String BASE_URL = "http://fge.cloudring.net:8888/";//测试服务器
//    private static final String BASE_URL = "http://192.168.1.211/";//测试服务器

    /**
     * 请求七牛配置信息
     */
    public static final String QINIU_CONFIG_REQUEST = BASE_URL + "cloudring-shop-mobile-web/qiniu/1.0/getConfig";

    /**
     * 图片名称上传地址
     */
    public static final String IMAGE_UPLOAD = BASE_URL + "cloudring-property-mobile-web/cloudAlbum/uploadPicture";

    /**
     * 请求图片 http://192.168.1.137:8080/cloudring-property-mobile-web/cloudAlbum/getPicture
     */
    public static final String IMAGE_REQUEST = BASE_URL + "cloudring-property-mobile-web/cloudAlbum/getPicture";

    /**
     * 图片删除
     */
    public static final String IMAGE_DELETE = BASE_URL + "cloudring-property-mobile-web/cloudAlbum/deletePicture";
}
