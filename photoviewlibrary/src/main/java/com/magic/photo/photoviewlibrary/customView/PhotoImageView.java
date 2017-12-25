package com.magic.photo.photoviewlibrary.customView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.magic.photo.photoviewlibrary.R;


/**
 * description:
 * Created by luohaijun on 2016/4/21.
 */
public class PhotoImageView extends ImageView {

    private int isChecked = 0;//是否被选中0:不可编辑 1、未选中 2、选中

    private Paint mPaint;

    public PhotoImageView(Context context) {
        super(context);
    }

    public PhotoImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Bitmap bitmap;
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        if (isChecked == 1) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.photo_uncheck_icon);
        } else if (isChecked == 2) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.photo_checked_icon);
        } else {
            bitmap = null;
        }
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, (getWidth() - bitmap.getWidth() - 8), 8, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Drawable drawable = getDrawable();
                if (drawable != null) {
                    setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Drawable drawableUp = getDrawable();
                if (drawableUp != null) {
                    clearColorFilter();
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    public void setEdit() {

    }

    /**
     * 0:不可编辑 1、未选中 2、选中
     *
     * @param isChecked
     */
    public void setChecked(int isChecked) {
        this.isChecked = isChecked;
        requestLayout();
    }

    /**
     * 获取选中状态
     *
     * @return
     */
    public int getChecked() {

        return isChecked;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setImageDrawable(null);
    }
}
