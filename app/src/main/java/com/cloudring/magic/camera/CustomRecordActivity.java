package com.cloudring.magic.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudring.magic.camera.utils.ClickProxy;
import com.cloudring.magic.camera.utils.SpUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class CustomRecordActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CustomRecordActivity";
    public static final int CONTROL_CODE = 1;
    //UI
    private ImageView mRecordControl;
    private ImageView ivBack;
    private ImageView ivCamera;
    private SurfaceView surfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Chronometer mRecordTime;

    //DATA
    private boolean isRecording;// 标记，判断当前是否正在录制
    private boolean isPause; //暂停标识
    private long mPauseTime = 0;           //录制暂停时间间隔

    // 存储文件
    private File mVecordFile;
    private Camera mCamera;
    private MediaRecorder mediaRecorder;
    private String currentVideoFilePath;
    private String saveVideoPath = "";


    private PhotographBroadCast photographBroadCast;
    private IntentFilter filter;
    private int width = 1280;
    private int height = 720;

    private long mCurrentTime;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        ActivityContainer.getInstance().addActivity(this);
        initView();

    }




    @Override
    protected void onResume() {
        super.onResume();

//        iaudioState.mode(0);
        sendBroadcast(new Intent("com.android.Camera.stopvideo"));

        registerReceiver();

        initCamera();

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
        mSurfaceHolder = surfaceView.getHolder();
        // 设置Surface不需要维护自己的缓冲区
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 设置分辨率
        mSurfaceHolder.setFixedSize(320, 280);
        // 设置该组件不会让屏幕自动关闭
        mSurfaceHolder.setKeepScreenOn(true);
        mSurfaceHolder.addCallback(mCallBack);//回调接口
    }


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


    public void registerReceiver() {
        filter = new IntentFilter();
        filter.addAction("com.android.Camera.takePhoto");
        filter.addAction("com.android.Camera.takePhotoFast");
        filter.addAction("com.android.Camera.closeCamera");
        filter.addAction("com.android.Camera.startVideo");
        filter.addAction("com.android.Camera.stopVideo");
        photographBroadCast = new PhotographBroadCast();
        registerReceiver(photographBroadCast, filter);
    }


    private void initCamera() {
        if (mCamera != null) {
            stopCamera();
        }
        //默认启动后置摄像头
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        if (mCamera == null) {
            Toast.makeText(this, "未能获取到相机！", Toast.LENGTH_SHORT).show();
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
        sendBroadcast(new Intent("com.android.Camera.stopvideo"));
        if (isRecording && mediaRecorder != null) {
            // 设置后不会崩
            mediaRecorder.setOnErrorListener(null);
            mediaRecorder.setOnInfoListener(null);
            mediaRecorder.setPreviewDisplay(null);
            //停止录制
            mediaRecorder.stop();
            mediaRecorder.reset();
            //释放资源
            mediaRecorder.release();
            mediaRecorder = null;

            mRecordTime.stop();
            mRecordTime.setVisibility(View.GONE);
            ivCamera.setVisibility(View.VISIBLE);
            isRecording = false;
            System.out.println("stop");
            SpUtil.writeString("videoPath", "");
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
                        stopCamera();
                        mRecordTime.stop();
                        mPauseTime = 0;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                initCamera();
//                                try {
//                                    if (!(saveVideoPath.equals(""))) {
//                                        String[] str = new String[]{saveVideoPath, currentVideoFilePath};
//                                        VideoUtils.appendVideo(CustomRecordActivity.this, getSDPath(CustomRecordActivity.this) + "append.mp4", str);
//                                        File reName = new File(saveVideoPath);
//                                        File f = new File(getSDPath(CustomRecordActivity.this) + "append.mp4");
//                                        //将合成的视频复制过来
//                                        f.renameTo(reName);
//                                        if (reName.exists()) {
//                                            f.delete();
//                                            new File(currentVideoFilePath).delete();
//                                            MediaScannerConnection.scanFile(CustomRecordActivity.this, new String[]{currentVideoFilePath}, null, null);
//                                        }
//                                    }
//
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
                            }
                        }).start();
                    }
                    MediaScannerConnection.scanFile(this, new String[]{currentVideoFilePath}, null, null);

                    mCurrentTime = System.currentTimeMillis();
                }
                break;
            case R.id.ivBack:
                if (back()) {
                    ActivityContainer.getInstance().finishAllActivity();
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
            Toast.makeText(this, "录制时间过短！", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        ActivityContainer.getInstance().finishAllActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();


        unregisterReceiver(photographBroadCast);
        if (isRecording) {
            mRecordControl.setImageResource(R.mipmap.recordvideo_start);
            stopRecord();
            mCamera.lock();
            stopCamera();
            mRecordTime.stop();
            mPauseTime = 0;
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
        //mediarecorder.setVideoEncodingBitRate(bitRat);
        // 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
//					if (fps != 0) {
//						mediarecorder.setVideoFrameRate(fps);
//					}
        //mediarecorder.setOrientationHint(90);
        mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
        //        //设置录像视频保存地址
        currentVideoFilePath = getSDPath(getApplicationContext()) + getVideoName();
        mediaRecorder.setOutputFile(currentVideoFilePath);
        SpUtil.writeString("videoPath", currentVideoFilePath);
//        mediaRecorder = new MediaRecorder();
//        mediaRecorder.reset();
//        mediaRecorder.setCamera(mCamera);
//        mediaRecorder.setOnErrorListener(OnErrorListener);
//
//        //使用SurfaceView预览
//        mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
//
//        //1.设置采集声音
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        //设置采集图像
//        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        //2.设置视频，音频的输出格式 mp4
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
//        //3.设置音频的编码格式
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//        //设置图像的编码格式
//        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        //设置立体声
//        //        mediaRecorder.setAudioChannels(2);
//        //设置最大录像时间 单位：毫秒
//        //        mediaRecorder.setMaxDuration(60 * 1000);
//        //设置最大录制的大小 单位，字节
//        //        mediaRecorder.setMaxFileSize(1024 * 1024);
//        //音频一秒钟包含多少数据位
//        CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
//        mediaRecorder.setAudioEncodingBitRate(44100);
//        if (mProfile.videoBitRate > 2 * 1024 * 1024) {
//            mediaRecorder.setVideoEncodingBitRate(2 * 1024 * 1024);
//        } else {
//            mediaRecorder.setVideoEncodingBitRate(1024 * 1024);
//        }
//        mediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);
//
//        //设置选择角度，顺时针方向，因为默认是逆向90度的，这样图像就是正常显示了,这里设置的是观看保存后的视频的角度
//        mediaRecorder.setOrientationHint(0);
//        //设置录像的分辨率
//        mediaRecorder.setVideoSize(640, 480);
//        //设置录像视频保存地址
//        currentVideoFilePath = getSDPath(getApplicationContext()) + getVideoName();
//        mediaRecorder.setOutputFile(currentVideoFilePath);
    }

    private String getVideoName() {
        return "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mp4";
    }


    private void setToResult() {
        Intent intent = new Intent();
        intent.putExtra("videoPath", currentVideoFilePath);
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
    protected void onDestroy() {
        super.onDestroy();

        ActivityContainer.getInstance().removeActivity(this);
    }
}
