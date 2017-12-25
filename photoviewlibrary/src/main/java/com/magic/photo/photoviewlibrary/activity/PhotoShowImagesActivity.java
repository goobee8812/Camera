package com.magic.photo.photoviewlibrary.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.adapter.PhotoImagePagerAdapter;
import com.magic.photo.photoviewlibrary.customView.PhotoEditDialog;
import com.magic.photo.photoviewlibrary.customView.PhotoMainBottomView;
import com.magic.photo.photoviewlibrary.entity.PhotoImageBean;
import com.magic.photo.photoviewlibrary.entity.event.ImageListEvent;
import com.magic.photo.photoviewlibrary.entity.event.RefreshImageEvent;
import com.magic.photo.photoviewlibrary.listener.OnEditClickListener;
import com.magic.photo.photoviewlibrary.manager.FileManager;
import com.magic.photo.photoviewlibrary.utils.FileUtils;
import com.magic.photo.photoviewlibrary.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 利用 Matrix 进行图片放大拖拉缩放
 *
 * @author Aslan
 */
public class PhotoShowImagesActivity extends AppCompatActivity implements PhotoImagePagerAdapter.OnItemClickListener {

    private PhotoMainBottomView bottomLayout;

    private List<String> imgList;//图片list
    private ViewPager mPager;
    private PhotoImagePagerAdapter adapter;
    private int viewPosition;

    private boolean isEdit;
    private boolean showBottomLayout;
    private int selectPosition = -1;

    private ProgressDialog mProgressDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_activity_showimage);
        EventBus.getDefault().register(this);
        initViewPager();
        initBottomLayout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        imgList = null;
    }

    private void initViewPager() {
        mPager = (ViewPager) this.findViewById(R.id.mPager);
        Bundle bundle = getIntent().getExtras();
        imgList = new ArrayList<>();
        if (bundle != null) {
            imgList = bundle.getStringArrayList("child_list");
            viewPosition = bundle.getInt("img_position");
            showBottomLayout = bundle.getBoolean("showBottomLayout");
        }
        showData();
    }

    private void showData() {
        adapter = new PhotoImagePagerAdapter(getApplicationContext());
        adapter.setOnItemClickListener(this);
        adapter.setData(imgList);
        mPager.setAdapter(adapter);
        mPager.setCurrentItem(viewPosition, true);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initBottomLayout() {
        bottomLayout = (PhotoMainBottomView) findViewById(R.id.bottomLayout);
        bottomLayout.setOnEidtClickListener(new OnEditClickListener() {
            @Override
            public void onEditClick(View v) {
                int i = v.getId();
                if (i == R.id.layout_remove) {
                    getImgFolderList();

                } else if (i == R.id.layout_delete2) {
                    deleteDialog();

                } else if (i == R.id.layout_shared) {
                   /* if (selectPosition != -1 && imgList.size() > selectPosition) {
                        List<String> tempList = new ArrayList<>();
                        tempList.add(imgList.get(selectPosition));
                        Intent intent = new Intent(PhotoShowImagesActivity.this, SharedPhotoActivity.class);
                        intent.putStringArrayListExtra("imgList", (ArrayList<String>) tempList);
                        startActivity(intent);
                        finish();
                    }*/
                }
            }
        });
    }

    private void deleteDialog() {
        PhotoEditDialog photoEditDialog = new PhotoEditDialog();
        photoEditDialog.deleteDialog(this, new PhotoEditDialog.OnDialogClickListener() {
            @Override
            public void onPositiveClick(Dialog dialog, String text, boolean bool) {
                dialog.dismiss();
                deleteFile();
            }
        });
    }

    private void deleteFile() {
        File curFile = new File(imgList.get(selectPosition));
        if (curFile.exists()) {
            //删除文件
            mProgressDialog = ProgressDialog.show(this, "", getString(R.string.delete_now), true, false);
            boolean bool = FileUtils.deleteFile(getApplicationContext(), curFile.getAbsolutePath());
            mProgressDialog.dismiss();
        }
        imgList.remove(selectPosition);
        int size = imgList.size();
        if (size > 0) {
            adapter.setData(imgList);
            mPager.setAdapter(adapter);
            mPager.setCurrentItem(selectPosition, true);
            EventBus.getDefault().post(new RefreshImageEvent(1, true, imgList));
            if (selectPosition >= size) {
                selectPosition = size - 1;
            }
        } else {
            EventBus.getDefault().post(new RefreshImageEvent(1, true, imgList));
            finish();
        }
    }

    private void getImgFolderList() {
        //显示进度条
        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.please_wait), true, true);
        FileManager.getInstance().getLocalImages(UIUtils.getContext());
    }

    private List<PhotoImageBean> groupList;

    @Subscribe
    public void onEventMainThread(ImageListEvent event) {
        dismiss();
        if (imgList == null||imgList.size()==0) {
            return;
        }
        int size = imgList.size();
        if (size > 0 && size <= selectPosition) {
            selectPosition = size - 1;
        }
        HashMap<String, ArrayList<String>> mGroupMap = event.mGroupMap;
        groupList = FileManager.getInstance().getFolderList(mGroupMap, imgList.get(selectPosition));
        if (groupList == null) {
            groupList = new ArrayList<>();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showRemoveWindow();
            }
        });
    }

    private void showRemoveWindow() {
        PhotoEditDialog photoEditDialog = new PhotoEditDialog();
        photoEditDialog.movementDialog(this, new PhotoEditDialog.OnDialogClickListener() {
            @Override
            public void onPositiveClick(Dialog dialog, String text, boolean bool) {
                getTargetPath(dialog, text, bool);
            }
        });
        photoEditDialog.setFolderList(groupList);
    }

    /**
     * 获取移动的路径
     *
     * @param text
     * @param bool
     */
    private void getTargetPath(Dialog dialog, String text, boolean bool) {
        File file;
        String tempPath = "";
        if (!bool) {
            if (TextUtils.isEmpty(text)) {
                UIUtils.showToast(getString(R.string.input_right_name));
                return;
            }
            String parentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            file = new File(parentPath + "/" + text);
            file.mkdirs();
            tempPath = file.getAbsolutePath();
        } else {
            file = new File(text);
            if (file.exists()) {
                tempPath = file.getParent();
            }
        }
        dialog.dismiss();
        removeFile(tempPath);
    }

    /**
     * 移动文件
     *
     * @param targetPath
     */
    private void removeFile(String targetPath) {
        //显示进度条
        mProgressDialog = ProgressDialog.show(this, "", getString(R.string.moving_wait), true, false);
        File curFile = new File(imgList.get(selectPosition));
        //复制文件
        FileUtils.cutFile(getApplicationContext(), curFile.getAbsolutePath(), targetPath + "/" + curFile.getName());
        dismiss();
        imgList.remove(selectPosition);
        adapter.setData(imgList);
        mPager.setAdapter(adapter);
        EventBus.getDefault().post(new RefreshImageEvent(1, true, imgList));
        if (imgList.size() == 0) {
            finish();
        }
    }


    @Override
    public void onItemClick(View view, int position) {
        if (showBottomLayout) {
            bottomLayout.setType(isEdit ? 0 : 1);
            bottomLayout.setRemoveEnabled(true);
            bottomLayout.setSharedEnabled(true);
            isEdit = !isEdit;
            selectPosition = position;
        } else {
            finish();
        }
    }

    private void dismiss() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
