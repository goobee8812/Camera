package com.magic.photo.photoviewlibrary.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.adapter.FamilyPhotoAdapter;
import com.magic.photo.photoviewlibrary.customView.DividerItemDecoration;
import com.magic.photo.photoviewlibrary.customView.PhotoEditDialog;
import com.magic.photo.photoviewlibrary.entity.CallbackInfo;
import com.magic.photo.photoviewlibrary.entity.FamilyPhotoInfo;
import com.magic.photo.photoviewlibrary.listener.OnDeleteClickListener;
import com.magic.photo.photoviewlibrary.manager.ImageRequest;
import com.magic.photo.photoviewlibrary.manager.UserManager;
import com.magic.photo.photoviewlibrary.utils.DateUtils;
import com.magic.photo.photoviewlibrary.utils.FamilyPhotoComparator;
import com.magic.photo.photoviewlibrary.utils.UIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;


/**
 * description:家庭云相册
 * Created by luohaijun on 2016/10/19.
 */

public class FamilyPhotoActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, OnDeleteClickListener, com.magic.photo.photoviewlibrary.listener.CallbackInfo {

    private TextView tv_publish;
    private SwipeRefreshLayout mRefresh_layout;
    private RecyclerView fp_recyclerView;

    private List<FamilyPhotoInfo> infoList;
    private FamilyPhotoAdapter mPhotoAdapter;

    /**
     * 分页查询
     */
    private String startTime = "2016-10-01 00:00:00";
    private String endTime = "2016-11-03 00:00:00";
    private int pageNumber = 1;//显示页数
    private int pageSize = 10;//一页显示数量
    /**
     * 标记是否正在加载更多，防止再次调用加载更多接口
     */
    private boolean mIsLoadingMore;

    private boolean mIsFooterEnable = false;//是否允许加载更多

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_familyphoto);
        initLayout();
        endTime = DateUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss");
        infoList = new ArrayList<>();
        getPhotoes();
    }

    private void initLayout() {
        ImageView bt_back = (ImageView) findViewById(R.id.bt_back);
        tv_publish = (TextView) findViewById(R.id.tv_publish);
        bt_back.setOnClickListener(this);
        tv_publish.setOnClickListener(this);
        initRecyclerView();
    }

    /**
     * 下拉刷新控件
     */
    private void initRefreshLayout() {
        mRefresh_layout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mRefresh_layout.setColorSchemeResources(R.color.main_color, R.color.main_color, R.color.main_color, R.color.main_color);
        mRefresh_layout.setOnRefreshListener(this);
        mRefresh_layout.setRefreshing(true);
    }

    private void initRecyclerView() {
        initRefreshLayout();
        fp_recyclerView = (RecyclerView) findViewById(R.id.fp_recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        fp_recyclerView.setLayoutManager(manager);
        fp_recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL_LIST, R.drawable.list_item_divider));

        mPhotoAdapter = new FamilyPhotoAdapter(getApplicationContext());
        fp_recyclerView.setAdapter(mPhotoAdapter);
        mPhotoAdapter.setFooterEnable(mIsFooterEnable);
        //监听滚动事件
        fp_recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mIsFooterEnable && !mIsLoadingMore && dy > 0) {
                    int lastVisiblePosition = ((LinearLayoutManager) fp_recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                    if (lastVisiblePosition + 1 == mPhotoAdapter.getItemCount()) {
                        getPhotoes();
                    }
                }
            }
        });
        mPhotoAdapter.setOnDeleteClickListener(this);
    }

    /**
     * 获取图片列表
     */
    private void getPhotoes() {
        String userId = UserManager.getInstance().getUserId();
        new ImageRequest().requestImages(userId, "" + pageNumber, "" + pageSize, startTime, endTime, this);
        pageNumber++;
    }

    private void deleteDialog(final int position) {
        new PhotoEditDialog().deleteDialog(this, new PhotoEditDialog.OnDialogClickListener() {
            @Override
            public void onPositiveClick(Dialog dialog, String text, boolean bool) {
                dialog.dismiss();
                deleteImages(position);
            }
        });
    }

    private void deleteImages(final int position) {
        if (infoList == null || infoList.size() == 0) {
            UIUtils.showToast(getString(R.string.unselect_delete_file));
            return;
        }
        FamilyPhotoInfo familyPhoto = infoList.get(position);
        new ImageRequest().deleteIamges(familyPhoto.getId(), familyPhoto.getImageUrls(), new com.magic.photo.photoviewlibrary.listener.CallbackInfo() {
            @Override
            public void onCallback(CallbackInfo info) {
                if (info.getCode().equals("0") && infoList.size() > position) {
                    infoList.remove(position);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPhotoAdapter.setList(infoList);
                        }
                    });
                }
            }
        });
    }

    /**
     * 数据回调
     *
     * @param info
     */
    @Override
    public void onCallback(CallbackInfo info) {
        if (!info.getCode().equals("0")) {
            mIsLoadingMore = false;
            mIsFooterEnable = false;
            showData();
            return;
        }
        List<FamilyPhotoInfo> tempList = null;
        try {
            TreeMap<String, List<FamilyPhotoInfo>> map = (TreeMap<String, List<FamilyPhotoInfo>>) info.getData();
            tempList = map.get("list");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (tempList != null && tempList.size() > 0) {
            infoList.addAll(tempList);
            Collections.sort(infoList, new FamilyPhotoComparator());
        } else {
            mIsLoadingMore = false;
            mIsFooterEnable = false;
        }
        showData();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.bt_back) {
            finish();
        } else if (i == R.id.tv_publish) {
          //  startActivity(new Intent(this, SharedPhotoActivity.class));
            finish();
        }
    }

    /**
     * 删除
     *
     * @param v
     * @param position
     */
    @Override
    public void onDeleteClick(View v, int position) {
        deleteDialog(position);
    }

    /**
     * 下拉刷新中
     */
    @Override
    public void onRefresh() {
        if (infoList != null) {
            infoList.clear();
        }
        mRefresh_layout.setEnabled(false);
        startTime = "2016-10-01 00:00:00";
        pageNumber = 1;
        pageSize = 10;
        mIsFooterEnable = false;
        getPhotoes();
    }

    private void showData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (infoList.size() == 0) {
                    UIUtils.showToast(getString(R.string.noInfo_hint));
                }
                int lastVisiblePosition = ((LinearLayoutManager) fp_recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                if (infoList.size() <= lastVisiblePosition) {
                    mIsFooterEnable = false;
                }
                mPhotoAdapter.setFooterEnable(mIsLoadingMore);
                mPhotoAdapter.setList(infoList);
                hideRefresh();
            }
        });
    }

    /**
     * 隐藏刷新控件
     */
    public void hideRefresh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRefresh_layout.setRefreshing(false);
                mRefresh_layout.setEnabled(true);
                mIsFooterEnable = true;
                mIsLoadingMore = false;
            }
        });
    }
}
