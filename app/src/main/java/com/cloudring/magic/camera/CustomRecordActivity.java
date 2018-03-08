package com.cloudring.magic.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudring.magic.camera.utils.PowerWakeLock;
import com.cloudring.magic.camera.utils.SingleMediaScanner;
import com.cloudring.magic.camera.utils.SpUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class CustomRecordActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int CONTROL_CODE = 1;
    private static final String TAG = "CustomRecordActivity";
    private Toast toast;
    //UI
    private ImageView mRecordControl;
    private ImageView ivBack;
    private ImageView ivCamera;
    private SurfaceView surfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Chronometer mRecordTime;
    //DATA
    private boolean isRecording;// 标记，判断当前是否正在录制
    private long mPauseTime = 0;           //录制暂停时间间隔
    // 存储文件
    private File mVecordFile;
    private Camera mCamera;
    private MediaRecorder mediaRecorder;
    private String currentVideoFilePath;
    private PhotographBroadCast photographBroadCast;
    private int width = 1280;
    private int height = 720;
    private long mCurrentTime;
    private Handler handler = new Handler();
    private int releaseLockTime = 2 * 60 * 1000;
    private boolean isReleaseLock;
    private SurfaceHolder.Callback mCallBack = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            initCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            if (mSurfaceHolder.getSurface() == null) {
                return;
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            stopCamera();
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        initView();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
        sendBroadcast(new Intent("com.android.Camera.stopvideo"));
        registerReceiver();
        initCamera();

        isReleaseLock = true;
        resetLock();
    }

    private Runnable releaseLock = new Runnable() {
        @Override
        public void run() {
            PowerWakeLock.getInstance(CustomRecordActivity.this).releaseLock();
            isReleaseLock = true;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(releaseLock);
        unregisterReceiver(photographBroadCast);
        if (isRecording) {
            mRecordControl.setImageResource(R.mipmap.recordvideo_start);
            stopRecord();
            mCamera.lock();
            delVideo();
        }
        stopCamera();

        PowerWakeLock.getInstance(this).releaseLock();
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
        mRecordControl.setOnClickListener(this);

        //配置SurfaceHodler
        mSurfaceHolder = surfaceView.getHolder();
        // 设置Surface不需要维护自己的缓冲区
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 设置分辨率
        mSurfaceHolder.setFixedSize(320, 280);
        // 设置该组件不会让屏幕自动关闭
        mSurfaceHolder.setKeepScreenOn(true);
        mSurfaceHolder.addCallback(mCallBack);//回调接口
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

    private void resetLock() {
        if (isReleaseLock) {
            PowerWakeLock.getInstance(this).acquire();
            isReleaseLock = false;
        }
        handler.removeCallbacks(releaseLock);
        handler.postDelayed(releaseLock, releaseLockTime);
    }


    private void initCamera() {
        if (mCamera != null) {
            stopCamera();
        }
        //默认启动后置摄像头
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        if (mCamera == null) {
            showToast("未能获取到相机！");
            return;
        }
        try {
            //配置CameraParams
            setCameraParams();
            mCamera.setPreviewDisplay(mSurfaceHolder);
            //启动相机预览
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }


    private void setCameraParams() {
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            for (Camera.Size size : params.getSupportedPreviewSizes()) {
                if (size.width == width && size.height == height) {
                    params.setPreviewSize(width, height);
                    break;
                }
            }
            List<int[]> fpsRanges = params.getSupportedPreviewFpsRange();
            if (fpsRanges.size() > 0) {
                int[] range = fpsRanges.get(0);
                if (range != null) {
                    params.setPreviewFpsRange(range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX], range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
                }
            }

            mCamera.setParameters(params);
        }
    }


    private void stopCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


    public void startRecord() {
        handler.removeCallbacks(releaseLock);
        if (isReleaseLock) {
            PowerWakeLock.getInstance(CustomRecordActivity.this).acquire();
            isReleaseLock = false;
        }


        sendBroadcast(new Intent("com.android.Camera.startvideo"));
        initCamera();
        mCamera.unlock();
        setConfigRecord();
        try {
            //开始录制
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isRecording = true;
        mRecordTime.setVisibility(View.VISIBLE);
        ivCamera.setVisibility(View.GONE);
        if (mPauseTime != 0) {
            mRecordTime.setBase(SystemClock.elapsedRealtime() - (mPauseTime - mRecordTime.getBase()));
        } else {
            mRecordTime.setBase(SystemClock.elapsedRealtime());
        }
        mRecordTime.start();

    }

    /**
     * 停止录制视频
     */
    public void stopRecord() {
        handler.postDelayed(releaseLock, releaseLockTime);
        sendBroadcast(new Intent("com.android.Camera.stopvideo"));
        if (isRecording && mediaRecorder != null) {
            // 设置后不会崩
            mediaRecorder.setOnErrorListener(null);
            mediaRecorder.setOnInfoListener(null);
            mediaRecorder.setPreviewDisplay(null);
            try {
                //停止录制
                mediaRecorder.stop();
                mediaRecorder.reset();
                //释放资源
                mediaRecorder.release();
                mediaRecorder = null;
            } catch (Exception e) {
                e.printStackTrace();
                delVideo();
            }
            mRecordTime.stop();
            mRecordTime.setVisibility(View.GONE);
            ivCamera.setVisibility(View.VISIBLE);
            isRecording = false;
            mPauseTime = 0;
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.record_control:
                if (System.currentTimeMillis() - mCurrentTime > 1200) {
                    if (!isRecording) {
                        //开始录制视频
                        startRecord();
                        mRecordControl.setImageResource(R.mipmap.recordvideo_stop);
                        //1s后才能停止
                    } else {
                        //停止视频录制
                        mRecordControl.setImageResource(R.mipmap.recordvideo_start);
                        stopRecord();
                        mCamera.lock();
                        new SingleMediaScanner(this, currentVideoFilePath);
//                        MediaScannerConnection.scanFile(this, new String[]{currentVideoFilePath}, null, null);
                    }
                    mCurrentTime = System.currentTimeMillis();
                } else {
                    back();
                }
                break;
            case R.id.ivBack:
                if (back()) {
                    setToResult();
                }
                break;
            case R.id.ivCamera:
                finish();
                break;
            default:
                break;
        }
    }

    private boolean back() {
        if (!isRecording) {
            return true;
        }
        if (System.currentTimeMillis() - mCurrentTime > 1200) {
            return true;
        } else {
            showToast("录制时间过短！");
            return false;
        }
    }


    public void showToast(String text) {

        if (!TextUtils.isEmpty(text)) {
            if (toast == null) {
                toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            } else {
                toast.setText(text);
            }
            toast.show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

    // 释放相机
    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


    /**
     * 创建视频文件保存路径
     */
    private boolean createRecordDir() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, "请查看您的SD卡是否存在！", Toast.LENGTH_SHORT).show();
            return false;
        }

        File sampleDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Record");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        String recordName = "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mp4";
        mVecordFile = new File(sampleDir, recordName);
        currentVideoFilePath = mVecordFile.getAbsolutePath();
        return true;
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


    /**
     * 配置MediaRecorder()
     */
    private void setConfigRecord() {
        mediaRecorder = new MediaRecorder();// 创建mediarecorder对象
        // 设置录制视频源为Camera(相机)
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

        //mediarecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
        // 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        // 设置录制的视频编码h263 h264
        //mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        // 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
        mediaRecorder.setVideoSize(width, height);
        mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
        //        //设置录像视频保存地址
        currentVideoFilePath = getSDPath(getApplicationContext()) + getVideoName();
        mediaRecorder.setOutputFile(currentVideoFilePath);
        SpUtil.writeString("videoPath", currentVideoFilePath);

    }

    private String getVideoName() {
        return "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mp4";
    }


    private void setToResult() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }


    public class PhotographBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "com.android.OpenCamera2"://相机语音识别
                case "com.android.Camera.takePhotoFast"://直接拍照
                case "com.android.Camera.takePhoto":
                    if (isRecording) {

                    } else {
                        ivCamera.performClick();
                    }
                    break;

                case "com.android.Camera.startVideo"://启动录像广播
                    mRecordControl.performClick();
                    break;

                case "com.android.Camera.stopVideo":
                    mRecordControl.performClick();
                    break;
                case "com.android.Camera.closeCamera"://按返回键
                    setToResult();
                    break;
                default:
                    break;
            }
        }
    }


}
