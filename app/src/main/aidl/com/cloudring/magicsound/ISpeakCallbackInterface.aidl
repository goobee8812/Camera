// ISpeakCallbackInterface.aidl
package com.cloudring.magicsound;
// Declare any non-default types here with import statements

interface ISpeakCallbackInterface {
    void onError(int errorCode, String errorMsg);//错误
    void onSpeakBegin();//开始播报
    void onCompletion();//播报完成
    void onProgress(int percent);//进度
}
