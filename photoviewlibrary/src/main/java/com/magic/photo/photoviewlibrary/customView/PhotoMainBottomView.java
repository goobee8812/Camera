package com.magic.photo.photoviewlibrary.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.listener.OnEditClickListener;

/**
 * description:
 * Created by luohaijun on 2016/10/8.
 */

public class PhotoMainBottomView extends LinearLayout implements View.OnClickListener {

    private Context mContext;
    private View bottomLayout1, bottomLayout2;
    private LinearLayout layout_rename, layout_delete, layout_remove, layout_shared, layout_delete2;
    private TextView tv_rename, tv_shared, tv_remove;
    private ImageView iv_rename, iv_shared, iv_remove;
    private int type;//

    private OnEditClickListener listener;

    public PhotoMainBottomView(Context context) {
        super(context);
        init(context);
    }

    public PhotoMainBottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * 初始化控件
     *
     * @param context
     */
    private void init(Context context) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.photo_bottom, this);
        initBottomLayout1();
        initBottomLayout2();
    }

    private void initBottomLayout1() {
        bottomLayout1 = findViewById(R.id.bottomLayout1);
        layout_rename = (LinearLayout) findViewById(R.id.layout_rename);
        layout_delete = (LinearLayout) findViewById(R.id.layout_delete);
        tv_rename = (TextView) findViewById(R.id.tv_rename);
        iv_rename = (ImageView) findViewById(R.id.iv_rename);
        layout_rename.setOnClickListener(this);
        layout_delete.setOnClickListener(this);

    }

    private void initBottomLayout2() {
        bottomLayout2 = findViewById(R.id.bottomLayout2);
        layout_remove = (LinearLayout) findViewById(R.id.layout_remove);
        layout_shared = (LinearLayout) findViewById(R.id.layout_shared);
        layout_delete2 = (LinearLayout) findViewById(R.id.layout_delete2);
        iv_remove = (ImageView) findViewById(R.id.iv_remove);
        tv_remove = (TextView) findViewById(R.id.tv_remove);
        iv_shared = (ImageView) findViewById(R.id.iv_shared);
        tv_shared = (TextView) findViewById(R.id.tv_shared);
        layout_remove.setOnClickListener(this);
        layout_shared.setOnClickListener(this);
        layout_delete2.setOnClickListener(this);
    }

    /**
     * @param type 1、重命名 删除 2、移动 分享 删除
     */
    public void setType(int type) {
        this.type = type;
        if (type == 2) {
            bottomLayout1.setVisibility(View.VISIBLE);
            bottomLayout2.setVisibility(View.GONE);
            setRenameEnabled(false);
        } else if (type == 1) {
            bottomLayout1.setVisibility(View.GONE);
            bottomLayout2.setVisibility(View.VISIBLE);
        } else {
            bottomLayout1.setVisibility(View.GONE);
            bottomLayout2.setVisibility(View.GONE);
        }
    }

    /**
     * 设置重命名控件是否可点击
     */
    public void setRenameEnabled(boolean bool) {
        layout_rename.setEnabled(bool);
        tv_rename.setTextColor(bool ? mContext.getResources().getColor(R.color.main_color) : mContext.getResources().getColor(R.color.light_gray));
        iv_rename.setImageResource(bool ? R.drawable.rename : R.drawable.rename2);
    }

    /**
     * 设置移动到控件是否可点击
     *
     * @param bool
     */
    public void setRemoveEnabled(boolean bool) {
        layout_remove.setEnabled(bool);
        tv_remove.setTextColor(bool ? mContext.getResources().getColor(R.color.main_color) : mContext.getResources().getColor(R.color.light_gray));
        iv_remove.setImageResource(bool ? R.drawable.move1 : R.drawable.move2);
    }

    /**
     * 设置分享控件是否可点击
     */
    public void setSharedEnabled(boolean bool) {
        layout_shared.setEnabled(bool);
        tv_shared.setTextColor(bool ? mContext.getResources().getColor(R.color.main_color) : mContext.getResources().getColor(R.color.light_gray));
        iv_shared.setImageResource(bool ? R.drawable.share : R.drawable.share2);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onEditClick(v);
        }
    }

    public void setOnEidtClickListener(OnEditClickListener listener) {
        this.listener = listener;
    }
}
