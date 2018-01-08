package com.cloudring.magic.camera;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.cloudring.magicsound.IVoiceAidlInterface;

/**
 * Created by BB on 2017/12/23.
 */

public class MyServiceConnection {


    private static MyServiceConnection instance;

    public IVoiceAidlInterface remoteService;

    private MyServiceConnection() {

    }

    public static MyServiceConnection getInstance() {
        if (instance == null) {
            synchronized (MyServiceConnection.class) {
                if (instance == null) {
                    instance = new MyServiceConnection();
                }
            }
        }
        return instance;
    }



    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            remoteService = com.cloudring.magicsound.IVoiceAidlInterface.Stub.asInterface(service);
            try {
                if (remoteService != null) {
                    remoteService.stopSpeaking();
                    remoteService.speak("蛋蛋可以帮您拍照,也可以帮您录像");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
