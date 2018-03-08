package com.cloudring.magic.camera.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

/**
 * Created by BB on 2018/3/8.
 */

public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

    public interface ScanListener {
        void onScanFinish();
    }

    private MediaScannerConnection mMs;
    private String mPath;
    private ScanListener listener;

    public SingleMediaScanner(Context context, String p) {
        mPath = p;
        mMs = new MediaScannerConnection(context, this);
        mMs.connect();
    }


    public SingleMediaScanner(Context context, String p, ScanListener l) {
        listener = l;
        mPath = p;
        mMs = new MediaScannerConnection(context, this);
        mMs.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        mMs.scanFile(mPath, null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        mMs.disconnect();
        if (listener != null) {
            listener.onScanFinish();
        }

    }

}
