package com.magic.photo.photoviewlibrary.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.activity.VideoListActivity;
import com.magic.photo.photoviewlibrary.adapter.PhotoGroupAdapter;
import com.magic.photo.photoviewlibrary.customView.PhotoEditDialog;
import com.magic.photo.photoviewlibrary.entity.PhotoImageBean;
import com.magic.photo.photoviewlibrary.entity.event.ImageListEvent;
import com.magic.photo.photoviewlibrary.entity.event.PhotoOperateEvent;
import com.magic.photo.photoviewlibrary.listener.OnItemClickListener;
import com.magic.photo.photoviewlibrary.manager.FileManager;
import com.magic.photo.photoviewlibrary.utils.FileUtils;
import com.magic.photo.photoviewlibrary.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



/**
 * description:
 * Created by luohaijun on 2016/9/28.
 */

public class PhotoIndexFragment extends BaseFragment implements OnItemClickListener {

    private RecyclerView mRecyclerView;
    private TextView tv_cloudphoto;

    private GridLayoutManager manager;
    private HashMap<String, ArrayList<String>> mGroupMap = null;
    private List<PhotoImageBean> groupList;
    private PhotoGroupAdapter adapter;

    private ProgressDialog mProgressDialog;//加载等待条

    private String clickPath = "";

    @Override
    protected View initView() {
        View view = LayoutInflater.from(mContext.getApplicationContext()).inflate(R.layout.fragment_photoindex, null);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.imgRecyclerView);
        tv_cloudphoto = (TextView) view.findViewById(R.id.tv_cloudphoto);
        return view;
    }

    @Override
    protected void init() {
        super.init();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initData() {
        super.initData();
        initImageList();
        tv_cloudphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), VideoListActivity.class));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        initImages();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 初始化图片列表控件
     */
    private void initImageList() {
        int spanCount = 3;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = 3;
        } else {
            spanCount = 2;
        }
        manager = new GridLayoutManager(mContext.getApplicationContext(), spanCount);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.left = 60;
                outRect.bottom = 20;
            }
        });
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new PhotoGroupAdapter(mContext.getApplicationContext());
        mRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    private void initImages() {
        //显示进度条
        mProgressDialog = ProgressDialog.show(getActivity(), null, getString(R.string.loading));
        FileManager.getInstance().getLocalImages(mContext.getApplicationContext());
    }

    @Subscribe
    public void onEventMainThread(ImageListEvent event) {
        dismiss();
        mGroupMap = event.mGroupMap;
        groupList = FileManager.getInstance().subGroupOfImage(mGroupMap);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (groupList != null && adapter != null) {
                    adapter.setData(groupList);
                }
            }
        });
    }

    /**
     * 是否全选
     *
     * @param isAll
     */
    @Override
    public void onSelectAll(boolean isAll) {
        super.onSelectAll(isAll);
        adapter.setSelectAll(isAll);
    }

    /**
     * 删除
     */
    @Override
    public void onFileDelete() {
        super.onFileDelete();
        List<Boolean> booleanList = adapter.getStatusList();
        if (booleanList == null || groupList == null || booleanList.size() == 0) {
            UIUtils.showToast(getString(R.string.no_delete_file));
            return;
        }
        int count = 0;
        for (int i = 0; i < booleanList.size(); i++) {
            if (booleanList.get(i)) {
                count++;
            }
        }
        if (count == 0) {
            UIUtils.showToast(getString(R.string.unselect_file));
            return;
        }
        deleteFileDialog();
    }

    /**
     * 重命名
     */
    @Override
    public void onFileRename() {
        super.onFileRename();
        rename();
    }

    /**
     * 删除文件提示框
     */
    private void deleteFileDialog() {
        AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
        ab.setTitle(getString(R.string.delete_operate));
        ab.setMessage(getString(R.string.sure_delete));
        ab.setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteFile();
            }
        });
        ab.setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        ab.create().show();
    }

    /**
     *
     */
    private void rename() {
        final List<Boolean> booleanList = adapter.getStatusList();
        if (booleanList == null || groupList == null || booleanList.size() != groupList.size()) {
            return;
        }
        String name = "";
        for (int i = 0; i < booleanList.size(); i++) {
            if (booleanList.get(i)) {
                clickPath = groupList.get(i).getTopImagePath();
                name = groupList.get(i).getFolderName();
                break;
            }
        }
        if (TextUtils.isEmpty(name)) {
            UIUtils.showToast(getString(R.string.unselect_file));
            return;
        }
        new PhotoEditDialog().renameDialog(getActivity(), name, new PhotoEditDialog.OnDialogClickListener() {
            @Override
            public void onPositiveClick(Dialog dialog, final String text, boolean bool) {
                if (TextUtils.isEmpty(text)) {
                    UIUtils.showToast(getString(R.string.input_right_name));
                    return;
                }
                if (clickPath != null && clickPath.contains("/")) {
                    clickPath = clickPath.substring(0, clickPath.lastIndexOf("/"));
                }
                dialog.dismiss();
                mProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.please_wait), true, false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean bool = FileUtils.renameFile(clickPath, text);
                        if (!bool) {
                            dismiss();
                            return;
                        }
                        EventBus.getDefault().post(new PhotoOperateEvent(0, 22, 2, false));
                        SystemClock.sleep(3000);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismiss();
                                if (adapter != null) {
                                    adapter.setNoEdit(false);
                                    initImages();
                                }
                            }
                        });
                    }
                }).start();
            }
        });
    }


    List<Boolean> selectStatus = null;

    /**
     * 删除文件
     */
    private void deleteFile() {
        if (adapter != null) {
            selectStatus = adapter.getStatusList();
        }
        if (selectStatus == null || groupList == null || selectStatus.size() != groupList.size()) {
            UIUtils.showToast(getString(R.string.no_delete_file));
            return;
        }
        //显示进度条
        mProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.delete_now), true, false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                PhotoImageBean imageBean;
                File file;
                boolean isSelect;
                for (int i = groupList.size() - 1; i >= 0; i--) {
                    isSelect = selectStatus.get(i);
                    imageBean = groupList.get(i);
                    String path = imageBean.getTopImagePath();
                    file = new File(path.substring(0, path.lastIndexOf("/")));
                    if (isSelect) {
                        FileUtils.RecursionDeleteFile(file, getActivity().getApplicationContext());
                        selectStatus.remove(i);
                        groupList.remove(i);
                    }
                }
                SystemClock.sleep(2000);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        adapter.setStatusList(selectStatus);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private void dismiss() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        if (groupList == null || mGroupMap == null) {
            return;
        }
        String parentName = groupList.get(position).getFolderName();
        List<PhotoImageBean> tempList = groupList;
        tempList.remove(position);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("childList", mGroupMap.get(parentName));
        bundle.putSerializable("folderList", (Serializable) tempList);
        PhotoChildrenFragment childrenFragment = new PhotoChildrenFragment();
        childrenFragment.setArguments(bundle);
        intentFragment(childrenFragment);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        longClickEvent(position);
        if (adapter != null) {
            adapter.setEdit(true);
        }
    }

    @Override
    public void onBackPressed(boolean isBack) {
        super.onBackPressed(isBack);
        if (isBack) {
            if (adapter != null) {
                adapter.setEdit(false);
            }
        }
    }

    /**
     *
     */
    private void longClickEvent(int position) {
        PhotoOperateEvent event = new PhotoOperateEvent(2, groupList.size(), 2, true);
        EventBus.getDefault().post(event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            manager.setSpanCount(3);
        } else {
            manager.setSpanCount(2);
        }
    }
}
