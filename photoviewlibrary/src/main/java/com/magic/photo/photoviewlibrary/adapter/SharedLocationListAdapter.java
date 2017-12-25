package com.magic.photo.photoviewlibrary.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.magic.photo.photoviewlibrary.R;

import java.util.List;

/**
 * description:
 * Created by luohaijun on 2016/10/18.
 */

public class SharedLocationListAdapter extends BaseAdapter {

    private List<String> mList;
    private String address;

    public void setList(List<String> list, String address) {
        this.mList = list;
        this.address = address;
        notifyDataSetChanged();
    }

    public String getItemByPosition(int position) {
        return mList.get(position);
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder holder;
        if (convertView == null) {
            holder = new MyViewHolder();
            convertView = LayoutInflater.from(parent.getContext().getApplicationContext()).inflate(R.layout.spinner_item_text, null);
            holder.tv_locName = (TextView) convertView.findViewById(R.id.tv_locName);
            holder.tv_addr = (TextView) convertView.findViewById(R.id.tv_addr);
            convertView.setTag(holder);
        } else {
            holder = (MyViewHolder) convertView.getTag();
        }
        if (position > 1) {
            holder.tv_addr.setVisibility(View.VISIBLE);
            holder.tv_addr.setText(address == null ? "" : address);
        } else {
            holder.tv_addr.setVisibility(View.GONE);
        }
        holder.tv_locName.setText(mList.get(position));
        return convertView;
    }

    class MyViewHolder {
        TextView tv_locName;
        TextView tv_addr;
    }
}
