package com.magic.photo.photoviewlibrary.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.customView.CustomVideoView;
import com.magic.photo.photoviewlibrary.entity.Video;
import com.magic.photo.photoviewlibrary.manager.FileManager;
import com.magic.photo.photoviewlibrary.utils.FileUtils;
import com.magic.photo.photoviewlibrary.utils.TimeUtil;
import com.magic.photo.photoviewlibrary.utils.Utils;

import java.util.List;

/**
 * 2017/6/25 10
 */
public class VideoActivity extends AppCompatActivity implements CustomVideoView.ADVideoPlayerListener, SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private static final String TAG = "VideoActivity";
    private static final String KEY_DATA = "data";
    private FrameLayout mVideoContent;
    private TextView mPlayTime;
    private SeekBar mSeekBar;
    private TextView mTotalTime;
    private Video mVideo;
    private CustomVideoView mCustomVideoView;
    private ImageView mBack;
    private LinearLayout mController;
    private ImageView mDelete;
    private Dialog mDialog;
    private List<Video> mVideos;
    private int position;
    private ImageView mPre;
    private ImageView mNext;
    private ImageView mPlay;
    private FrameLayout mTopController;
    private boolean isShowController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_photo);
        initView();
        initData();
        initVideoView();
        initController();
        initListener();
    }

    private void initController() {
        mTotalTime.setText(TimeUtil.formatDuration((int) mVideo.duration));
        mSeekBar.setMax((int) mVideo.duration);
    }

    private void initData() {
        mVideo = (Video) getIntent().getSerializableExtra(KEY_DATA);
        mVideos = FileManager.getInstance().getVideosFromCamera(this);
        for (int i = 0; i < mVideos.size(); i++) {
            Video video = mVideos.get(i);
            if (video.id == mVideo.id) {
                position = i;
                break;
            }
        }
    }

    private void initVideoView() {
        mCustomVideoView = new CustomVideoView(this, mVideoContent);
        mCustomVideoView.setDataSource(mVideo.path);
        mCustomVideoView.setListener(this);
        mVideoContent.addView(mCustomVideoView);
    }

    private void initView() {
        mVideoContent = (FrameLayout) findViewById(R.id.frameLayout_video);
        mTopController = (FrameLayout) findViewById(R.id.framelayout_top);
        mPlayTime = (TextView) findViewById(R.id.music_play_time);
        mSeekBar = (SeekBar) findViewById(R.id.music_seekbar);
        mTotalTime = (TextView) findViewById(R.id.music_all_timed);
        mBack = (ImageView) findViewById(R.id.imageView_back);
        mController = (LinearLayout) findViewById(R.id.linearLayout_controller);
        mDelete = (ImageView) findViewById(R.id.imageView_delete);
        mPre = (ImageView) findViewById(R.id.music_pre);
        mNext = (ImageView) findViewById(R.id.music_next);
        mPlay = (ImageView) findViewById(R.id.music_play);
    }

    private void initListener() {
        mSeekBar.setOnSeekBarChangeListener(this);
        mBack.setOnClickListener(this);
        mDelete.setOnClickListener(this);
        mPre.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mPlay.setOnClickListener(this);
        mVideoContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    return true;
                }
                return false;
            }
        });
        mTopController.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBufferUpdate(int time) {
        mPlayTime.setText(TimeUtil.formatDuration(time));
        mSeekBar.setProgress(time);
    }

    @Override
    public void onClickFullScreenBtn() {

    }

    @Override
    public void onClickVideo() {
        if (isShowController) {
            showController();
        } else {
            hideController();
        }
        isShowController = !isShowController;
    }

    private void showController() {
        mController.animate().translationY(0).setDuration(200).start();
        mTopController.animate().translationY(0).setDuration(200).start();
    }

    private void hideController() {
        mController.animate().translationY(Utils.dip2px(this, 80)).setDuration(200).start();
        mTopController.animate().translationY(Utils.dip2px(this, -80)).setDuration(200).start();
    }

    @Override
    public void onClickBackBtn() {

    }

    @Override
    public void onClickPlay() {
        if (mCustomVideoView.isComplete()) {
            mCustomVideoView.load();
        } else {
            if (mCustomVideoView.isPlaying()) {
                mPlay.setImageResource(R.drawable.play_2);
                mCustomVideoView.pause();
            } else {
                mPlay.setImageResource(R.drawable.pause_2);
                mCustomVideoView.resume();
            }
        }
    }

    @Override
    public void onAdVideoLoadSuccess() {
        hideController();
        isShowController = false;
        mSeekBar.setProgress(0);
        mSeekBar.setMax((int) getCurrentVideo().duration);
        mTotalTime.setText(TimeUtil.formatDuration((int) getCurrentVideo().duration));
        mPlay.setImageResource(R.drawable.play_2);
    }

    @Override
    public void onAdVideoLoadFailed() {

    }

    @Override
    public void onAdVideoLoadComplete() {
        mSeekBar.setProgress(0);
        mPlay.setImageResource(R.drawable.pause_2);
        mPlayTime.setText(TimeUtil.formatDuration(0));
        mTotalTime.setText(TimeUtil.formatDuration((int) getCurrentVideo().duration));
        showController();
        mCustomVideoView.stopMedia();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d(TAG, "onProgressChanged: "+fromUser);
        if (fromUser) {
            if (mCustomVideoView.isPlaying()) {
                mCustomVideoView.seekAndResume(progress);
            } else {
                mCustomVideoView.seekAndPause(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    protected void onDestroy() {
        mCustomVideoView.destroy();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imageView_back) {
            finish();
        } else if (v.getId() == R.id.imageView_delete) {
            mCustomVideoView.pause();
            if (mDialog != null) {
                mDialog.dismiss();
            }
            mDialog = createDeleteDialog();
            mDialog.show();
        } else if (v.getId() == R.id.cancel) {
            if (mDialog != null) {
                mDialog.dismiss();
            }
            mCustomVideoView.resume();
        } else if (v.getId() == R.id.confirm) {
            mCustomVideoView.stopMedia();
            boolean deleteFile = FileUtils.deleteFile(this, mCustomVideoView.getUrl());
            if (deleteFile) {
                mVideos.remove(position);
                if (isPositionExist()) {
                    playOther(position);
                } else if (hasPre()) {
                    playOther(--position);
                } else {
                    finish();
                }
            } else {
                Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
            }
            if (mDialog != null) {
                mDialog.dismiss();
            }
        } else if (v.getId() == R.id.music_next) {
            mCustomVideoView.stopMedia();
            if (hasNext()) {
                playOther(++position);
            }
        } else if (v.getId() == R.id.music_pre) {
            mCustomVideoView.stopMedia();
            if (hasPre()) {
                playOther(--position);
            }
        } else if (v.getId() == R.id.music_play) {
            if (mCustomVideoView.isComplete()) {
                mCustomVideoView.load();
            } else {
                if (mCustomVideoView.isPlaying()) {
                    mCustomVideoView.pause();
                    mPlay.setImageResource(R.drawable.pause_2);
                } else {
                    mCustomVideoView.resume();
                    mPlay.setImageResource(R.drawable.play_2);
                }
            }
        }
    }

    private void playOther(int index) {
        mCustomVideoView.setDataSource(mVideos.get(index).path);
        mCustomVideoView.load();
    }

    private Dialog createDeleteDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_photo_delete);
        dialog.findViewById(R.id.cancel).setOnClickListener(this);
        dialog.findViewById(R.id.confirm).setOnClickListener(this);
        return dialog;
    }

    public boolean hasNext() {
        if (mVideos.isEmpty()) {
            return false;
        }
        if (position + 1 >= mVideos.size()) {
            return false;
        }
        return true;
    }

    public boolean hasPre() {
        if (mVideos.isEmpty()) {
            return false;
        }
        if (position - 1 < 0) {
            return false;
        }
        return true;
    }

    public boolean isPositionExist(){
        if (mVideos.isEmpty()) {
            return false;
        }
        if (position >= mVideos.size()) {
            return false;
        }
        return true;
    }

    public Video getCurrentVideo() {
        Video video = null;
        String url = mCustomVideoView.getUrl();
        for (int i = 0; i < mVideos.size(); i++) {
            if (url.equals(mVideos.get(i).path)) {
                video = mVideos.get(i);
                break;
            }
        }
        return video;
    }
}
