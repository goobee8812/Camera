package com.cloudring.magic.camera.photograph.present;

import android.app.Activity;
import android.hardware.Camera;

import com.cloudring.magic.camera.photograph.PhotographActivity;
import com.cloudring.magic.camera.photograph.ZXPhotographActivity;


public interface PhotographPresent {
    void takePhoto(Camera camera, PhotographActivity activity);
    void takePhoto(Camera camera, ZXPhotographActivity activity);
    void getSystemPhoto(Activity activity);
    void reCording(Activity activity);//目前暂时是调用系统录像功能
    void takePhotoDelay(int delayMillis, Camera camera, PhotographActivity activity);
    void takePhotoDelay(int delayMillis, Camera camera, ZXPhotographActivity activity);
}
