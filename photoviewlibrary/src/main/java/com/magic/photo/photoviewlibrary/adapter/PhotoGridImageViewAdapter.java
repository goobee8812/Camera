package com.magic.photo.photoviewlibrary.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.magic.photo.photoviewlibrary.customView.PhotoImageView;

import java.util.List;

/**
 * Created by Jaeger on 16/2/24.
 * <p/>
 * Email: chjie.jaeger@gamil.com
 * GitHub: https://github.com/laobie
 */
public abstract class PhotoGridImageViewAdapter<T> {
    public abstract void onDisplayImage(Context context, ImageView imageView, T t);

    public void onItemImageClick(boolean canEdit, ImageView view, int group, int index, List<Boolean> list) {
    }

    public void onItemLongClick(ImageView view, int position) {

    }

    /**
     * 普通图片
     *
     * @param context
     * @return
     */
    public ImageView generateImageView(Context context) {
        PhotoImageView imageView = new PhotoImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }
}