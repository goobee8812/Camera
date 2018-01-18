package com.magic.photo.photoviewlibrary.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.fragments.ImageFragment;
import com.magic.photo.photoviewlibrary.fragments.VideoFragment;

import me.yokeyword.fragmentation.SupportActivity;
import me.yokeyword.fragmentation.SupportFragment;

public class PhotoMainActivity extends SupportActivity implements TabLayout.OnTabSelectedListener, ImageFragment.OnPhotoSelectChangeListener, VideoFragment.OnVideoSelectChangeListener {

    private TabLayout mTabLayout;
    private SupportFragment[] mFragments;
    private Toolbar mToolbar;
    private RelativeLayout rlSelect;
    private ImageView ivCancel;
    private ImageFragment mImageFragment;
    private TextView tvCount;
    private TextView tvSel;
    private VideoFragment mVideoFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_photo);
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_common);
        rlSelect = (RelativeLayout) findViewById(R.id.rl_select);
        tvSel = (TextView) findViewById(R.id.tv_sel);
        tvCount = (TextView) findViewById(R.id.tv_count);
        ivCancel = (ImageView) findViewById(R.id.iv_cancel);
        mImageFragment = ImageFragment.newInstance();
        mImageFragment.setOnPhotoSelectChangeListener(this);
        mVideoFragment = VideoFragment.newInstance();
        mVideoFragment.setOnVideoSelectChangeListener(this);
        mFragments = new SupportFragment[]{mImageFragment, mVideoFragment};
        loadMultipleRootFragment(R.id.framelayout_content, 0, mFragments);
        mTabLayout.addOnTabSelectedListener(this);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageFragment.cancelPhotoSelect();
                mVideoFragment.cancelVideoSelect();
                cancelPhotoSelect();
            }
        });
    }


    public void showPhotoSelect() {
        mToolbar.setVisibility(View.INVISIBLE);
        rlSelect.setVisibility(View.VISIBLE);
    }

    public void cancelPhotoSelect() {
        mToolbar.setVisibility(View.VISIBLE);
        rlSelect.setVisibility(View.GONE);
    }

    public void showSelectPhotoCount(int count) {
        if (count == 0) {
            tvSel.setText("未选择");
            tvCount.setVisibility(View.GONE);
            mImageFragment.noPhotoSelect();
        } else {
            tvSel.setText("已选择");
            tvCount.setVisibility(View.VISIBLE);
            tvCount.setText(count + "");
            mImageFragment.photoSelect();
        }
    }

    public void showSelectVideoCount(int count) {
        if (count == 0) {
            tvSel.setText("未选择");
            tvCount.setVisibility(View.GONE);
            mVideoFragment.noVideoSelect();
        } else {
            tvSel.setText("已选择");
            tvCount.setVisibility(View.VISIBLE);
            tvCount.setText(count + "");
            mVideoFragment.videoSelect();
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int position = tab.getPosition();
        showHideFragment(mFragments[position]);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onBackPressedSupport() {
        if (mToolbar.getVisibility() != View.VISIBLE) {
            mImageFragment.cancelPhotoSelect();
            cancelPhotoSelect();
        } else {
            super.onBackPressedSupport();
        }

    }

    @Override
    public void onPhotoSelectChange(int count) {
        showSelectPhotoCount(count);
    }

    @Override
    public void onVideoSelectChange(int count) {
        showSelectVideoCount(count);
    }
}
