package com.magic.photo.photoviewlibrary.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.activity.PhotoShowImagesActivity;
 import com.magic.photo.photoviewlibrary.adapter.DataExpandableListAdapter;
import com.magic.photo.photoviewlibrary.adapter.PhotoChildImageAdapter;
import com.magic.photo.photoviewlibrary.customView.PhotoEditDialog;
import com.magic.photo.photoviewlibrary.entity.DateExpandableEntity;
import com.magic.photo.photoviewlibrary.entity.PhotoImageBean;
import com.magic.photo.photoviewlibrary.entity.PhotoInfo;
import com.magic.photo.photoviewlibrary.entity.event.ImageStatusEvent;
import com.magic.photo.photoviewlibrary.entity.event.PhotoOperateEvent;
import com.magic.photo.photoviewlibrary.entity.event.RefreshImageEvent;
import com.magic.photo.photoviewlibrary.utils.DateUtils;
import com.magic.photo.photoviewlibrary.utils.FileUtils;
import com.magic.photo.photoviewlibrary.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * description:
 * Created by luohaijun on 2016/10/9.
 */

public class PhotoChildrenFragment extends BaseFragment {

    private RecyclerView mRecyclerView;
    private ExpandableListView mExpandableListView;

    private PhotoChildImageAdapter adapter;
    private List<PhotoInfo> imgList;

    private DataExpandableListAdapter mExpandableListAdapter;

    private TreeMap<String, List<Boolean>> statusMap;
    private List<PhotoImageBean> groupList;
    private int groupPosition = -1;

    private ProgressDialog mProgressDialog;//加载等待条

