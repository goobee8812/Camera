package com.magic.photo.photoviewlibrary.manager;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.magic.photo.photoviewlibrary.config.UrlConfigure;
import com.magic.photo.photoviewlibrary.entity.CallbackInfo;
import com.magic.photo.photoviewlibrary.entity.FamilyPhotoInfo;
import com.magic.photo.photoviewlibrary.entity.FamilyPhotoRequest;
import com.magic.photo.photoviewlibrary.entity.ImageDeleteParamsEntity;
import com.magic.photo.photoviewlibrary.entity.PhotoUpLoadBean;
import com.magic.photo.photoviewlibrary.entity.QINIUConfigEntity;
import com.magic.photo.photoviewlibrary.entity.QiNiuConfigRequest;
import com.magic.photo.photoviewlibrary.listener.ImageRequestListener;
import com.magic.photo.photoviewlibrary.utils.GsonUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * description:
 * Created by luohaijun on 2016/9/22.
 */
public class ImageRequest implements ImageRequestListener {

    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("application/json; charset=utf-8");

    /**
     * 请求七牛信息
     *
     * @param userId
     * @param funType 业务类型：： 1商品图片，2其他
     */
    @Override
    public void requestQINIUConfig(String userId, String token, int funType) {
        final OkHttpClient httpClient = new OkHttpClient();
        QiNiuConfigRequest configRequest = new QiNiuConfigRequest(funType + "", userId);
        String json = GsonUtils.toJson(configRequest);
        String url = UrlConfigure.QINIU_CONFIG_REQUEST + "?token=" + token;
        final Request request = createRequest(json, url);
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                EventBus.getDefault().post(new CallbackInfo<QINIUConfigEntity>());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    CallbackInfo<QINIUConfigEntity> info = parseQINIUInfo(result);
                    EventBus.getDefault().post(info);
                } else {
                    EventBus.getDefault().post(new CallbackInfo<QINIUConfigEntity>());
                }
            }
        });
    }

    /**
     * 图片上传
     *
     * @param userId
     * @param imgPath
     * @param address
     */
    @Override
    public void imageUpload(String userId, List<String> imgPath, String address, final com.magic.photo.photoviewlibrary.listener.CallbackInfo info) {
        final OkHttpClient httpClient = new OkHttpClient();
        PhotoUpLoadBean upLoadBean = new PhotoUpLoadBean(userId, imgPath, address);
        String json = GsonUtils.toJson(upLoadBean);
        final Request request = createRequest(json, UrlConfigure.IMAGE_UPLOAD);
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                CallbackInfo callbackInfo = new CallbackInfo();
                callbackInfo.setCode("404");
                callbackInfo.setMessage(e.getMessage());
                if (info != null) {
                    info.onCallback(callbackInfo);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                CallbackInfo callbackInfo = null;
                if (response.isSuccessful()) {
                    try {
                        callbackInfo = GsonUtils.getSingleBean(result, CallbackInfo.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (callbackInfo == null) {
                    callbackInfo = new CallbackInfo();
                }
                if (info != null) {
                    info.onCallback(callbackInfo);
                }
            }
        });
    }

    /**
     * 请求图片
     */
    /**
     * 请求图片
     */
    @Override
    public void requestImages(String userId, String pageNumber, String pageSize, String startTime, String endTime, final com.magic.photo.photoviewlibrary.listener.CallbackInfo info) {
        final OkHttpClient httpClient = new OkHttpClient();
        FamilyPhotoRequest params = new FamilyPhotoRequest(userId, pageNumber, pageSize, startTime, endTime);
        String json = GsonUtils.toJson(params);
        final Request request = createRequest(json, UrlConfigure.IMAGE_REQUEST);
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                CallbackInfo<TreeMap<String, List<FamilyPhotoInfo>>> infoList = new CallbackInfo<>();
                infoList.setCode("404");
                infoList.setMessage(e.getMessage());
                if (info != null) {
                    info.onCallback(infoList);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                CallbackInfo<TreeMap<String, List<FamilyPhotoInfo>>> infoList = null;
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    infoList = new Gson().fromJson(result, new TypeToken<CallbackInfo<TreeMap<String, List<FamilyPhotoInfo>>>>() {
                    }.getType());
                } else {
                    Log.e("downLoad_result", response.message());
                }
                if (infoList == null) {
                    infoList = new CallbackInfo<>();
                }
                if (info != null) {
                    info.onCallback(infoList);
                }
            }
        });
    }


    /**
     * 图片删除
     *
     * @param id
     * @param imageUrls 删除的图片地址集
     */
    @Override
    public void deleteIamges(String id, String imageUrls, final com.magic.photo.photoviewlibrary.listener.CallbackInfo info) {
        OkHttpClient okHttpClient = new OkHttpClient();
        ImageDeleteParamsEntity paramsEntity = new ImageDeleteParamsEntity(id, imageUrls);
        String json = GsonUtils.toJson(paramsEntity);
        final Request request = createRequest(json, UrlConfigure.IMAGE_DELETE);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                CallbackInfo callbackInfo = new CallbackInfo();
                callbackInfo.setCode("404");
                callbackInfo.setMessage(e.getMessage());
                if (info != null) {
                    info.onCallback(callbackInfo);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                CallbackInfo callbackInfo = null;
                if (response.isSuccessful()) {
                    try {
                        callbackInfo = GsonUtils.getSingleBean(result, CallbackInfo.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (callbackInfo == null) {
                    callbackInfo = new CallbackInfo();
                }
                if (info != null) {
                    info.onCallback(callbackInfo);
                }
            }
        });
    }

    /**
     * 生成request
     *
     * @param json
     * @param url
     * @return
     */
    private Request createRequest(String json, String url) {
        RequestBody requestBody = FormBody.create(MEDIA_TYPE_MARKDOWN, json);
        Request request = new Request.Builder().post(requestBody).url(url).build();
        return request;
    }

    private CallbackInfo<QINIUConfigEntity> parseQINIUInfo(String json) {
        CallbackInfo<QINIUConfigEntity> info = null;
        try {

            info = new Gson().fromJson(json, new TypeToken<CallbackInfo<QINIUConfigEntity>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;

    }
}
