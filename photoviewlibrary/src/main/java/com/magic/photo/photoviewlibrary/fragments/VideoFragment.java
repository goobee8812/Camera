package com.magic.photo.photoviewlibrary.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.activity.VideoActivity;
import com.magic.photo.photoviewlibrary.adapter.VideoAdapter;
import com.magic.photo.photoviewlibrary.entity.Video;
import com.magic.photo.photoviewlibrary.manager.FileManager;
import com.magic.photo.photoviewlibrary.utils.OnRecyclerItemClickListener;

import java.util.List;

import me.yokeyword.fragmentation.SupportFragment;

/**
 * 2017/6/24 16
 */
public class VideoFragment extends SupportFragment implements OnRecyclerItemClickListener.ItemClickListener {
    private static final String TAG = "VideoFragment";
    private int count;
    private Activity mActivity;
    private RecyclerView mRecyclerView;
    private VideoAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
//    private GridItemDecoration mItemDecoration;

    public static VideoFragment newInstance() {
        return new VideoFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayoutManager = new GridLayoutManager(mActivity, count = 4);
//        mItemDecoration = new GridItemDecoration(mActivity, R.color.main_color);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video_list_layout, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
//        mRecyclerView.addItemDecoration(mItemDecoration);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new VideoAdapter(mActivity, count);
//        mItemDecoration.setAdapter(mAdapter);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mAdapter.getVideoWraps().get(position).isTitle ? count : 1;
            }
        });
        OnRecyclerItemClickListener onRecyclerItemClickListener = new OnRecyclerItemClickListener(mRecyclerView);
        mRecyclerView.addOnItemTouchListener(onRecyclerItemClickListener);
        onRecyclerItemClickListener.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        List<Video> videos = FileManager.getInstance().getVideosFromMedia(mActivity);
        mAdapter.setVideos(videos);
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder vh) {
        int position = vh.getAdapterPosition();
        if (!mAdapter.isTitle(position)) {
            Video video = mAdapter.getVideo(position);
            Intent intent = new Intent(mActivity, VideoActivity.class);
            intent.putExtra("data", video);
            mActivity.startActivity(intent);
        }
    }

}
