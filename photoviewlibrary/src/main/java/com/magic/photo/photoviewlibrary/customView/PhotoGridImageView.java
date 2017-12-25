package com.magic.photo.photoviewlibrary.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.magic.photo.photoviewlibrary.adapter.PhotoGridImageViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * Created by luohaijun on 2016/4/20.
 */
public class PhotoGridImageView<T> extends ViewGroup {

    private int mColumnCount = 4;//列数
    private int mGap = 5;//间隙
    private int imageSize;//图片大小

    private PhotoGridImageViewAdapter<T> mAdapter;
    private List<PhotoImageView> mImageViewList = new ArrayList<>();//存储item
    private List<T> imgList;

    private int group;//item position

//    private List<Boolean> statusList;//选中状态存储
    private boolean canSelect;//可编辑的
    private boolean boolParams;

    public PhotoGridImageView(Context context) {
        super(context);
    }

    public PhotoGridImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height;
        int totalWidth = width - getPaddingLeft() - getPaddingRight();
        if (imgList != null && imgList.size() > 0) {
            mImageViewList.get(0).setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageSize = (totalWidth - mGap * (mColumnCount - 1)) / mColumnCount;
            int mRowCount = imgList.size() / mColumnCount + (imgList.size() % mColumnCount == 0 ? 0 : 1);
            height = imageSize * mRowCount + mGap * (mRowCount - 1) + getPaddingTop() + getPaddingBottom();
            setMeasuredDimension(width, height);
        } else {
            height = width;
            setMeasuredDimension(width, height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        childLayout();
    }

    /**
     * child layout布局
     */
    private void childLayout() {
        if (imgList == null) {
            return;
        }
        int childCount = imgList.size();
        for (int i = 0; i < childCount; i++) {
            PhotoImageView child = (PhotoImageView) getChildAt(i);
            //图片显示
            if (mAdapter != null) {
                mAdapter.onDisplayImage(getContext(), child, imgList.get(i));
            }
            int columnNum = i % mColumnCount;
            int rowNum = i / mColumnCount;
            int left = (imageSize + mGap) * columnNum + getPaddingLeft();
            int top = (imageSize + mGap) * rowNum + getPaddingTop();
            int right = left + imageSize;
            int bottom = top + imageSize;
            child.layout(left, top, right, bottom);
        }
    }

    public void setBoolParams(boolean boolParams) {
        this.boolParams = boolParams;
    }

    /**
     * 设置数据
     */
    public void setData(List lists, int group) {
        this.group = group;
        //判断list
        if (lists == null || lists.isEmpty()) {
            this.setVisibility(GONE);
            return;
        } else {
            this.setVisibility(VISIBLE);
        }
        //初始化list
        if (imgList == null) {
            int i = 0;
            while (i < lists.size()) {
                ImageView iv = createImageView(i);
                if (iv == null) {
                    return;
                }
                addView(iv, generateDefaultLayoutParams());
                i++;
            }
        } else {
            int oldViewCount = imgList.size();
            int newViewCount = lists.size();
            if (oldViewCount >= newViewCount) {
                removeViews(newViewCount, getChildCount() - newViewCount);
            } else if (oldViewCount < newViewCount) {
                for (int i = oldViewCount; i < newViewCount; i++) {
                    ImageView iv = createImageView(i);
                    if (iv == null) {
                        return;
                    }
                    if (iv.getParent() != null) {
                        removeView(iv);
                    }
                    addView(iv, generateDefaultLayoutParams());
                }
            }
        }
        imgList = lists;
        requestLayout();
    }

    /**
     * 设置适配器
     *
     * @param adapter 适配器
     */
    public void setAdapter(PhotoGridImageViewAdapter adapter) {
        mAdapter = adapter;
    }

    /**
     * 获得 ImageView
     * 保证了 ImageView 的重用
     *
     * @param position 位置
     */
    private ImageView createImageView(final int position) {
        if (position < mImageViewList.size()) {
            return mImageViewList.get(position);
        } else {
            if (mAdapter != null) {
                final PhotoImageView imageView = (PhotoImageView) mAdapter.generateImageView(getContext());

                mImageViewList.add(imageView);
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (canSelect) {//编辑状态
                            //更新存储状态
//                            if (statusList != null) {
//                                boolean bool = statusList.get(position);
//                                bool = !bool;
//                                statusList.set(position, bool);
//                                ((PhotoImageView) v).setChecked(bool ? 2 : 1);
//                            }
//                            mAdapter.onItemImageClick(canSelect,imageView, group,position, statusList);
                        } else {//图片浏览状态
//                            //跳转浏览图片页面
//                            Intent intent = new Intent(getContext(), PhotoShowImagesActivity.class);
//                            intent.putStringArrayListExtra("child_list", (ArrayList<String>) imgList);
//                            intent.putExtra("img_position", position);
//                            intent.putExtra("showBottomLayout", boolParams);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            getContext().startActivity(intent);
                        }
                        mAdapter.onItemImageClick(canSelect,imageView, group,position, null);
                    }
                });
                imageView.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mAdapter.onItemLongClick(imageView, group);
                        return false;
                    }
                });
                return imageView;
            } else {
                return null;
            }
        }
    }

//    /**
//     * 获取选中状态list
//     */
//    public List<Boolean> getSelectStatus() {
//
//        return statusList;
//    }
//
//    /**
//     * 设置选中状态list
//     */
//    public void setSelectStatus(List<Boolean> statusList) {
//        this.statusList = statusList;
//        if (statusList != null && mImageViewList.size() == statusList.size()) {
//            for (int i = 0; i < mImageViewList.size(); i++) {
//                mImageViewList.get(i).setChecked(statusList.get(i) ? 2 : 1);
//            }
//        }
//    }

//    /**
//     * 全选
//     *
//     * @param selectAll
//     */
//    public void setSelectAll(boolean selectAll) {
//        if (mImageViewList == null || !canSelect || statusList == null || statusList.size() != mImageViewList.size()) {
//            return;
//        }
//        for (int i = 0; i < mImageViewList.size(); i++) {
//            mImageViewList.get(i).setChecked(selectAll ? 2 : 1);
//            statusList.set(i, selectAll);
//        }
//    }

    /**
     * 设置编辑模式
     */
    public void setEdit(boolean canSelect) {

        if (mImageViewList == null) {
            return;
        }
        this.canSelect = canSelect;
//        statusList = new ArrayList<>();
        PhotoImageView photoImageView;
        for (int i = 0; i < mImageViewList.size(); i++) {
            photoImageView = mImageViewList.get(i);
//            statusList.add(false);
            photoImageView.setChecked(canSelect ? 1 : 0);
        }
    }

    /**
     * 设置编辑模式
     */
    public void setEdit(boolean canSelect, List<Boolean> statusList) {

        if (mImageViewList == null || statusList == null) {
            return;
        }
//        this.statusList = statusList;
        this.canSelect = canSelect;
        PhotoImageView photoImageView;
        for (int i = 0; i < mImageViewList.size(); i++) {
            if (i > statusList.size() - 1) {
                break;
            }
            photoImageView = mImageViewList.get(i);
            photoImageView.setChecked(statusList.get(i) ? 2 : 1);
        }
    }
}
