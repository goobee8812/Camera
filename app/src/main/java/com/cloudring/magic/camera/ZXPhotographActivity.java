package com.cloudring.magic.camera;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudring.magic.camera.present.PhotographPresent;
import com.cloudring.magic.camera.present.PhotographPresentImpl;
import com.cloudring.magic.camera.present.SaveCallback;
import com.cloudring.magic.camera.utils.PackageUtils;
import com.cloudring.magic.camera.utils.PhotoEntity;
import com.cloudring.magic.camera.utils.PowerWakeLock;
import com.cloudring.magic.camera.utils.ScanPhoto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;


public class ZXPhotographActivity extends AppCompatActivity implements ScanPhoto.LookUpPhotosCallback, SaveCallback, SurfaceHolder.Callback {
    public static final String TAG = "ZXPhotographActivity";

    @BindView(R.id.ivPhotoAlbum)
    public CircleImageView ivPhotoAlbum;
    @BindView(R.id.ivDelay)
    ImageView ivDelay;
    @BindView(R.id.llDelaySetting)
    LinearLayout flDelaySetting;
    @BindView(R.id.ivDelayClose)
    ImageView ivDelayClose;
    @BindView(R.id.tvDelayClose)
    TextView tvDelayClose;
    @BindView(R.id.ivDelay3s)
    ImageView ivDelay3s;
    @BindView(R.id.tvDelay3s)
    TextView tvDelay3s;
    @BindView(R.id.ivDelay6s)
    ImageView ivDelay6s;
    @BindView(R.id.ivRecord)
    ImageView ivRecord;
    @BindView(R.id.tvDelay6s)
    TextView tvDelay6s;
    @BindView(R.id.timedown)
    TextView teTimeDown;
    @BindColor(R.color.photo_delay_text_color_red)
    int delayColorRed;
    @BindColor(R.color.photo_delay_text_color_white)
    int delayColorWhite;

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 3;
    boolean isDelay3 = false;
    boolean isDelay6 = false;
    private int count = 6;
    private Animation animation;   //动画
    private long mCurrentTime;
    private Camera mCamera;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;//默认使用前置摄像头(咚咚机器人神坑,CAMERA_FACING_BACK明明应该是后置....d)
    private PhotographBroadCast photographBroadCast;
    private MediaPlayer mMediaPlayer;
    private int releaseLockTime = 2 * 60 * 1000;
    private boolean isReleaseLock;
    private PhotographPresent photographPresent = new PhotographPresentImpl();
    private ScreenOffBroadCast screenOffBroadCast;

