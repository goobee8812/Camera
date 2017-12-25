package com.magic.photo.photoviewlibrary.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.magic.photo.photoviewlibrary.R;

import java.util.List;

/**
 * description:
 * Created by luohaijun on 2016/10/14.
 */

public class AddPhotoAdapter extends RecyclerView.Adapter<AddPhotoAdapter.MyViewHolder> {

    private Context mContext;
    private List<String> mList;

    private OnLocationClickListener listener;

    public AddPhotoAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setList(List<String> mList) {
        this.mList = mList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext().getApplicationContext()).inflate(R.layout.list_item_image, null);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        int count = mList.size();
        if (position < count - 1) {
            //图片显示
            Glide.with(mContext)
                    .load(mList.get(position))
                    .placeholder(R.drawable.photo_default)
                    .error(R.drawable.photo_icon_default_bad)
                    .crossFade()
                    .into(holder.iv_image);
        } else {
            holder.iv_image.setImageResource(R.drawable.photo_add);
        }
        holder.itemView.bringToFront();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_image;

        public MyViewHolder(View itemView) {
            super(itemView);
            iv_image = (ImageView) itemView.findViewById(R.id.iv_image);
        }
    }

    public void setOnItemClickListener(OnLocationClickListener listener) {
        this.listener = listener;
    }

    public interface OnLocationClickListener {
        public void onItemClick(View view, int position);
    }
}
