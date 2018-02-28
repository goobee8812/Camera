package com.magic.photo.photoviewlibrary.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.adapter.CheckImageAdapter;
import com.magic.photo.photoviewlibrary.entity.Image;
import com.magic.photo.photoviewlibrary.manager.FileManager;
import com.magic.photo.photoviewlibrary.utils.FileUtils;

import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import me.yokeyword.fragmentation.SupportActivity;


public class CheckImageActivity extends SupportActivity implements View.OnClickListener, CheckImageAdapter.OnItemClickListener {
    private static final String TAG = "CheckImageActivity";
    private FrameLayout mTopLayout;
    private ImageView mBack;
    private ImageView mDelete;
    //    private ImageView mShare;
    private ViewPager mPager;
    private Dialog mDialogDelete;
    private int mPosition;
    private static final String KEY_DATA = "data";
    private List<Image> mImages;
    private CheckImageAdapter mAdapter;
    private boolean isShow = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_image);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        mBack.setOnClickListener(this);
        mDelete.setOnClickListener(this);
//        mShare.setOnClickListener(this);
        mAdapter.setOnItemClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        hideController();
    }

    private void initData() {
        long id = getIntent().getLongExtra(KEY_DATA, 0);
        mImages = FileManager.getInstance().getImageFormCamera(this);
        Log.d(TAG, "initData: " + mImages.toString());
        for (int i = 0; i < mImages.size(); i++) {
            Image image = mImages.get(i);
            if (image.id == id) {
                mPosition = i;
                break;
            }
        }
        mAdapter = new CheckImageAdapter(mImages, this);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(mPosition);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    hideController();
                }
            }
        });
    }

    private void initView() {
        mTopLayout = (FrameLayout) findViewById(R.id.framelayout_top);
        mBack = (ImageView) findViewById(R.id.imageView_back);
//        mShare = (ImageView) findViewById(R.id.imageView_share);
        mDelete = (ImageView) findViewById(R.id.imageView_delete);
        mPager = (ViewPager) findViewById(R.id.viewpager);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.imageView_back) {
            finish();
        } else if (id == R.id.imageView_delete) {
            if (mDialogDelete != null) {
                mDialogDelete.dismiss();
            }
            mDialogDelete = createDeleteDialog();
            mDialogDelete.show();
        } else if (id == R.id.cancel) {
            if (mDialogDelete != null) {
                mDialogDelete.dismiss();
            }
        } else if (id == R.id.imageView_share) {
            showShare(mImages.get(mPosition).path);
            mAdapter.notifyDataSetChanged();
            if (mImages.size() == 1) {  //在mImages.size()==1的时候，无法执行onPageChangedListener()：posistion无法赋值
                mPosition = 0;
            }
        } else if (id == R.id.confirm) {
            boolean deleteFile = FileUtils.deleteFile(this, mImages.get(mPosition).path);
            if (deleteFile) {
                mImages.remove(mPosition);
                mPager.setAdapter(mAdapter);
                mDialogDelete.dismiss();
                hideController();
                if (hasNext()) {
                    mPager.setCurrentItem(mPosition);
                } else if (hasPre()) {
                    mPager.setCurrentItem(mPosition - 1);
                } else if (mImages.size() == 0) {
                    finish();
                }
            }
            if (mImages.size() == 1) {
                mPosition = 0;
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private Dialog createDeleteDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_photo_delete);
        dialog.findViewById(R.id.cancel).setOnClickListener(this);
        dialog.findViewById(R.id.confirm).setOnClickListener(this);
        return dialog;
    }

    @Override
    public void onItemClick(View view, int position) {
        mTopLayout.setVisibility(View.VISIBLE);
        if (!isShow) {
            showController();
        } else {
            hideController();
        }
        isShow = !isShow;
    }

    private void showController() {
        mTopLayout.animate().translationY(0).setDuration(200).start();
    }

    private void hideController() {
        mTopLayout.animate().translationY(dip2px(this, -80)).setDuration(200).start();
    }

    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale);
    }

    public boolean hasNext() {
        if (mImages.isEmpty()) {
            return false;
        }
        if (mPosition >= mImages.size()) {
            return false;
        }
        return true;
    }

    public boolean hasPre() {
        if (mImages.isEmpty()) {
            return false;
        }
        if (mPosition - 1 < 0) {
            return false;
        }
        return true;
    }

    private void showShare(String imagePath) {
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
        oks.setImagePath(imagePath);//确保SDcard下面存在此张图片
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
                Toast.makeText(CheckImageActivity.this, "分享成功", Toast.LENGTH_LONG);
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                Toast.makeText(CheckImageActivity.this, "分享出错", Toast.LENGTH_LONG);

            }

            @Override
            public void onCancel(Platform platform, int i) {
                Toast.makeText(CheckImageActivity.this, "取消分享", Toast.LENGTH_LONG);

            }
        });
        // 启动分享GUI
        oks.show(this);
    }
}
