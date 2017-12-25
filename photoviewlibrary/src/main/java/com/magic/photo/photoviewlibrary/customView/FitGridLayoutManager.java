package com.magic.photo.photoviewlibrary.customView;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * description:
 * Created by luohaijun on 2016/10/14.
 */

public class FitGridLayoutManager extends GridLayoutManager {

    public FitGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FitGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        int height = 0;
        Log.i("msg", "onMeasure---MeasureSpec-" + View.MeasureSpec.getSize(heightSpec));
        int childCount = getItemCount();
        int spanCount = getSpanCount();
        if (childCount>0) {
            for (int i = 0; i < childCount; i++) {
                View child = recycler.getViewForPosition(i);
                measureChild(child, widthSpec, heightSpec);
                if (i % spanCount == 0) {
                    int measuredHeight = child.getMeasuredHeight() + getDecoratedBottom(child);
                    height += measuredHeight;
                }
            }
            Log.i("msg", "onMeasure---height-" + height);
            setMeasuredDimension(View.MeasureSpec.getSize(widthSpec), height);
        }
    }
}
