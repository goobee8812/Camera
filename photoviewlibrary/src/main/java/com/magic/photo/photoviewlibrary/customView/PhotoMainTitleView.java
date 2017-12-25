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
 * Created by luohaijun on 2016/9/27.
 */

public class PhotoMainTitleView extends LinearLayout implements View.OnClickListener {

    private TextView bt_cancel, bt_selectAll;
    private ImageView bt_back, bt_add;

    private boolean isEdit, selectAll;

    private OnEditClickListener listener;

    public PhotoMainTitleView(Context context) {
        super(context);
        init(context);
    }

    public PhotoMainTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.photo_main_title, this);
        bt_back = (ImageView) findViewById(R.id.bt_back);
        bt_cancel = (TextView) findViewById(R.id.bt_cancel);
        bt_add = (ImageView) findViewById(R.id.bt_add);
        bt_selectAll = (TextView) findViewById(R.id.bt_selectAll);
        bt_back.setOnClickListener(this);
        bt_cancel.setOnClickListener(this);
        bt_add.setOnClickListener(this);
        bt_selectAll.setOnClickListener(this);
    }

    /**
     * 设置是否可编辑
     *
     * @param isEdit
     */
    public void canEdit(boolean isEdit) {
        this.isEdit = isEdit;
        selectAll(false);
        visibleButton();
    }

    public void selectAll(boolean bool) {
        this.selectAll = bool;
        bt_selectAll.setText(selectAll ? getResources().getString(R.string.button_cancel_select_all) : getResources().getString(R.string.button_select_all));
    }

    public boolean getSelectAllStatus() {

        return selectAll;
    }

    /**
     * 设置控件的显示
     */
    private void visibleButton() {
        if (isEdit) {
            bt_cancel.setVisibility(View.VISIBLE);
            bt_selectAll.setVisibility(View.VISIBLE);
            bt_back.setVisibility(View.GONE);
//            bt_add.setVisibility(View.GONE);
        } else {
            bt_cancel.setVisibility(View.GONE);
            bt_selectAll.setVisibility(View.GONE);
            bt_back.setVisibility(View.VISIBLE);
//            bt_add.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.bt_selectAll) {
            updateSelectAll(selectAll);
        }
        if (listener != null) {
            listener.onEditClick(v);
        }

    }

    /**
     * 更新全选按钮
     */
    public void updateSelectAll(boolean isAll) {
        if (!isAll) {
            bt_selectAll.setText(getResources().getString(R.string.button_cancel_select_all));
            selectAll = true;
        } else {
            bt_selectAll.setText(getResources().getString(R.string.button_select_all));
            selectAll = false;
        }
    }

    public void setOnEidtClickListener(OnEditClickListener listener) {
        this.listener = listener;
    }
}
