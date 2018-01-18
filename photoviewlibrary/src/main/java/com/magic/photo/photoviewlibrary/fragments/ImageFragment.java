package com.magic.photo.photoviewlibrary.fragments;

import android.app.AlertDialog;
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
import android.widget.Toast;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.activity.CheckImageActivity;
import com.magic.photo.photoviewlibrary.activity.PhotoMainActivity;
import com.magic.photo.photoviewlibrary.adapter.ImageAdapter;
import com.magic.photo.photoviewlibrary.entity.Image;
import com.magic.photo.photoviewlibrary.manager.FileManager;
import com.magic.photo.photoviewlibrary.utils.OnRecyclerItemClickListener;

import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import me.yokeyword.fragmentation.SupportFragment;


/**
 * @date 2017/6/27 09
 */
public class ImageFragment extends SupportFragment implements OnRecyclerItemClickListener.ItemClickListener, OnRecyclerItemClickListener.ItemLongClickListener, View.OnClickListener {

    private static final String TAG = "VideoFragment";
    private int count;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private ImageAdapter mAdapter;
    //    private GridImageItemDecoration mItemDecoration;
    private RelativeLayout llSelect;
    private OnPhotoSelectChangeListener listener;
    private TextView tvShare;
    private TextView tvDelete;
    private TextView tvAllselect;

    public static ImageFragment newInstance() {
        return new ImageFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayoutManager = new GridLayoutManager(_mActivity, count = 4);
//        mItemDecoration = new GridImageItemDecoration(_mActivity, R.color.main_color);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_list_layout, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        llSelect = (RelativeLayout) view.findViewById(R.id.ll_select);
        tvShare = (TextView) view.findViewById(R.id.tv_share);
        tvDelete = (TextView) view.findViewById(R.id.tv_delete);
        tvAllselect = (TextView) view.findViewById(R.id.tv_allselect);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ImageAdapter(_mActivity, count, listener);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                boolean isHeaderOrFooter = mAdapter.isFooterPosition(position);

                return isHeaderOrFooter ? count : (mAdapter.getImageWraps().get(position).isTitle ? count : 1);
            }
        });
        OnRecyclerItemClickListener onRecyclerItemClickListener = new OnRecyclerItemClickListener(mRecyclerView);
        mRecyclerView.addOnItemTouchListener(onRecyclerItemClickListener);
        onRecyclerItemClickListener.setOnItemClickListener(this);
        onRecyclerItemClickListener.setOnItemLongClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        tvShare.setOnClickListener(this);
        tvDelete.setOnClickListener(this);
        tvAllselect.setOnClickListener(this);
    }

    private void initData() {
        List<Image> images = FileManager.getInstance().getImageFormMedia(_mActivity);
        mAdapter.setImages(images);
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder vh) {
        int position = vh.getAdapterPosition();
        if (mAdapter.isFooterPosition(position)) {
            return;
        }
        if (!mAdapter.isTitle(position)) {

            if (mAdapter.getSelectState()) {
                if (mAdapter.isSelectThePhoto(position)) {
                    mAdapter.removeSelectPhoto(position);
                    mAdapter.notifyDataSetChanged();
                } else {
                    mAdapter.saveSelectPhoto(position);
                    mAdapter.notifyDataSetChanged();
                }

            } else {
                Image image = mAdapter.getImage(position);
                Intent intent = new Intent(_mActivity, CheckImageActivity.class);
                intent.putExtra("data", image.id);
                _mActivity.startActivity(intent);
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initData();
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
                mAdapter.saveSelectPhoto(position);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public void cancelPhotoSelect() {
        llSelect.setVisibility(View.GONE);
        mAdapter.clearSelectPhoto();
        mAdapter.setSelectState(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_share) {
            List<String> selectImagePath = mAdapter.getSelectImagePath();
            showShare(selectImagePath);
        } else if (id == R.id.tv_delete) {
            createDeleteDialog();
        } else if (id == R.id.tv_allselect) {
            mAdapter.allSelectPhotos();
        }
    }


    public interface OnPhotoSelectChangeListener {
        void onPhotoSelectChange(int count);
    }

    public void setOnPhotoSelectChangeListener(OnPhotoSelectChangeListener listener) {
        this.listener = listener;
    }


    private void createDeleteDialog() {

        AlertDialog builder = new AlertDialog.Builder(getContext())
                .setMessage(String.format("是否删除所选%s个文件", mAdapter.getSelectPhoto()))
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAdapter.delectPhotos();

                        dialog.dismiss();
                        cancelPhotoSelect();
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


    public void noPhotoSelect() {
        tvShare.setAlpha(0.5f);
        tvShare.setClickable(false);

        tvDelete.setAlpha(0.5f);
        tvDelete.setClickable(false);

        tvAllselect.setText("全选");
    }

    public void photoSelect() {
        tvShare.setAlpha(1.0f);
        tvShare.setClickable(true);

        tvDelete.setAlpha(1.0f);
        tvDelete.setClickable(true);

        if (mAdapter.isAllPhotoSelect()) {
            tvAllselect.setText("取消全选");
        } else {
            tvAllselect.setText("全选");
        }
    }

    private void showShare(List<String> imagePath) {
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // 分享时Notification的图标和文字  2.5.9以后的版本不     调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle("照片分享");
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        //oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText("我正在使用蛋蛋拍照");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        for (String s : imagePath) {
            oks.setImagePath(s);//确保SDcard下面存在此张图片
        }

        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://sharesdk.cn");
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        //oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://sharesdk.cn");
        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
            @Override
            public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
                //可以修改分享参数
            }
        });
        oks.setCallback(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                Toast.makeText(getContext(), "分享成功", Toast.LENGTH_LONG);
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                Toast.makeText(getContext(), "分享出错", Toast.LENGTH_LONG);

            }

            @Override
            public void onCancel(Platform platform, int i) {
                Toast.makeText(getContext(), "取消分享", Toast.LENGTH_LONG);

            }
        });
        // 启动分享GUI
        oks.show(getContext());
    }

}
