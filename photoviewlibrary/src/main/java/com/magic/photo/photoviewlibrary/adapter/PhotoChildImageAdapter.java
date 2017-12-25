package com.magic.photo.photoviewlibrary.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.customView.PhotoGridImageView;
import com.magic.photo.photoviewlibrary.customView.PhotoImageView;
import com.magic.photo.photoviewlibrary.entity.PhotoInfo;
import com.magic.photo.photoviewlibrary.entity.event.ImageStatusEvent;
import com.magic.photo.photoviewlibrary.entity.event.PhotoOperateEvent;
import com.magic.photo.photoviewlibrary.utils.DateUtils;
import com.magic.photo.photoviewlibrary.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


/**
 * description:
 * Created by luohaijun on 2016/4/26.
 */
public class PhotoChildImageAdapter extends RecyclerView.Adapter<PhotoChildImageAdapter.MyViewHolder> {

    private List<PhotoInfo> mBeanList;

    private boolean canSelect;
    private boolean selectAll;//全选
    private TreeMap<String, List<Boolean>> statusMap;//存储选中状态

    /**
     * 1、选择 2、全选 3、设置状态
     */
    private static int clickType;

    public PhotoChildImageAdapter() {
        statusMap = new TreeMap<>();
    }

    public void setList(List<PhotoInfo> mBeanList) {
        clickType = 0;
        this.mBeanList = mBeanList;
        notifyDataSetChanged();
    }

    public List<PhotoInfo> getList() {
        return mBeanList;
    }

    /**
     * 编辑
     *
     * @param canSelect
     */
    public void setEdit(boolean canSelect) {
        this.canSelect = canSelect;
        clickType = 1;
        if (canSelect == false) {
            selectAll = false;
            reSetSelectStatus();
        }
        notifyDataSetChanged();
    }

    /**
     * 是否可编辑
     *
     * @return
     */
    public boolean getEdit() {
        return canSelect;
    }

    /**
     * 全选
     */
    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
        clickType = 2;
        reSetSelectStatus();
        notifyDataSetChanged();
    }

    /**
     * 设置选中状态list
     */
    public void setSelectStatus(TreeMap<String, List<Boolean>> statusMap) {
        this.statusMap = statusMap;
        clickType = 3;

    }

    /**
     * 获取选中状态list
     *
     * @return
     */
    public TreeMap<String, List<Boolean>> getSelectStatus() {
        return statusMap;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_childimage, null);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        PhotoInfo imageBean = mBeanList.get(position);
        holder.tv_date.setText(DateUtils.getFormaterTime(imageBean.getImgDate(), UIUtils.getContext().getString(R.string.string_date_patten)));
        holder.mGridImageView.setAdapter(mAdapter);
        holder.mGridImageView.setData(imageBean.getUrlList(), position);

        switch (clickType) {
            case 0:
                Log.e("shitian22", "hahahahah....." + clickType);
                break;
            case 1://编辑
                holder.mGridImageView.setEdit(canSelect);
                break;
            case 2://全选
                holder.mGridImageView.setEdit(canSelect, statusMap.get("" + position));
                break;
            case 3:
                if (!statusMap.containsKey("" + position)) {
                    setMapValue(position);
                }
                holder.mGridImageView.setEdit(canSelect, statusMap.get("" + position));
                break;
            default:

                break;
        }
    }

    @Override
    public int getItemCount() {
        return mBeanList == null ? 0 : mBeanList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_date;
        PhotoGridImageView mGridImageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            mGridImageView = (PhotoGridImageView) itemView.findViewById(R.id.gridImageView);
            mGridImageView.setBoolParams(true);
        }
    }

    //设置adapter
    private PhotoGridImageViewAdapter mAdapter = new PhotoGridImageViewAdapter() {

        @Override
        public void onDisplayImage(Context context, ImageView imageView, Object o) {
            Glide.with(context)
                    .load(o.toString())
                    .centerCrop()
                    .placeholder(R.drawable.photo_default)
                    .error(R.drawable.photo_icon_default_bad)
                    .into(imageView);
        }

        @Override
        public void onItemImageClick(boolean canEdit, ImageView view, int group, int index, List list) {
            super.onItemImageClick(canEdit, view, group, index, list);
            if (canSelect) {
                setStatusByPosition(view, group, index);
//                statusMap.put("" + index, list);
                clickType = 3;
                EventBus.getDefault().post(new ImageStatusEvent(statusMap, canSelect, group, index));
            } else {
                EventBus.getDefault().post(new ImageStatusEvent(null, canSelect, group, index));
            }
        }

        @Override
        public void onItemLongClick(ImageView view, int position) {
            super.onItemLongClick(view, position);
            if (!canSelect) {
                canSelect = true;
                reSetSelectStatus();
                setEdit(canSelect);
                PhotoOperateEvent event = new PhotoOperateEvent(1, 22, 2, true);
                EventBus.getDefault().post(event);
            }
        }
    };

    /**
     * 重置选中状态
     */
    private void reSetSelectStatus() {

        if (mBeanList == null) {
            return;
        }
        List<String> tempList;
        for (int i = 0; i < mBeanList.size(); i++) {
            tempList = mBeanList.get(i).getUrlList();
            List<Boolean> boolList = new ArrayList<>();
            for (int j = 0; j < tempList.size(); j++) {
                boolList.add(selectAll);
            }
            statusMap.put(i + "", boolList);
        }
        EventBus.getDefault().post(statusMap);
    }

    /**
     * 初始化map状态
     */
    private void setMapValue(int position) {
        if (mBeanList == null || mBeanList.size() < position) {
            return;
        }
        List<String> tempList = mBeanList.get(position).getUrlList();
        List<Boolean> bList = new ArrayList<>();
        for (int i = 0; i < tempList.size(); i++) {
            bList.add(selectAll);
        }
        statusMap.put("" + position, bList);
    }

    private void setStatusByPosition(ImageView view, int groupPosition, int childPosition) {
        if (statusMap != null) {
            if (!statusMap.containsKey("" + groupPosition)) {
                setMapValue(groupPosition);
            }
            List<Boolean> tempList = statusMap.get("" + groupPosition);
            boolean bool = tempList.get(childPosition);
            ((PhotoImageView) view).setChecked(!bool ? 2 : 1);
            tempList.set(childPosition, !bool);
            statusMap.put("" + groupPosition, tempList);
        }
    }
}
