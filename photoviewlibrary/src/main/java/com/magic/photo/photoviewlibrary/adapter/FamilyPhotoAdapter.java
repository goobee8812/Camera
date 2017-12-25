package com.magic.photo.photoviewlibrary.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.activity.PhotoShowImagesActivity;
import com.magic.photo.photoviewlibrary.customView.PhotoGridImageView;
import com.magic.photo.photoviewlibrary.entity.FamilyPhotoInfo;
import com.magic.photo.photoviewlibrary.listener.OnDeleteClickListener;
import com.magic.photo.photoviewlibrary.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * Created by luohaijun on 2016/10/20.
 */

public class FamilyPhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<FamilyPhotoInfo> mList;

    private boolean mIsFooterEnable = false;//是否允许加载更多

    private OnDeleteClickListener mListener;

    private String thumb_img = "?imageView2/1/w/250/h/180";

    public FamilyPhotoAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setList(List<FamilyPhotoInfo> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public void setFooterEnable(boolean mIsFooterEnable) {
        this.mIsFooterEnable = mIsFooterEnable;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == 2) {
            view = LayoutInflater.from(parent.getContext().getApplicationContext()).inflate(R.layout.recyclerview_footview, null);
            return new FootViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext().getApplicationContext()).inflate(R.layout.item_list_familyphoto, null);
            return new MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        int type = getItemViewType(position);
        if (type == 2) {
        } else {
            MyViewHolder viewHolder = (MyViewHolder) holder;
            FamilyPhotoInfo photoInfo = mList.get(position);
            viewHolder.tv_name.setText(photoInfo.getNickName());
            viewHolder.tv_addr.setText(photoInfo.getUploadSite());
            viewHolder.tv_time.setText(photoInfo.getUploadTime());

            viewHolder.photoGridView.setAdapter(mAdapter);
            List<String> tempList = null;
            try {
                tempList = GsonUtils.getStringList(photoInfo.getImageUrls());
            } catch (Exception e) {
                e.printStackTrace();
            }
            viewHolder.photoGridView.setData(tempList, position);
            //头像图片显示
            Glide.with(mContext)
                    .load(photoInfo.getFace())
                    .placeholder(R.drawable.photo_default)
                    .error(R.drawable.photo_icon_default_bad)
                    .crossFade()
                    .into(viewHolder.iv_head);
            viewHolder.tv_delete.setVisibility(View.VISIBLE);
            viewHolder.tv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onDeleteClick(v, position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        int size = mList == null ? 0 : mList.size();
        if (mIsFooterEnable && size > 0) {
            size++;
        }
        return size;
    }

    @Override
    public int getItemViewType(int position) {
        int footPosition = getItemCount() - 1;
        if (footPosition == position && mIsFooterEnable) {
            return 2;
        }
        return 1;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_head;
        TextView tv_name, tv_time, tv_addr, tv_delete;
        PhotoGridImageView photoGridView;

        public MyViewHolder(View itemView) {
            super(itemView);
            iv_head = (ImageView) itemView.findViewById(R.id.iv_head);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            tv_addr = (TextView) itemView.findViewById(R.id.tv_addr);
            tv_delete = (TextView) itemView.findViewById(R.id.tv_delete);
            photoGridView = (PhotoGridImageView) itemView.findViewById(R.id.photoGridView);
        }
    }

    public class FootViewHolder extends RecyclerView.ViewHolder {

        public FootViewHolder(View itemView) {
            super(itemView);
        }
    }

    //设置adapter
    private PhotoGridImageViewAdapter mAdapter = new PhotoGridImageViewAdapter() {

        @Override
        public void onDisplayImage(Context context, ImageView imageView, Object o) {
            String path = o.toString() + thumb_img;
            Glide.with(context)
                    .load(path)
                    .centerCrop()
                    .placeholder(R.drawable.photo_default)
                    .error(R.drawable.photo_icon_default_bad)
                    .into(imageView);
        }

        @Override
        public void onItemImageClick(boolean canEdit, ImageView view, int group, int index, List list) {
            super.onItemImageClick(canEdit, view, group, index, list);

            intentActivity(index, group);
        }

        @Override
        public void onItemLongClick(ImageView view, int position) {
            super.onItemLongClick(view, position);

        }
    };

    private void intentActivity(int position, int groupPosition) {
        if (mList != null && mList.size() > groupPosition) {
            FamilyPhotoInfo photoInfo = mList.get(groupPosition);
            List<String> tempList = null;
            try {
                tempList = GsonUtils.getStringList(photoInfo.getImageUrls());

                //跳转浏览图片页面
                Intent intent = new Intent(mContext, PhotoShowImagesActivity.class);
                intent.putStringArrayListExtra("child_list", (ArrayList<String>) tempList);
                intent.putExtra("img_position", position);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
