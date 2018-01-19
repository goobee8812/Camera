package com.cloudring.magic.camera.present;

/**
 * Created by Administrator on 2018/1/18.
 */

public interface SaveCallback {

    void success(String photoPath);

    void onError(Exception e);
}
