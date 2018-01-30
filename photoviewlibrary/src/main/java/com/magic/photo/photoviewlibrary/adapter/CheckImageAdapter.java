package com.magic.photo.photoviewlibrary.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.entity.Image;

import java.io.File;
import java.util.List;

/**
 * @date 2017/6/28 13
 */
public class CheckImageAdapter extends PagerAdapter {

    private List<Image> mList;
    private Context mContext;
    private OnItemClickListener listener;

    public CheckImageAdapter(List<Image> list, Context context) {
        mList = list;
        mContext = context;
    }

    public void setList(List<Image> list) {
        mList = list;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_check_image, null);
        ImageView photoView = (ImageView) rootView.findViewById(R.id.photo_view);
//        photoView.enable();
        String path = mList.get(position).path;
        if (path != null && new File(path).exists()) {
            Glide.with(mContext).load(path).into(photoView);
        }
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(v, position);
                }
            }
        });
        container.addView(rootView);
        return rootView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
