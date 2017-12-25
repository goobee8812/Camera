package com.magic.photo.photoviewlibrary.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.magic.photo.photoviewlibrary.R;

import java.util.List;

/**
 * Created by luohaijun on 2016/2/26.
 */
public class PhotoImagePagerAdapter extends PagerAdapter {

    private List<String> imgList;
    private Context context;

    private OnItemClickListener listener;
    private boolean isChanged;

    public PhotoImagePagerAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<String> imgList) {
        this.imgList = imgList;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return imgList == null ? 0 : imgList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        View view = LayoutInflater.from(container.getContext().getApplicationContext()).inflate(R.layout.photo_single_img_layout, null);
        PhotoView imageView = (PhotoView) view.findViewById(R.id.iv_img);
        imageView.enable();
        //图片显示
        Glide.with(context)
                .load(imgList.get(position))
                .placeholder(R.drawable.photo_default)
                .error(R.drawable.photo_icon_default_bad)
                .crossFade()
                .into(imageView);
        // Now just add PhotoView to ViewPager and return it
        container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(v, position);
                }
            }
        });
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        super.destroyItem(container, position, object);
        container.removeView((View) object);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }
}
