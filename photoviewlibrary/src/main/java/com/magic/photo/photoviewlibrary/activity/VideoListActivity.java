package com.magic.photo.photoviewlibrary.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.fragments.VideoFragment;

import me.yokeyword.fragmentation.SupportActivity;

/**
 * @date 2017/6/26 09
 */
public class VideoListActivity extends SupportActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_common);
        loadRootFragment(R.id.framelayout_content, VideoFragment.newInstance());
        initListener();
    }

    private void initListener() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
