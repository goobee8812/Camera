package com.cloudring.magic.camera.photograph;

import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudring.magic.camera.R;
import com.cloudring.magic.camera.photograph.present.PhotographPresent;
import com.cloudring.magic.camera.photograph.present.PhotographPresentImpl;
import com.cloudring.magic.camera.photograph.utils.CameraPreview;

import java.io.File;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;


public class PhotographActivity extends AppCompatActivity {
    public static final int    TAKE_PHOTO   = 1;
    public static final int    CHOOSE_PHOTO = 2;
    public static final int    RECORDING    = 3;
    public static final String TAG          = "PhotographActivity";
    boolean isDelay3 = false;
    boolean isDelay6 = false;
    @BindView(R.id.ivDelay)
    ImageView ivDelay;
    @BindView(R.id.ivPhotoAlbum)
    public CircleImageView ivPhotoAlbum;
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
    @BindView(R.id.tvDelay6s)
    TextView tvDelay6s;
    @BindView(R.id.timedown)
    TextView teTimeDown;

    @BindColor(R.color.photo_delay_text_color_red)
    int delayColorRed;
    @BindColor(R.color.photo_delay_text_color_white)
    int delayColorWhite;
    private int count = 6;
    private Animation animation;   //动画

    private long   tinyid;
    private String familyName;

    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout mCameralayout;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;//默认使用前置摄像头(咚咚机器人神坑,CAMERA_FACING_BACK明明应该是后置....d)
    PhotographPresent photographPresent = new PhotographPresentImpl();
    private PhotographBroadCast photographBroadCast;
    private IntentFilter filter;
    private boolean             isTakePhoto;
    private MediaPlayer mMediaPlayer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_photograph);
        ButterKnife.bind(this);
        //        注册广播
        registerReceiver();
        initMediaPlayer();

        getFamilyData(getIntent());

    }

    private void getFamilyData(Intent intent) {
        tinyid = intent.getLongExtra("tinyid", 1);
        familyName = intent.getStringExtra("familyName");
        //        intent.putExtra("tinyid",tinyid);
        //        intent.putExtra("name",familyName);
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
//                startActivity(new Intent(this, PhotoMainAvtivity.class));
                releaseCamera();
                break;
            case R.id.ivBack:
                finish();
                break;
            case R.id.ivTakingPictures:
                if (isDelay3) {//延迟三秒
                    photographPresent.takePhotoDelay(3, mCamera, this);
                    PhotographPresentImpl.takePhotoLock = true;
                }
                if (isDelay6) {//延迟六秒
                    photographPresent.takePhotoDelay(6, mCamera, this);
                    PhotographPresentImpl.takePhotoLock = true;
                }
                if (!isDelay3 && !isDelay6) {//正常拍摄
                    //photographPresent.takePhoto(mCamera, this);
                    wantTakePic();
                }
                //                refreshPhotoOne();//无论如何都刷新相册image
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
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
        refreshPhotoOne(ivPhotoAlbum);
        PhotographPresentImpl.isPhotoStopThread = false;
        //        CameraRecognize.getInstance(this).create();
        isTakePhoto = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        isTakePhoto = false;
        PhotographPresentImpl.isPhotoStopThread = true;
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(photographBroadCast);
        releaseCamera();
        PhotographPresentImpl.isPhotoStopThread = true;
        handler.removeCallbacksAndMessages(null);
        //CameraRecognize.getInstance(this).destroy();
    }

    public void refreshPhotoOne(final ImageView view) {
      /*  ScanPhoto.getPhotoFromLocalStorage(this, new ScanPhoto.LookUpPhotosCallback() {//获取本地相册第一张图片
            @Override
            public void onSuccess(ArrayList<PhotoEntity> photoArrayList) {
                if (photoArrayList.size() == 0) {
                    view.setVisibility(View.GONE);
                    return;
                }
                view.setVisibility(View.VISIBLE);
                Glide.with(PhotographActivity.this).load(new File(photoArrayList.get(0).url)).into(view);
            }
        }, 1);*/


    }

    public void refreshPhotoOne(final ImageView view, File file) {
        /*if (file != null && view.getVisibility() == View.GONE) {
            view.setVisibility(View.VISIBLE);
        }
        Glide.with(PhotographActivity.this).load(file).into(view);*/

        try {
            ContentValues values = new ContentValues();
            values.put("face_url", file.getPath());
            ContentResolver contentResolver = getContentResolver();
            Uri uri = Uri.parse("content://com.tencent.devicedemo.db/TXBinderInfo");
            int update = contentResolver.update(uri, values, "tinyid =?", new String[]{String.valueOf(tinyid)});
            if (update != -1) {
//                ToastUtils.showToast(PhotographActivity.this, "更新成功");
            } else {
//                ToastUtils.showToast(PhotographActivity.this, "更新成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
//            ToastUtils.showToast(PhotographActivity.this, "更新成功");
        }

        //        FamilyPhoto f = new FamilyPhoto();
        //        f.imageUrl = file.getPath();
        //        f.tinyid = tinyid;
        //        f.name = familyName;
        //        f.save();
        Log.e(TAG, "refreshPhotoOne:  " + file.getPath() + "  " + tinyid + "  " + familyName);
        finish();
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
            Toast.makeText(this, "相机不支持", Toast.LENGTH_SHORT)
                    .show();
        } else {
            openCamera();
        }
    }

    // 开始预览相机
    public void openCamera() {
        if (mCamera == null) {
            mCamera = getCameraInstance();
            mPreview = new CameraPreview(PhotographActivity.this, mCamera);
            mCameralayout = (FrameLayout) findViewById(R.id.camera_preview);
            mCameralayout.addView(mPreview);
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

    /**
     * 前后置摄像头转换
     */
    public void switchCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, cameraInfo);
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        mCameralayout.removeView(mPreview);
        releaseCamera();
        openCamera();
    }


    // 释放相机
    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mCameralayout.removeView(mPreview);//注意释放surfaceView.否则唤醒回来surfaceView将是之前new的SurfaceView
        }
    }

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

    /**
     * 用于监听广播信息执行相机操作(主要是配合语音控制)
     * Created by 羊柯 on 2017/6/30.
     */

    public class PhotographBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "com.android.OpenCamera2"://相机语音识别

                    break;
                case "com.android.Camera.takePhoto"://延迟三秒
                    if (isTakePhoto) {
                        //photographPresent.takePhotoDelay(3, mCamera, PhotographActivity.this);
                        wantTakePic();
                    } else {
                        backRunnable();//如果处于录制状态,按返回键
                    }
                    break;
                case "com.android.Camera.takePhotoFast"://直接拍照
                    if (isTakePhoto) {
                        //photographPresent.takePhoto(mCamera, PhotographActivity.this);
                        wantTakePic();
                    } else {
                        backRunnable();//如果处于录制状态,按返回键
                    }
                    break;
                case "com.android.Camera.closeCamera"://按返回键
                    if (isTakePhoto) {
                        finish();
                    } else {
                        backRunnable();
                    }
                    break;
                case "com.android.Camera.startVideo"://启动录像广播
                    if (isTakePhoto) {
                        sendBroadcast(new Intent("com.android.video"));
                        photographPresent.reCording(PhotographActivity.this);//如果处于拍照状态,调用系统录像

                    } else {
                        sendBroadcast(new Intent("com.android.Camera.startVideoRecording"));
                    }
                    break;
                case "com.android.Camera.stopVideo"://停止录像广播
                    if (!isTakePhoto) {
                        sendBroadcast(new Intent("com.android.Camera.stopVideoRecording"));
                    }
                    break;
            }
        }
    }

    public void backRunnable() {
        new Thread() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (count == 0) {
                return;
            }
            int numNow = getCount();
            if (numNow != 0) {
                teTimeDown.setText("" + numNow);
                handler.sendEmptyMessageDelayed(1, 1000);
                big();
            }

        }
    };

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
                    photographPresent.takePhoto(mCamera, PhotographActivity.this);
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
        animation = AnimationUtils.loadAnimation(PhotographActivity.this, R.anim.animation_text);
        teTimeDown.startAnimation(animation);
        handler.sendEmptyMessageDelayed(1, 1000);
    }


}