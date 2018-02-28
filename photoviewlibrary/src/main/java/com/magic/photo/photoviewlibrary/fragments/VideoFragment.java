package com.magic.photo.photoviewlibrary.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.activity.PhotoMainActivity;
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
public class VideoFragment extends SupportFragment implements OnRecyclerItemClickListener.ItemClickListener, OnRecyclerItemClickListener.ItemLongClickListener, View.OnClickListener {
    private static final String TAG = "VideoFragment";
    private int count;
    private Activity mActivity;
    private RecyclerView mRecyclerView;
    private VideoAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private RelativeLayout llSelect;
    private TextView tvDelete;
    private TextView tvAllselect;
    private OnVideoSelectChangeListener listener;

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
        llSelect = (RelativeLayout) view.findViewById(R.id.ll_select);
        tvDelete = (TextView) view.findViewById(R.id.tv_delete);
        tvAllselect = (TextView) view.findViewById(R.id.tv_allselect);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new VideoAdapter(mActivity, count, listener);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                boolean isHeaderOrFooter = mAdapter.isFooterPosition(position);
                return isHeaderOrFooter ? count : (mAdapter.getVideoWraps().get(position).isTitle ? count : 1);
            }
        });
        OnRecyclerItemClickListener onRecyclerItemClickListener = new OnRecyclerItemClickListener(mRecyclerView);
        mRecyclerView.addOnItemTouchListener(onRecyclerItemClickListener);
        onRecyclerItemClickListener.setOnItemClickListener(this);
        onRecyclerItemClickListener.setOnItemLongClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        tvDelete.setOnClickListener(this);
        tvAllselect.setOnClickListener(this);
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void initData() {
        List<Video> videos = FileManager.getInstance().getVideosFromCamera(mActivity);
        mAdapter.setVideos(videos);
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder vh) {
        int position = vh.getAdapterPosition();
        if (mAdapter.isFooterPosition(position)) {
            return;
        }
        if (!mAdapter.isTitle(position)) {
            if (mAdapter.getSelectState()) {
                if (mAdapter.isSelectTheVideo(position)) {
                    mAdapter.removeSelectVideo(position);
                    mAdapter.notifyDataSetChanged();
                } else {
                    mAdapter.saveSelectVideo(position);
                    mAdapter.notifyDataSetChanged();
                }

            } else {
                Video video = mAdapter.getVideo(position);
                Intent intent = new Intent(mActivity, VideoActivity.class);
                intent.putExtra("data", video);
                mActivity.startActivity(intent);
            }
        }
    }

    @Override
    public void onItemLongClick(RecyclerView.ViewHolder vh) {
        int position = vh.getAdapterPosition();
        if (mAdapter.isFooterPosition(position)) {
            return;
        }
        if (!mAdapter.isTitle(position)) {
            if (llSelect.getVisibility() == View.GONE) {
                ((PhotoMainActivity) getActivity()).showPhotoSelect();
                llSelect.setVisibility(View.VISIBLE);
                mAdapter.setSelectState(true);
                mAdapter.saveSelectVideo(position);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public void cancelVideoSelect() {
        llSelect.setVisibility(View.GONE);
        mAdapter.clearSelectVideo();
        mAdapter.setSelectState(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_delete) {
            createDeleteDialog();
        } else if (id == R.id.tv_allselect) {
            mAdapter.allSelectVideos();
        }
    }


    public interface OnVideoSelectChangeListener {
        void onVideoSelectChange(int count);
    }

    public void setOnVideoSelectChangeListener(OnVideoSelectChangeListener listener) {
        this.listener = listener;
    }


    private void createDeleteDialog() {

        AlertDialog builder = new AlertDialog.Builder(getContext())
                .setMessage(String.format("是否删除所选%s个文件", mAdapter.getSelectVideo()))
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAdapter.delectVideos();

                        dialog.dismiss();
                        cancelVideoSelect();
                        ((PhotoMainActivity) getActivity()).cancelPhotoSelect();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();

        Window dialogWindow = builder.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialogWindow.setWindowAnimations(R.style.dialogstyle); // 添加动画
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = -20; // 新位置Y坐标
        lp.width = (int) getResources().getDisplayMetrics().widthPixels; // 宽度
        lp.alpha = 9f; // 透明度
        dialogWindow.setAttributes(lp);
        builder.show();
    }


    public void noVideoSelect() {

        tvDelete.setAlpha(0.5f);
        tvDelete.setClickable(false);

        tvAllselect.setText("全选");
    }

    public void videoSelect() {

        tvDelete.setAlpha(1.0f);
        tvDelete.setClickable(true);

        if (mAdapter.isAllVideoSelect()) {
            tvAllselect.setText("取消全选");
        } else {
            tvAllselect.setText("全选");
        }
    }

}
