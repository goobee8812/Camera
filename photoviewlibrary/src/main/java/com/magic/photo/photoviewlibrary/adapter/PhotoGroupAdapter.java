package com.magic.photo.photoviewlibrary.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.entity.PhotoImageBean;
import com.magic.photo.photoviewlibrary.entity.event.PhotoSelectStatusEvent;
import com.magic.photo.photoviewlibrary.listener.OnItemClickListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * Created by luohaijun on 2016/9/27.
 */

public class PhotoGroupAdapter extends RecyclerView.Adapter<PhotoGroupAdapter.MyViewHolder> {

    private List<PhotoImageBean> mList;
    private Context mContext;

    private boolean isEdit;
    private List<Boolean> statusList;

    private OnItemClickListener listener;

    public PhotoGroupAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<PhotoImageBean> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public void setEdit(boolean isEdit) {
        this.isEdit = isEdit;
        initStatusList();
        notifyDataSetChanged();
    }

    public void setNoEdit(boolean isEdit) {
        this.isEdit = isEdit;
    }

    public void setStatusList(List<Boolean> statusList) {
        this.statusList = statusList;
    }

    public List<Boolean> getStatusList() {

        return statusList;
    }

    /**
     * 全选
     */
    public void setSelectAll(boolean isAll) {
        if (mList == null)
            return;
        isEdit = true;
        statusList = new ArrayList<>();
        for (int i = 0; i < mList.size(); i++) {
            statusList.add(isAll);
        }
        notifyDataSetChanged();
    }

    /**
     * 更新状态
     */
    public void notifyDateChanged() {
        if (mList == null || mList.size() == 0) {
            isEdit = false;
        }
        if (isEdit) {
            statusList = new ArrayList<>();
            for (int i = 0; i < mList.size(); i++) {
                statusList.add(false);
            }
        }
        notifyItemRangeChanged(0, mList.size());
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_mainphoto, null);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        PhotoImageBean imageBean = mList.get(position);

        holder.tv_folderName.setText(imageBean.getFolderName());
        holder.tv_count.setText(imageBean.getImageCounts() + mContext.getString(R.string.string_piece));

        //图片编辑状态
        if (isEdit) {
            holder.iv_check.setVisibility(View.VISIBLE);
            boolean bool = statusList.get(position);
            holder.iv_check.setImageResource(bool ? R.drawable.photo_checked_icon : R.drawable.photo_uncheck_icon);
        } else {
            holder.iv_check.setVisibility(View.GONE);
        }

        //图片显示
        Glide.with(mContext)
                .load(imageBean.getTopImagePath())
                .placeholder(R.drawable.photo_default)
                .error(R.drawable.photo_icon_default_bad)
                .crossFade()
                .into(holder.iv_cover);
        //单击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdit) {
                    boolean bool = statusList.get(position);
                    holder.iv_check.setImageResource(bool ? R.drawable.photo_uncheck_icon : R.drawable.photo_checked_icon);
                    statusList.set(position, !bool);
                    EventBus.getDefault().post(new PhotoSelectStatusEvent(statusList));
                    return;
                }
                if (listener != null) {
                    listener.onItemClick(v, position);
                }
            }
        });
        //长按事件
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onItemLongClick(v, position);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    private void initStatusList() {
        if (mList == null || !isEdit)
            return;
        statusList = new ArrayList<>();
        for (int i = 0; i < mList.size(); i++) {
            statusList.add(false);
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_cover;
        TextView tv_folderName;
        TextView tv_count;
        ImageView iv_check;

        public MyViewHolder(View itemView) {
            super(itemView);
            iv_cover = (ImageView) itemView.findViewById(R.id.iv_cover);
            tv_folderName = (TextView) itemView.findViewById(R.id.tv_folderName);
            tv_count = (TextView) itemView.findViewById(R.id.tv_count);
            iv_check = (ImageView) itemView.findViewById(R.id.iv_check);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
