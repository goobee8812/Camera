package com.magic.photo.photoviewlibrary.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.customView.PhotoMainBottomView;
import com.magic.photo.photoviewlibrary.customView.PhotoMainTitleView;
import com.magic.photo.photoviewlibrary.entity.event.ImageStatusEvent;
import com.magic.photo.photoviewlibrary.entity.event.PhotoOperateEvent;
import com.magic.photo.photoviewlibrary.entity.event.PhotoSelectStatusEvent;
import com.magic.photo.photoviewlibrary.fragments.BaseFragment;
import com.magic.photo.photoviewlibrary.fragments.PhotoIndexFragment;
import com.magic.photo.photoviewlibrary.listener.OnEditClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.TreeMap;


/**
 * description:
 * Created by luohaijun on 2016/9/27.
 */

public class PhotoMainAvtivity extends AppCompatActivity implements OnEditClickListener {

    private PhotoMainTitleView titleLayout;//标题栏
    private PhotoMainBottomView mBottomView;

    /**
     * 编辑
     */
    private boolean isEdit = false;

    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photomain);
        EventBus.getDefault().register(this);
        titleLayout = (PhotoMainTitleView) findViewById(R.id.titleLayout);
        titleLayout.setOnEidtClickListener(this);
        initBottomLayout();
        intentFragment(new PhotoIndexFragment());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initBottomLayout() {
        mBottomView = (PhotoMainBottomView) findViewById(R.id.bottomLayout);
        mBottomView.setOnEidtClickListener(this);
    }

    @Subscribe
    public void onEventMainThread(final PhotoOperateEvent event) {
        if (event == null) {
            return;
        }
        isEdit = event.isShow;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                titleLayout.canEdit(isEdit);
                mBottomView.setType(event.type);
            }
        });
    }

    /**
     * 文件夹选中状态改变
     *
     * @param event
     */
    @Subscribe
    public void onEventMainThread(PhotoSelectStatusEvent event) {
        if (event == null || !isEdit) {
            return;
        }
        List<Boolean> statusList = event.statusList;
        if (statusList == null) {
            return;
        }
        int count = 0;
        for (int i = 0; i < statusList.size(); i++) {
            if (statusList.get(i)) {
                count++;
            }
        }
        if (count == 0 || count > 1) {
            mBottomView.setRenameEnabled(false);
        } else {
            mBottomView.setRenameEnabled(true);
        }
        //更新全选按钮
        if (count > 0 && count == statusList.size()) {
            titleLayout.updateSelectAll(false);
        } else {
            titleLayout.updateSelectAll(true);
        }
    }

    /**
     * 子相册选中状态
     *
     * @param event
     */
    @Subscribe
    public void onEventMainThread(ImageStatusEvent event) {
        if (event == null || !event.canEdit) {
            return;
        }
        TreeMap<String, List<Boolean>> statusMap = event.statusMap;
        if (statusMap == null || statusMap.isEmpty()) {
            return;
        }
        List<Boolean> statusList;
        int count = 0;
        int total = 0;
        for (int i = 0; i < statusMap.size(); i++) {
            statusList = statusMap.get(i + "");
            for (int j = 0; j < statusList.size(); j++) {
                if (statusList.get(j)) {
                    count++;
                }
            }
            total += statusList.size();
        }
        if (count == total && total != 0) {//全部选中
            titleLayout.updateSelectAll(false);
        } else {
            titleLayout.updateSelectAll(true);
        }

        if (count == 0 || count > 9) {
            mBottomView.setSharedEnabled(false);
        } else {
            mBottomView.setSharedEnabled(true);
        }

        mBottomView.setRemoveEnabled(count == 0 ? false : true);
    }

    @Override
    public void onEditClick(View v) {
        int i = v.getId();
        if (i == R.id.bt_back) {
            boolean bool = fm.popBackStackImmediate();
            if (bool == false) {
                finish();
            }

        } else if (i == R.id.bt_cancel) {
            if (isEdit) {
                titleLayout.canEdit(false);
                mBottomView.setType(0);
                Fragment fg = fm.findFragmentById(R.id.fl_fragment);
                if (fg != null && fg instanceof BaseFragment) {
                    ((BaseFragment) fg).onBackPressed(isEdit);
                }
                isEdit = false;
            }

        } else if (i == R.id.bt_add) {
        } else if (i == R.id.bt_selectAll) {
            Fragment fg = fm.findFragmentById(R.id.fl_fragment);
            boolean boolSelect = titleLayout.getSelectAllStatus();
            if (fg != null && fg instanceof BaseFragment) {
                ((BaseFragment) fg).onSelectAll(boolSelect);
            }
            mBottomView.setRemoveEnabled(boolSelect);
            mBottomView.setSharedEnabled(false);

        } else if (i == R.id.layout_rename) {
            Fragment renameFragment = fm.findFragmentById(R.id.fl_fragment);
            if (renameFragment != null && renameFragment instanceof BaseFragment) {
                ((BaseFragment) renameFragment).onFileRename();
            }

        } else if (i == R.id.layout_delete) {
            Fragment delFragment = fm.findFragmentById(R.id.fl_fragment);
            if (delFragment != null && delFragment instanceof BaseFragment) {
                ((BaseFragment) delFragment).onFileDelete();
            }

        } else if (i == R.id.layout_remove) {
            Fragment moveFragment = fm.findFragmentById(R.id.fl_fragment);
            if (moveFragment != null && moveFragment instanceof BaseFragment) {
                ((BaseFragment) moveFragment).onFileRemove();
            }

        } else if (i == R.id.layout_delete2) {
            Fragment delFragment2 = fm.findFragmentById(R.id.fl_fragment);
            if (delFragment2 != null && delFragment2 instanceof BaseFragment) {
                ((BaseFragment) delFragment2).onChildFileDelete();
            }

        } else if (i == R.id.layout_shared) {
            Fragment sharedFragment = fm.findFragmentById(R.id.fl_fragment);
            if (sharedFragment != null && sharedFragment instanceof BaseFragment) {
                ((BaseFragment) sharedFragment).onFileShared();
            }
        }
    }

    /**
     * fragment跳转
     */
    public void intentFragment(Fragment fragment) {
        fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.replace(R.id.fl_fragment, fragment);
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if (isEdit) {
            titleLayout.canEdit(false);
            mBottomView.setType(0);
            Fragment fg = fm.findFragmentById(R.id.fl_fragment);
            if (fg != null && fg instanceof BaseFragment) {
                ((BaseFragment) fg).onBackPressed(isEdit);
            }
            isEdit = false;
        } else {
            super.onBackPressed();
        }
    }
}
