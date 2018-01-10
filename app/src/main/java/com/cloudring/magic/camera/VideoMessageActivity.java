package com.cloudring.magic.camera;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudring.magic.camera.utils.ClickProxy;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class VideoMessageActivity extends Activity implements OnClickListener, SurfaceHolder.Callback {

    private int width = 1280;
    private int height = 720;

    private Camera camera = null;
    private MediaRecorder mediarecorder;
    private SurfaceView surfaceview;

    private ImageView mRecordControl;
    private ImageView ivBack;
    private ImageView ivCamera;
    private SurfaceView surfaceView;
    private Chronometer mRecordTime;
    private long systemTime = 0;
    private PhotographBroadCast photographBroadCast;
    private String currentVideoFilePath;
    private boolean isRecording;
    private long mCurrentTime;
    private SurfaceHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);

        ActivityContainer.getInstance().addActivity(this);
        initView();


    }

    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.record_surfaceView);
        mRecordControl = (ImageView) findViewById(R.id.record_control);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        ivCamera = (ImageView) findViewById(R.id.ivCamera);
        mRecordTime = (Chronometer) findViewById(R.id.record_time);
        mRecordTime.setVisibility(View.GONE);
        ivBack.setOnClickListener(this);
        ivCamera.setOnClickListener(this);
        mRecordControl.setOnClickListener(new ClickProxy(this));

        //配置SurfaceHodler
        surfaceview = (SurfaceView) findViewById(R.id.record_surfaceView);
        // 取得holder
        holder = surfaceview.getHolder();
        holder.addCallback(this); // holder加入回调接口
        // setType必须设置，要不出错.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 设置分辨率
        holder.setFixedSize(320, 280);
        surfaceview.setZOrderOnTop(true);
        surfaceview.setZOrderMediaOverlay(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sendBroadcast(new Intent("com.android.Camera.stopvideo"));

        registerReceiver();

        openCamera(holder);

    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.Camera.takePhoto");
        filter.addAction("com.android.Camera.takePhotoFast");
        filter.addAction("com.android.Camera.closeCamera");
        filter.addAction("com.android.Camera.startVideo");
        filter.addAction("com.android.Camera.stopVideo");
        photographBroadCast = new PhotographBroadCast();
        registerReceiver(photographBroadCast, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCamera();
        if (mediarecorder != null) {
            //设置后不会崩
            mediarecorder.setOnErrorListener(null);
            mediarecorder.setPreviewDisplay(null);
            try {
                mediarecorder.stop();
            } catch (IllegalStateException e) {
                //	Log.d("stopRecord", e.getMessage());
            } catch (RuntimeException e) {
                //Log.d("stopRecord", e.getMessage());
            } catch (Exception e) {
                //Log.d("stopRecord", e.getMessage());
            }
            mediarecorder.release();
            mediarecorder = null;
        }
    }


    private void refreshPath() {
        currentVideoFilePath = getSDPath(this) + getVideoName();
    }

    private String getVideoName() {
        return "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mp4";
    }


    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.record_control) {//btn_videomsg_record
            if (isRecording == false) {
                systemTime = System.currentTimeMillis();
                refreshPath();

                //	Button btnRecord = (Button) v;
                //btnRecord.setText("结束");
                mRecordControl.setImageResource(R.mipmap.recordvideo_stop);

                if (camera == null) {
                    openCamera(surfaceview.getHolder());
                }

                try {
                    if (camera == null) {
                        Toast.makeText(getApplicationContext(), "摄像头打开失败", Toast.LENGTH_LONG).show();
                        //btnRecord.setText("录制");
                        mRecordControl.setImageResource(R.mipmap.recordvideo_start);
                        return;
                    }
                    camera.unlock();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                mediarecorder = new MediaRecorder();// 创建mediarecorder对象
                mediarecorder.setCamera(camera);
                mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                mediarecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

                mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                mediarecorder.setVideoSize(width, height);
                mediarecorder.setPreviewDisplay(surfaceview.getHolder().getSurface());
                mediarecorder.setOutputFile(currentVideoFilePath);

                try {
                    mediarecorder.prepare();
                    mediarecorder.start();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isRecording = true;
                mRecordTime.setVisibility(View.VISIBLE);
                ivCamera.setVisibility(View.GONE);

                mRecordTime.setBase(SystemClock.elapsedRealtime());
                mRecordTime.start();
            } else {
                long currentTime = System.currentTimeMillis();


                mRecordControl.setImageResource(R.mipmap.recordvideo_start);

                try {
                    if (mediarecorder != null) {
                        mediarecorder.stop();
                        mediarecorder.release();
                        mediarecorder = null;
                    }

                    camera.lock();
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (currentTime - systemTime > 1000) {
                    MediaScannerConnection.scanFile(this, new String[]{currentVideoFilePath}, null, null);
                    openCamera(holder);
                } else {
                    Toast.makeText(this, "视频录制的时间太短！", Toast.LENGTH_LONG).show();
                    isRecording = false;

                }
                isRecording = false;
                mRecordTime.stop();
                mRecordTime.setVisibility(View.GONE);
                ivCamera.setVisibility(View.VISIBLE);
            }
            mCurrentTime = System.currentTimeMillis();
        } else if (id == R.id.ivCamera) {
            finish();
        } else if (id == R.id.ivBack) {
            if (back()) {
                ActivityContainer.getInstance().finishAllActivity();
            }
        }
    }

    private boolean back() {
        if (!isRecording) {
            return true;
        } else {
            if (System.currentTimeMillis() - mCurrentTime > 1200) {
                return true;
            } else {
                Toast.makeText(this, "录制时间过短！", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(photographBroadCast);
        if (isRecording) {
            mRecordControl.setImageResource(R.mipmap.recordvideo_start);

            try {
                if (mediarecorder != null) {
                    mediarecorder.stop();
                    mediarecorder.release();
                    mediarecorder = null;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            camera.lock();
            camera.stopPreview();
            camera.release();
            camera = null;

            mRecordTime.stop();
            mRecordTime.setVisibility(View.GONE);
            ivCamera.setVisibility(View.VISIBLE);
            isRecording = false;
            delVideo();
        }
    }

    private void delVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(currentVideoFilePath);
                    //将合成的视频复制过来
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String getRes() {
        String[] res = {"蛋蛋还是先帮您打开相机吧!", "蛋蛋正在为您打开相机!"};
        Random random = new Random();// 定义随机类
        int ran = random.nextInt(2);
        if (ran == 0) {
            return res[0];
        } else if (ran == 1) {
            return res[1];
        }
        return res[1];
    }

    public class PhotographBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "com.android.OpenCamera2"://相机语音识别
                case "com.android.Camera.takePhotoFast"://直接拍照
                case "com.android.Camera.takePhoto":
                    if (isRecording) {
                        try {
                            MyServiceConnection.getInstance().remoteService.speak("正在为您录像呢！");
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            MyServiceConnection.getInstance().remoteService.speak(getRes());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        ivCamera.performClick();
                    }
                    break;

                case "com.android.Camera.startVideo"://启动录像广播
//                    try {
//                        MyServiceConnection.getInstance().remoteService.speak("好的,蛋蛋正在为您开启录像！");
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
                    mRecordControl.performClick();
                    break;

                case "com.android.Camera.stopVideo":
                    //VoiceTTSManager.getInstance(CommonLib.getContext()).speak("正在为您打开录像呢！");
                    mRecordControl.performClick();
                    break;

                case "com.android.Camera.closeCamera"://按返回键
                    ActivityContainer.getInstance().finishAllActivity();
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera(holder);
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void openCamera(SurfaceHolder holder) {
        try {
            Camera.CameraInfo info = new Camera.CameraInfo();
            int numCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numCameras; i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    camera = Camera.open(i);
                    break;
                }
            }

            if (camera == null) {
                camera = Camera.open();
            }

            if (camera == null) {
                Toast.makeText(getApplicationContext(), "摄像头打开失败", Toast.LENGTH_LONG).show();
                return;
            }

            Camera.Parameters parms = camera.getParameters();
            for (Camera.Size size : parms.getSupportedPreviewSizes()) {
                if (size.width == width && size.height == height) {
                    parms.setPreviewSize(width, height);
                    break;
                }
            }

            List<int[]> fpsRanges = parms.getSupportedPreviewFpsRange();
            if (fpsRanges.size() > 0) {
                int[] range = fpsRanges.get(0);
                if (range != null) {
                    parms.setPreviewFpsRange(range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX], range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
                    int fps = range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX] / 1000;
                }
            }

            camera.setParameters(parms);
            camera.setDisplayOrientation(0);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
            isRecording = false;
        }
    }

    public static String getSDPath(Context context) {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        } else if (!sdCardExist) {

            Toast.makeText(context, "SD卡不存在", Toast.LENGTH_SHORT).show();

        }
        File eis = new File(sdDir.toString() + "/Camera/");
        try {
            if (!eis.exists()) {
                eis.mkdir();
            }
        } catch (Exception e) {

        }
        return sdDir.toString() + "/Camera/";
    }
}
