// IVoiceAidlInterface.aidl
package com.cloudring.magicsound;
import com.cloudring.magicsound.ISpeakCallbackInterface;

// Declare any non-default types here with import statements

interface IVoiceAidlInterface {
    void speak(String text);
    void stopSpeaking();
    void register(ISpeakCallbackInterface callback);
    void unregister(ISpeakCallbackInterface callback);
}
