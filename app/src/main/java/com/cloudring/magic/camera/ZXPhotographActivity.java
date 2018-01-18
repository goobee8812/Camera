package com.cloudring.magic.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.cloudring.magic.camera.utils.CameraPreview;
import com.cloudring.magic.camera.utils.PhotoEntity;
import com.cloudring.magic.camera.utils.ScanPhoto;
import com.magic.photo.photoviewlibrary.activity.PhotoMainActivity;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;


public class ZXPhotographActivity extends AppCompatActivity implements ScanPhoto.LookUpPhotosCallback {
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
    @BindView(R.id.ivRecording)
    ImageView ivRecording;
    @BindView(R.id.tvDelay6s)
    TextView tvDelay6s;
    @BindView(R.id.timedown)
    TextView teTimeDown;
    @BindColor(R.color.photo_delay_text_color_red)
    int delayColorRed;
    @BindColor(R.color.photo_delay_text_color_white)
    int delayColorWhite;

    boolean isDelay3 = false;
    boolean isDelay6 = false;
    private int count = 6;
    private Animation animation;   //动画
    private long mCurrentTime;
    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout mCameralayout;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;//默认使用前置摄像头(咚咚机器人神坑,CAMERA_FACING_BACK明明应该是后置....d)
    private PhotographBroadCast photographBroadCast;
    private MediaPlayer  mMediaPlayer;

    PhotographPresent photographPresent = new PhotographPresentImpl();
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
                if (count == 0) {
                    return false;
                }
                int numNow = getCount();
                if (numNow != 0) {
                    teTimeDown.setText("" + numNow);
                    handler.sendEmptyMessageDelayed(1, 1000);
                    big();
                }
            return true;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zxphotograph);
        ButterKnife.bind(this);
        bindVoiceService();
        initMediaPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ScanPhoto.getInstance(this).setOnLookUpPhotosCallback(this);
        initCamera();
        refreshPhotoOne();
        PhotographPresentImpl.isPhotoStopThread = false;
        initReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ScanPhoto.getInstance(this).setOnLookUpPhotosCallback(null);
        releaseCamera();
        PhotographPresentImpl.isPhotoStopThread = true;
        handler.removeCallbacksAndMessages(null);
        unRegisterReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
        PhotographPresentImpl.isPhotoStopThread = true;
        handler.removeCallbacksAndMessages(null);
        unbindService(MyServiceConnection.getInstance().conn);
        MyServiceConnection.getInstance().remoteService = null;
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }

    private void bindVoiceService() {
        Intent it = new Intent();
        it.setPackage("com.cloudring.magic");
        it.setAction("com.cloudring.voice.IRemoteService");
        bindService(it, MyServiceConnection.getInstance().conn, BIND_AUTO_CREATE);
    }

    @OnClick({R.id.ivDelay, R.id.ivPhotoAlbum, R.id.ivBack, R.id.ivTakingPictures, R.id.ivRecording, R.id.flDelayClose, R.id.flDelay3s, R.id.flDelay6S})
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
                startActivity(new Intent(this, PhotoMainActivity.class));
                releaseCamera();
                break;
            case R.id.ivBack:
                finish();
                break;
            case R.id.ivTakingPictures:
                if (isDelay3) {
                    //延迟三秒
                    photographPresent.takePhotoDelay(3, mCamera, this);
                    PhotographPresentImpl.takePhotoLock = true;
                }
                if (isDelay6) {
                    //延迟六秒
                    photographPresent.takePhotoDelay(6, mCamera, this);
                    PhotographPresentImpl.takePhotoLock = true;
                }
                if (!isDelay3 && !isDelay6) {
                    //正常拍摄
                    if (System.currentTimeMillis() - mCurrentTime > 1000) {
                        photographPresent.takePhoto(mCamera, this);
                        mCurrentTime = System.currentTimeMillis();
                    }
                }
                break;
            case R.id.ivRecording:
                photographPresent.reCording(this);
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

    private void initReceiver() {
        registerReceiver();
        Intent intent = new Intent("com.android.OpenCamera");
        sendBroadcast(intent);
    }

    private void unRegisterReceiver() {
        if (photographBroadCast != null) {
            unregisterReceiver(photographBroadCast);
        }
        Intent intent = new Intent("com.android.CloseCamera");
        sendBroadcast(intent);
    }

    public void refreshPhotoOne() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ScanPhoto.getInstance(ZXPhotographActivity.this).getPhotoFromLocalStorage();
            }
        }, 500);
    }

    public void refreshPhoto(String photoPath){
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


    public void initCamera() {
        if (!checkCameraHardware(this)) {
            Toast.makeText(this, "相机不支持", Toast.LENGTH_SHORT).show();
        } else {
            openCamera();
        }
    }

    // 开始预览相机
    public void openCamera() {
        if (mCamera == null) {
            mCamera = getCameraInstance();
        }

        if (mPreview == null) {
            mPreview = new CameraPreview(ZXPhotographActivity.this, mCamera);

        }
        if (mCameralayout == null) {
            mCameralayout = (FrameLayout) findViewById(R.id.camera_preview);
        }
        mCameralayout.addView(mPreview);

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
        long lastTime = System.currentTimeMillis();
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mCameralayout.removeView(mPreview);//注意释放surfaceView.否则唤醒回来surfaceView将是之前new的SurfaceView
        }
        if (mPreview != null) {
            mPreview = null;
        }
        Log.i(TAG, "releaseCamera: newPreviewToUseTime = " + (System.currentTimeMillis() - lastTime));
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
                    mMediaPlayer.start();
                    break;
                case "com.android.Camera.closeCamera"://按返回键
                    finish();
                    break;
                case "com.android.Camera.startVideo"://启动录像广播
                    //VoiceTTSManager.getInstance(CommonLib.getContext()).speak("好的,咚咚正在为您打开！");
                    ivRecording.performClick();
                    break;
                case "com.android.Camera.stopVideo"://停止录像广播
                    try {
                        MyServiceConnection.getInstance().remoteService.speak("蛋蛋还是先帮您打开录像吧！");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    ivRecording.performClick();
                    break;
                default:
                    break;
            }
        }
    }

    private int getCount() {
        count--;
        if (count == 0) {
            teTimeDown.setVisibility(View.GONE);
            mMediaPlayer.start();
        }
        return count;
    }


    private void initMediaPlayer() {
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
                    //开始照相
                    photographPresent.takePhoto(mCamera, ZXPhotographActivity.this);
                }


            }
        });
    }

    public void big() {
        animation.reset();
        teTimeDown.startAnimation(animation);
    }

    private void wantTakePic() {
        count = 2;
        teTimeDown.setText("准备");
        teTimeDown.setVisibility(View.VISIBLE);
        animation = AnimationUtils.loadAnimation(ZXPhotographActivity.this, R.anim.animation_text);
        teTimeDown.startAnimation(animation);
        handler.sendEmptyMessageDelayed(1, 1000);
    }


}