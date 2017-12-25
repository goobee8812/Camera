package com.magic.photo.photoviewlibrary.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.entity.PhotoImageBean;
import com.magic.photo.photoviewlibrary.listener.OnItemClickListener;

import java.util.List;

/**
 * description:
 * Created by luohaijun on 2016/10/12.
 */

public class PhotoFolderAdapter extends RecyclerView.Adapter<PhotoFolderAdapter.MyViewHolder> {

    private List<PhotoImageBean> folderList;

    private OnItemClickListener listener;

    public void setList(List<PhotoImageBean> folderList) {
        this.folderList = folderList;
    }

    public String getList(int position) {
        if (folderList == null || folderList.size() < position) {
            return null;
        }
        return folderList.get(position).getTopImagePath();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_folder, parent,false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.folderName.setText(folderList.get(position).getFolderName());
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
        return folderList == null ? 0 : folderList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView folderName;

        public MyViewHolder(View itemView) {
            super(itemView);
            folderName = (TextView) itemView.findViewById(R.id.folderName);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
