package com.magic.photo.photoviewlibrary.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.entity.event.ImageListEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity {

    private HashMap<String, List<String>> mGruopMap = null;

    private Toast mToast = null;
    private ProgressDialog mProgressDialog;//加载等待条

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PhotoMainAvtivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initImages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initImages() {
        //显示进度条
//        mProgressDialog = ProgressDialog.show(this, null, "正在加载...");
//        new FileManager().getLocalImages(getApplicationContext());
    }

    @Subscribe
    public void onEventMainThread(ImageListEvent event) {
//        dismiss();
//        mGruopMap = event.mGruopMap;
    }


    /******文件操作*************************************************************************************/

    /**
     * 删除文件
     */
    private void deleteFileDialog() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle("删除操作");
        ab.setMessage("确定删除?");
        ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                deleteFile();
            }
        });
        ab.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        ab.create().show();
    }

    private void dismiss() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * 显示底部提示框
     *
     * @param message
     */
    private void showToast(String message) {
        if (mToast == null) {
            mToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(message);
        }
        mToast.show();
    }

}
