package com.magic.photo.photoviewlibrary.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

 import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.listener.PhotoEditListener;

public abstract class BaseFragment extends Fragment implements PhotoEditListener {
    public Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return initView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initData();
        initListener();
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * @des 初始化
     * @call 子类可以选择性的覆写该方法
     */
    protected void init() {

    }

    /**
     * fragment跳转
     */
    public void intentFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.replace(R.id.fl_fragment, fragment);
        transaction.addToBackStack(fragment.getClass().getName());
        transaction.commitAllowingStateLoss();
    }

    /**
     * @des 初始化视图
     * @call 必须实现, 基类不知道具体实现, 定义成为抽象方法, 交给子类具体实现
     */
    protected abstract View initView();

    /**
     * @des 初始化数据
     * @call 子类可以选择性的覆写该方法
     */
    protected void initData() {

    }

    /**
     * @des 初始化监听 子类可以选择性的覆写该方法
     */
    protected void initListener() {

    }

    public void onBackPressed(boolean isBack) {

    }

    /**
     * 监听返回键 0:执行返回键操作，1：不作处理
     */
    public int onKeyDown(int keyCode, KeyEvent event) {

        return 0;
    }

    @Override
    public void onSelectAll(boolean isAll) {

    }

    @Override
    public void onFileDelete() {

    }

    @Override
    public void onChildFileDelete() {

    }

    @Override
    public void onFileRename() {

    }

    @Override
    public void onFileRemove() {

    }

    @Override
    public void onFileShared() {

    }


    @Override
    public void onResume() {
        super.onResume();
     }

    @Override
    public void onPause() {
        super.onPause();
     }
}