    private int width  = 1024;
    private int height = 768;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (count == 0) {
                return false;
            }
            int numNow = getCount();
            if (numNow != 0) {
                teTimeDown.setText("" + numNow);
                big();
            }
            return true;
        }
    });
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private int PREVIEW_WIDTH = 1280;
    private int PREVIEW_HEIGHT = 720;
    private int Picture_WIDTH = 1600;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zxphotograph);

        requestCameraPermissions();


        boolean openRecord = getIntent().getBooleanExtra("open_record", false);

        if (openRecord) {
            startRecord();
        }

        ButterKnife.bind(this);
        initMediaPlayer();

        registerScreenOffBroadCast();
        initView();
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.camera_preview);
        setSurfaceView();
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);

        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void registerScreenOffBroadCast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        screenOffBroadCast = new ScreenOffBroadCast();
        registerReceiver(screenOffBroadCast, filter);
    }

    private void requestCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ScanPhoto.getInstance(this).setOnLookUpPhotosCallback(this);
        initCamera();
        refreshPhotoOne();
        PhotographPresentImpl.isPhotoStopThread = false;
        initReceiver();

        isReleaseLock = true;
        resetLock();

    }


    private Runnable releaseLock = new Runnable() {
        @Override
        public void run() {
            PowerWakeLock.getInstance(ZXPhotographActivity.this).releaseLock();
            isReleaseLock = true;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
        ScanPhoto.getInstance(this).setOnLookUpPhotosCallback(null);
        releaseCamera();
        PhotographPresentImpl.isPhotoStopThread = true;
        handler.removeCallbacksAndMessages(null);
        unRegisterReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        Intent intent = new Intent("com.android.CloseCamera");
        sendBroadcast(intent);
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }

        if (screenOffBroadCast != null) {
            unregisterReceiver(screenOffBroadCast);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    finish();
                }
                return;
            }
        }
    }

    @OnClick({R.id.ivDelay, R.id.ivPhotoAlbum, R.id.ivBack, R.id.ivTakingPictures, R.id.ivRecord, R.id.flDelayClose, R.id.flDelay3s, R.id.flDelay6S})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivDelay:
                if (flDelaySetting.getVisibility() == View.GONE) {
                    flDelaySetting.setVisibility(View.VISIBLE);
                } else {
                    flDelaySetting.setVisibility(View.GONE);
                }
                break;
            case R.id.ivPhotoAlbum:
                PackageUtils.startApplication(this, "com.magic.photo.photoviewlibrary");
                releaseCamera();
                break;
            case R.id.ivBack:
                finish();
                break;
            case R.id.ivTakingPictures:
                if (isDelay3) {
                    //延迟三秒
                    photographPresent.takePhotoDelay(3, mCamera, this, this);
                    PhotographPresentImpl.takePhotoLock = true;
                }
                if (isDelay6) {
                    //延迟六秒
                    photographPresent.takePhotoDelay(6, mCamera, this, this);
                    PhotographPresentImpl.takePhotoLock = true;
                }
                if (!isDelay3 && !isDelay6) {
                    //正常拍摄
                    if (System.currentTimeMillis() - mCurrentTime > 1400) {
                        photographPresent.takePhoto(mCamera, this, this);
                        mCurrentTime = System.currentTimeMillis();
                        resetLock();
                    }
                }
                break;
            case R.id.ivRecord:
                startRecord();
                break;
            case R.id.flDelayClose:
                ivDelayClose.setImageResource(R.mipmap.delay_close_red);
                ivDelay3s.setImageResource(R.mipmap.delay_3s);
                ivDelay6s.setImageResource(R.mipmap.delay_6s);
                tvDelay3s.setTextColor(delayColorWhite);
                tvDelay6s.setTextColor(delayColorWhite);
                tvDelayClose.setTextColor(delayColorRed);
                isDelay3 = false;
                isDelay6 = false;
                break;
            case R.id.flDelay3s:
                ivDelay3s.setImageResource(R.mipmap.delay_3s_red);
                ivDelay6s.setImageResource(R.mipmap.delay_6s);
                ivDelayClose.setImageResource(R.mipmap.delay_close);
                tvDelay3s.setTextColor(delayColorRed);
                tvDelay6s.setTextColor(delayColorWhite);
                tvDelayClose.setTextColor(delayColorWhite);
                isDelay3 = true;
                isDelay6 = false;
                break;
            case R.id.flDelay6S:
                ivDelay6s.setImageResource(R.mipmap.delay_6s_red);
                ivDelay3s.setImageResource(R.mipmap.delay_3s);
                ivDelayClose.setImageResource(R.mipmap.delay_close);
                tvDelay3s.setTextColor(delayColorWhite);
                tvDelayClose.setTextColor(delayColorWhite);
                tvDelay6s.setTextColor(delayColorRed);
                isDelay3 = false;
                isDelay6 = true;
                break;
            default:
                break;
        }
    }

    private void startRecord() {
        Intent intent = new Intent(this, CustomRecordActivity.class);
        startActivityForResult(intent, 0);
        finish();
    }

    private void initReceiver() {
        registerReceiver();
        Intent intent = new Intent("com.android.OpenCamera");
        sendBroadcast(intent);
    }

    private void unRegisterReceiver() {
        if (photographBroadCast != null) {
            unregisterReceiver(photographBroadCast);
        }

    }

    public void refreshPhotoOne() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ScanPhoto.getInstance(ZXPhotographActivity.this).getPhotoFromLocalStorage();
            }
        }, 500);
    }

    public void refreshPhoto(String photoPath) {
        ivPhotoAlbum.setVisibility(View.VISIBLE);
        Glide.with(ZXPhotographActivity.this).load(photoPath).into(ivPhotoAlbum);
    }

    // 判断相机是否支持
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK) {
            finish();
        }
    }


    public void initCamera() {
        if (!checkCameraHardware(this)) {
            Toast.makeText(this, "相机不支持", Toast.LENGTH_SHORT).show();
        } else {
            openCamera();
        }
    }

    // 开始预览相机
    public void openCamera() {
        if (mCamera != null) {
            releaseCamera();
        }
        if (mCamera == null) {
            mCamera = getCameraInstance();
        }
        if (mCamera == null) {
            // Toast.makeText(this, "打开相机失败", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "openCamera: 打开相机失败");
            return;
        }
        try {
            //配置CameraParams
            setCameraParams();
            mCamera.setPreviewDisplay(mHolder);
            //启动相机预览
            mCamera.startPreview();

        } catch (IOException e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }


    // 获取相机实例
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(mCameraId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
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
    public void onSuccess(ArrayList<PhotoEntity> photoArrayList) {
        if (photoArrayList.size() == 0) {
            ivPhotoAlbum.setVisibility(View.GONE);
            return;
        }
        ivPhotoAlbum.setVisibility(View.VISIBLE);
        Glide.with(ZXPhotographActivity.this).load(new File(photoArrayList.get(0).url)).into(ivPhotoAlbum);
    }

    @Override
    public void success(String photoPath) {
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + photoPath)));
        refreshPhoto(photoPath);
    }

    @Override
    public void onError(Exception e) {

    }


    private void setCameraParams() {
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setPreviewFormat(ImageFormat.NV21);
            String model = Build.MODEL;
            if (model.contains("R610")) {
                parameters.setPreviewSize(1280, 960);
                parameters.setPictureSize(1280, 960);
            } else if (model.contains("R631")
                    || model.contains("R633")
                    || model.contains("R636")
                    || model.contains("R668")
                    || model.contains("R651")
                    || model.contains("R652")
                    || model.contains("R660")
                    || model.contains("R661")) {
                parameters.setPreviewSize(width, height);
                parameters.setPictureSize(width, height);
            } else {
                List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
                for (Camera.Size previewSize : previewSizes) {
                    if (previewSize.width == PREVIEW_WIDTH && previewSize.height == PREVIEW_HEIGHT) {
                        parameters.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
                        break;
                    }
                }


                List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
                Camera.Size fs = null;
                for (int i = 0; i < pictureSizes.size(); i++) {
                    Camera.Size psize = pictureSizes.get(i);
                    if (psize.width == Picture_WIDTH) {
                        fs = psize;
                    }
                }
                parameters.setPictureSize(fs.width, fs.height);


            }


            mCamera.setParameters(parameters);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            return;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    public class PhotographBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "com.android.OpenCamera2"://相机语音识别

                    break;
                case "com.android.Camera.takePhoto"://延迟三秒
                    wantTakePic();
                    break;
                case "com.android.Camera.takePhotoFast"://直接拍照
                    takePhoto();
                    break;
                case "com.android.Camera.closeCamera"://按返回键
                    finish();
                    break;
                case "com.android.Camera.startVideo"://启动录像广播
                    //VoiceTTSManager.getInstance(CommonLib.getContext()).speak("好的,咚咚正在为您打开！");
                    ivRecord.performClick();
                    break;
                case "com.android.Camera.stopVideo"://停止录像广播
                    ivRecord.performClick();
                    break;
                default:
                    break;
            }
        }
    }

    public class ScreenOffBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, Intent.ACTION_SCREEN_OFF)) {
                Intent closeCamera = new Intent("com.android.CloseCamera");
                sendBroadcast(closeCamera);
            } else if (TextUtils.equals(action, "iflytek.intend.action.WAKE_UP")) {
                finish();
            }
        }
    }

    private int getCount() {
        count--;
        if (count == 0) {
            teTimeDown.setVisibility(View.GONE);
            takePhoto();
        }
        return count;
    }


    private void initMediaPlayer() {
        try {
            mMediaPlayer = MediaPlayer.create(this, R.raw.takephotoes);
            // 确保我们的MediaPlayer在播放时获取了一个唤醒锁，
            // 如果不这样做，当歌曲播放很久时，CPU进入休眠从而导致播放停止
            // 要使用唤醒锁，要确保在AndroidManifest.xml中声明了android.permission.WAKE_LOCK权限
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // 在MediaPlayer在它准备完成时、完成播放时、发生错误时通过监听器通知我们，
            // 以便我们做出相应处理
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer player) {
                    //	isPrepared = true;
                    player.setVolume(1.0f, 1.0f); // 设置大声播放
                    //  player.start();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer arg0) {

                    if (animation != null) {     //animation为null此时是直接进入相机进行拍照
                        animation.cancel();
                    }
                    //开始照相
                    photographPresent.takePhoto(mCamera, ZXPhotographActivity.this, ZXPhotographActivity.this);

                    resetLock();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetLock() {
        if (isReleaseLock) {
            PowerWakeLock.getInstance(this).acquire();
            isReleaseLock = false;
        }
        handler.removeCallbacks(releaseLock);
        handler.postDelayed(releaseLock, releaseLockTime);
    }

    public void big() {
        try {
            animation.reset();
            teTimeDown.startAnimation(animation);
            handler.sendEmptyMessageDelayed(1, 1000);
        } catch (Exception e) {
            e.printStackTrace();
            teTimeDown.setVisibility(View.GONE);
            takePhoto();
        }

    }

    private void wantTakePic() {
        try {
            count = 2;
            teTimeDown.setText("准备");
            teTimeDown.setVisibility(View.VISIBLE);
            animation = AnimationUtils.loadAnimation(ZXPhotographActivity.this, R.anim.animation_text);
            teTimeDown.startAnimation(animation);
            handler.sendEmptyMessageDelayed(1, 1000);
        } catch (Exception e) {
            e.printStackTrace();
            teTimeDown.setVisibility(View.GONE);
            takePhoto();
        }
    }

    private void takePhoto() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        } else {
            if (animation != null) {
                animation.cancel();
            }
            //开始照相
            photographPresent.takePhoto(mCamera, ZXPhotographActivity.this, ZXPhotographActivity.this);

            resetLock();
        }
    }

    //强制设置SurfaceView的宽高比，不然会有拉伸~
    private void setSurfaceView() {
        String model = Build.MODEL;
        FrameLayout.LayoutParams SURFACE_VIEW = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
        if (model.equals("R610")) {
            SURFACE_VIEW.width = 1280;
            SURFACE_VIEW.height = 960;
        } else {
            SURFACE_VIEW.width = 1024;
            SURFACE_VIEW.height = 768;
        }
        mSurfaceView.setLayoutParams(SURFACE_VIEW);
    }
}