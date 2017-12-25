package com.magic.photo.photoviewlibrary.customView;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.adapter.PhotoFolderAdapter;
import com.magic.photo.photoviewlibrary.entity.PhotoImageBean;
import com.magic.photo.photoviewlibrary.listener.OnItemClickListener;

import java.util.List;

/**
 * description:
 * Created by luohaijun on 2016/10/9.
 */

public class PhotoEditDialog {

    private AlertDialog.Builder ab = null;

    private EditText mEditText;

    private PhotoFolderAdapter mFolderAdapter;

    private String path = "";
    private boolean itemSelect = false;

    public void renameDialog(Context context, String name, final OnDialogClickListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_photo_rename, null);
        ab = new AlertDialog.Builder(context);
        ab.setView(view);
        ab.setPositiveButton(context.getString(R.string.button_confirm), null);
        ab.setNegativeButton(context.getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        final AlertDialog dialog = ab.create();
        dialog.show();
        Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        button.setTextColor(context.getResources().getColor(R.color.main_color));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.gray));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEditText.getText().toString();
                if (listener != null) {
                    listener.onPositiveClick(dialog, text, false);
                }
            }
        });
        initRenameLayout(view, name);
    }

    /**
     * 删除提示窗口
     *
     * @param context
     * @param listener
     */
    public void deleteDialog(Context context, final OnDialogClickListener listener) {
        ab = new AlertDialog.Builder(context);
        ab.setTitle(context.getString(R.string.sure_delete));
        ab.setPositiveButton(context.getString(R.string.button_confirm), null);
        ab.setNegativeButton(context.getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        final AlertDialog dialog = ab.create();
        dialog.show();
        Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        button.setTextColor(context.getResources().getColor(R.color.main_color));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.gray));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onPositiveClick(dialog, "", false);
                }
            }
        });
    }

    /**
     * 移动窗口
     *
     * @param context
     * @param listener
     */
    public void movementDialog(Context context, final OnDialogClickListener listener) {
        itemSelect = false;
        View view = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.dialog_photo_movement, null);
        ab = new AlertDialog.Builder(context);
        ab.setView(view);
        ab.setPositiveButton(context.getString(R.string.button_confirm), null);
        ab.setNegativeButton(context.getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        final AlertDialog dialog = ab.create();
        dialog.show();
        Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        button.setTextColor(context.getResources().getColor(R.color.main_color));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.gray));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEditText.getText().toString();
                if (listener != null) {
                    listener.onPositiveClick(dialog, itemSelect ? path : text, itemSelect);
                }
            }
        });
        initRemoveLayout(context, view);
    }

    private void initRenameLayout(View view, String name) {
        mEditText = (EditText) view.findViewById(R.id.ed_rename);
        mEditText.setText(name);
    }

    /**
     * 初始化recyclerView控件
     *
     * @param context
     * @param view
     */
    private void initRemoveLayout(Context context, View view) {
        mEditText = (EditText) view.findViewById(R.id.et_folderName);
        final LinearLayout layout_newFolder = (LinearLayout) view.findViewById(R.id.layout_newFolder);
        final LinearLayout layout_editName = (LinearLayout) view.findViewById(R.id.layout_editName);
        RecyclerView folderRecyclerView = (RecyclerView) view.findViewById(R.id.folderRecyclerView);

        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        folderRecyclerView.setLayoutManager(manager);

        mFolderAdapter = new PhotoFolderAdapter();
        folderRecyclerView.setAdapter(mFolderAdapter);
        mFolderAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                layout_newFolder.setVisibility(View.VISIBLE);
                layout_editName.setVisibility(View.GONE);
                itemSelect = true;
                path = mFolderAdapter.getList(position);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });

        layout_newFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_newFolder.setVisibility(View.GONE);
                layout_editName.setVisibility(View.VISIBLE);
                mEditText.setText("");
                itemSelect = false;
            }
        });
    }

    public void setFolderList(List<PhotoImageBean> mList) {
        if (mFolderAdapter != null) {
            mFolderAdapter.setList(mList);
        }
    }


    public interface OnDialogClickListener {
        public void onPositiveClick(Dialog dialog, String text, boolean bool);
    }
}
