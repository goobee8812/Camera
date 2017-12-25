package com.magic.photo.photoviewlibrary.customView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.magic.photo.photoviewlibrary.adapter.ImageAdapter;
import com.magic.photo.photoviewlibrary.utils.Utils;

/**
 * 2017/6/24 17
 */
public class GridImageItemDecoration extends RecyclerView.ItemDecoration {
    private Paint mPaint;
    private Context mContext;
    private ImageAdapter mImageAdapter;
    private static final int STROKE_WIDTH = 2; //dp
    private static final int LEFT_OFFSET = 10; //dp

    public GridImageItemDecoration(Context context, int color) {
        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(Utils.dip2px(mContext, STROKE_WIDTH));
        mPaint.setColor(color);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(childView);
            if (mImageAdapter.isDrawLine(position)) {
                Rect r = new Rect();
                r.top = childView.getTop();
                r.left = childView.getLeft();
                r.right = childView.getLeft() + Utils.dip2px(mContext, (LEFT_OFFSET - STROKE_WIDTH) / 2);
                r.bottom = childView.getBottom();
                c.drawRect(r, mPaint);
            }
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view);
        if (mImageAdapter.isDrawLine(position)) {
            outRect.left = Utils.dip2px(mContext, LEFT_OFFSET);
        }
    }

    public void setAdapter(ImageAdapter adapter) {
        mImageAdapter = adapter;
    }
}
