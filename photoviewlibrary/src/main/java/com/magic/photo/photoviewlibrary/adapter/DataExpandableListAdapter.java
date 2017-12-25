package com.magic.photo.photoviewlibrary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.utils.DateUtils;

import java.util.List;
import java.util.Map;


/**
 * description:
 * Created by luohaijun on 2016/11/23.
 */

public class DataExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<String> groupList;
    private Map<String, List<String>> childList;

    public DataExpandableListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setList(List<String> groupList, Map<String, List<String>> childList) {
        this.groupList = groupList;
        this.childList = childList;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return groupList == null ? 0 : groupList.size();
    }

    @Override
    public int getChildrenCount(int i) {
        if (childList == null) {
            return 0;
        }
        String date = groupList.get(i);
        List<String> tempList = childList.get(date);
        return tempList == null ? 0 : tempList.size();
    }

    @Override
    public Object getGroup(int i) {
        return groupList.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        String date = groupList.get(i);
        List<String> tempList = childList.get(date);
        return tempList == null ? "" : tempList.get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        String year = DateUtils.getFormaterTime(groupList.get(i), "yyyyMM", "yyyy");
        View v = getGenericView(year, mContext.getResources().getColor(R.color.main_color));
        return v;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        String s = getChild(i, i1).toString();
        String month = DateUtils.getFormaterTime(s, "yyyyMM", mContext.getString(R.string.string_date_month));
        View v = getGenericView(month, mContext.getResources().getColor(R.color.gray));
        return v;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    //创建组/子视图
    public View getGenericView(String s, int color) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.textview_layout, null);
        TextView textView = (TextView) view.findViewById(R.id.tv_date);
        textView.setText(s);
        textView.setTextColor(color);
        return view;
    }
}