    @Override
    protected View initView() {
        View view = LayoutInflater.from(mContext.getApplicationContext()).inflate(R.layout.fragment_photochildren, null);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.mRecyclerView);
        mExpandableListView = (ExpandableListView) view.findViewById(R.id.expand_dataList);
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
        initRecyclerview();
        initExpandableListView();
        getImageData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initRecyclerview() {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity().getApplicationContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.left = 20;
                outRect.bottom = 30;
            }
        });
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new PhotoChildImageAdapter();
        mRecyclerView.setAdapter(adapter);
    }

    /**
     * 日期列表
     */
    private void initExpandableListView() {
        mExpandableListAdapter = new DataExpandableListAdapter(getActivity());
        mExpandableListView.setAdapter(mExpandableListAdapter);
        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                String child = mExpandableListAdapter.getChild(i, i1).toString();
                int position = judgePosition(child);
                if (position >= 0) {
                    mRecyclerView.scrollToPosition(position);
                }
                return false;
            }
        });
    }

    private void getExpandableData(List<String> mList) {
        DateExpandableEntity expandableEntity = FileUtils.getDateList(mList);
        mExpandableListAdapter.setList(expandableEntity.getGroupList(), expandableEntity.getChildList());
    }

    /**
     * 获取图片信息
     */
    private void getImageData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            List<String> mList = bundle.getStringArrayList("childList");
            imgList = FileUtils.getCurrentList(mList);
            groupList = (List<PhotoImageBean>) bundle.getSerializable("folderList");
            getExpandableData(mList);
        }
        if (imgList == null) {
            imgList = new ArrayList<>();
        }
        adapter.setList(imgList);
    }

    /**
     * 全选
     *
     * @param isAll
     */
    @Override
    public void onSelectAll(boolean isAll) {
        super.onSelectAll(isAll);
        adapter.setSelectAll(isAll);
    }

    @Override
    public void onBackPressed(boolean isBack) {
        super.onBackPressed(isBack);
        boolean bool = adapter.getEdit();
        if (bool) {
            adapter.setEdit(false);
        }
    }

    /**
     * 移动
     */
    @Override
    public void onFileRemove() {
        super.onFileRemove();
        List<String> tempList = chargeSelect();
        if (tempList == null || tempList.size() == 0) {
            UIUtils.showToast(getString(R.string.unselect_file));
        } else {
            showRemoveWindow();
        }
    }

    /**
     * 删除
     */
    @Override
    public void onChildFileDelete() {
        super.onChildFileDelete();
        List<String> tempList = chargeSelect();
        if (tempList == null || tempList.size() == 0) {
            UIUtils.showToast(getString(R.string.unselect_file));
        } else {
            PhotoEditDialog photoEditDialog = new PhotoEditDialog();
            photoEditDialog.deleteDialog(getActivity(), new PhotoEditDialog.OnDialogClickListener() {
                @Override
                public void onPositiveClick(Dialog dialog, String text, boolean bool) {
                    dialog.dismiss();
                    deleteFile();
                }
            });
        }
    }

    /**
     * 分享
     */
    @Override
    public void onFileShared() {
        super.onFileShared();
        List<String> tempList = chargeSelect();
        if (tempList == null || tempList.size() == 0) {
            UIUtils.showToast(getString(R.string.unselect_file));
        } else {
//            Intent intent = new Intent(getActivity(), SharedPhotoActivity.class);
//            intent.putStringArrayListExtra("imgList", (ArrayList<String>) tempList);
//            startActivity(intent);
        }
    }

    private void showRemoveWindow() {
        PhotoEditDialog photoEditDialog = new PhotoEditDialog();
        photoEditDialog.movementDialog(getActivity(), new PhotoEditDialog.OnDialogClickListener() {
            @Override
            public void onPositiveClick(Dialog dialog, String text, boolean bool) {
                getTargetPath(dialog, text, bool);
            }
        });
        photoEditDialog.setFolderList(groupList);
    }

    private void deleteFile() {
        //显示进度条
        mProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.delete_now), true, false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> tempList;
                File curFile;
                for (int i = 0; i < imgList.size(); i++) {
                    if (!statusMap.containsKey(i + "")) {
                        continue;
                    }
                    List<Boolean> boolList = statusMap.get(i + "");
                    tempList = imgList.get(i).getUrlList();
                    for (int j = tempList.size() - 1; j >= 0; j--) {
                        if (boolList.get(j)) {
                            curFile = new File(tempList.get(j));
                            //删除文件
                            FileUtils.deleteFile(getActivity().getApplicationContext(), curFile.getAbsolutePath());
                            tempList.remove(j);
                            boolList.remove(j);
                        }
                    }
                }
                SystemClock.sleep(2000);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateDeleteFile();
                    }
                });
            }
        }).start();
    }

    private void updateDeleteFile() {
        dismiss();
        adapter.setSelectStatus(statusMap);
        adapter.notifyDataSetChanged();
        judgeList();
    }

    /**
     *
     */
    private List<String> chargeSelect() {
        List<String> pList = new ArrayList<>();
        statusMap = adapter.getSelectStatus();
        if (statusMap == null) {
            UIUtils.showToast(getString(R.string.unselect_file));
            return pList;
        }
        int count = 0;
        int total = 0;
        List<String> tempList;
        for (int i = 0; i < imgList.size(); i++) {
            if (!statusMap.containsKey(i + "")) {
                continue;
            }
            List<Boolean> boolList = statusMap.get(i + "");
            tempList = imgList.get(i).getUrlList();
            for (int j = 0; j < tempList.size(); j++) {
                if (boolList.get(j)) {
                    pList.add(tempList.get(j));
                    count++;
                }
            }
            total += boolList.size();
        }
        return pList;
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
        List<String> tempList;
        File curFile;
        //显示进度条
        mProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.moving_wait), true, false);
        for (int i = 0; i < imgList.size(); i++) {
            if (!statusMap.containsKey(i + "")) {
                continue;
            }
            List<Boolean> boolList = statusMap.get(i + "");
            tempList = imgList.get(i).getUrlList();
            for (int j = tempList.size() - 1; j >= 0; j--) {
                if (boolList.get(j)) {
                    curFile = new File(tempList.get(j));
                    //复制文件
                    FileUtils.cutFile(getActivity().getApplicationContext(), curFile.getAbsolutePath(), targetPath + "/" + curFile.getName());
                    tempList.remove(j);
                    boolList.remove(j);
                }
            }
        }
        dismiss();
        adapter.notifyDataSetChanged();
    }

    private int judgePosition(String d) {
        if (imgList == null || imgList.size() == 0) {
            return -1;
        }
        for (int i = 0; i < imgList.size(); i++) {
            PhotoInfo info = imgList.get(i);
            String date = DateUtils.getFormaterTime(info.getImgDate(), "yyyyMM");
            if (d.equals(date)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 文件夹选中状态改变
     *
     * @param event TreeMap<String, List<Boolean>>
     */
    @Subscribe
    public void onEventMainThread(ImageStatusEvent event) {
        if (event == null) {
            return;
        }
        boolean edit = adapter.getEdit();
        if (!edit) {
            groupPosition = event.groupPosition;
            intentActivity(event.childPosition, event.groupPosition);
        } else {
            this.statusMap = event.statusMap;
        }
    }

    @Subscribe
    public void onEventMainThread(RefreshImageEvent event) {
        if (event != null && event.toRefresh) {
            if (imgList != null && imgList.size() > groupPosition) {
                PhotoInfo info = imgList.get(groupPosition);
                info.setUrlList(event.pathList);
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            judgeList();
        }
    }

    private void intentActivity(int position, int groupPosition) {
        if (imgList != null && imgList.size() > groupPosition) {
            PhotoInfo photoInfo = imgList.get(groupPosition);
            List<String> tempList = photoInfo.getUrlList();
            //跳转浏览图片页面
            Intent intent = new Intent(getContext(), PhotoShowImagesActivity.class);
            intent.putStringArrayListExtra("child_list", (ArrayList<String>) tempList);
            intent.putExtra("img_position", position);
            intent.putExtra("showBottomLayout", true);
            startActivity(intent);
        }
    }

    private void judgeList() {
        if (imgList != null) {
            imgList = FileUtils.judgeNullList(imgList);
        }
        if (imgList.size() == 0) {
//            Intent intent = new Intent(getActivity(), PhotoMainAvtivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
            PhotoOperateEvent event = new PhotoOperateEvent(0, 22, 2, false);
            EventBus.getDefault().post(event);
            getActivity().getSupportFragmentManager().popBackStackImmediate(null, 0);
        }
    }

    private void dismiss() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }
}
