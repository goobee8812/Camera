package com.magic.photo.photoviewlibrary.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.entity.Image;
import com.magic.photo.photoviewlibrary.fragments.ImageFragment;
import com.magic.photo.photoviewlibrary.utils.DateUtils;
import com.magic.photo.photoviewlibrary.utils.FileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 2017/6/24 16
 */
public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "VideoAdapter";
    private static final int IMAGE_TYPE = 0;
    private static final int TITLE_TYPE = 1;
    private static final int BOTTOM_TYPE = 2;
    private int count;
    private boolean isEdit; //编辑模式的标志位；
    private List<ImageWrap> mImageWraps;
    private Context mContext;
    private boolean isSelect = false;
    private List<Integer> selectList = new ArrayList<>();
    private ImageFragment.OnPhotoSelectChangeListener listener;

    public ImageAdapter(Context context, int count, ImageFragment.OnPhotoSelectChangeListener listener) {
        this.count = count;
        this.mContext = context;
        mImageWraps = new ArrayList<>();
        this.listener = listener;
    }

    public void setImages(List<Image> images) {
        mImageWraps.clear();
        mImageWraps = transformVideo(images);

        notifyDataSetChanged();
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView;
        if (viewType == IMAGE_TYPE) {
            rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_layout, parent, false);
            return new VideoViewHolder(rootView);
        } else if (viewType == BOTTOM_TYPE) {
            rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_footer_layout, parent, false);
            return new FooterViewHolder(rootView);
        } else {
            rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_title_layout, parent, false);
            return new TitleViewHolder(rootView);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VideoViewHolder) {
            VideoViewHolder viewHolder = ((VideoViewHolder) holder);
            viewHolder.mPlayIcon.setVisibility(View.GONE);
            if (TextUtils.equals(mImageWraps.get(position).mImage.mimeType, "image/gif")) {
                Glide.with(mContext).load(mImageWraps.get(position).mImage.path).asGif().into(viewHolder.mThumbnail);
            } else {
                Glide.with(mContext).load(mImageWraps.get(position).mImage.path).into(viewHolder.mThumbnail);
            }
            if (isSelect) {
                if (isSelectThePhoto(position)) {
                    viewHolder.mIvCheck.setImageResource(R.drawable.sel_box);
                    viewHolder.mIvCheck.setAlpha(1.0f);
                    viewHolder.mIvCheck.setVisibility(View.VISIBLE);
                    viewHolder.view.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.mIvCheck.setImageResource(R.drawable.box);
                    viewHolder.mIvCheck.setAlpha(0.5f);
                    viewHolder.mIvCheck.setVisibility(View.VISIBLE);
                    viewHolder.view.setVisibility(View.GONE);
                }
            } else {
                viewHolder.mIvCheck.setVisibility(View.GONE);
                viewHolder.view.setVisibility(View.GONE);
            }

            ((VideoViewHolder) holder).mTextView.setText(DateUtils.getFormaterTime(mImageWraps.get(position).mImage.createTime, "HH:mm"));
        } else if (holder instanceof TitleViewHolder) {
            ((TitleViewHolder) holder).mTitle.setText(mImageWraps.get(position).title);
        }
    }


    public boolean isAllPhotoSelect() {
        int size = mImageWraps.size();
        int title = 0;
        for (int i = 0; i < size; i++) {
            if (mImageWraps.get(i).isTitle) {
                title++;
                continue;
            }
        }
        if (selectList.size() == size - title) {
            return true;
        } else {
            return false;
        }

    }

    public void allSelectPhotos() {
        if (isAllPhotoSelect()) {
            selectList.clear();
            if (listener != null) {
                listener.onPhotoSelectChange(selectList.size());
            }
            notifyDataSetChanged();
        } else {
            selectList.clear();
            for (int i = 0; i < mImageWraps.size(); i++) {
                if (mImageWraps.get(i).isTitle) {
                    continue;
                }
                selectList.add(i);
            }
            System.out.println(selectList);
            if (listener != null) {
                listener.onPhotoSelectChange(selectList.size());
            }
            notifyDataSetChanged();
        }

    }

    public List<String> getSelectImagePath() {
        List<String> list = new ArrayList<>();
        for (int position : selectList) {
            String path = mImageWraps.get(position).mImage.path;
            list.add(path);
        }
        return list;
    }

    public void delectPhotos() {
        List<Integer> list = new ArrayList<>();
        for (int position : selectList) {
            System.out.println(position);
            if (position >= mImageWraps.size()) {
                System.out.println("continue");
                continue;
            }
            boolean deleteFile = FileUtils.deleteFile(mContext, mImageWraps.get(position).mImage.path);
            if (deleteFile) {
                list.add(position);
            }
        }

        for (int i = list.size() - 1; i >= 0; i--) {
            int position = list.get(i);
            if (position >= mImageWraps.size()) {
                continue;
            }
            mImageWraps.remove(position);
        }

        list.clear();
        for (int i = 0; i < mImageWraps.size(); i++) {
            ImageWrap imageWrap = mImageWraps.get(i);
            if (imageWrap.isTitle) {
                if ((i + 1) == mImageWraps.size() || mImageWraps.get(i + 1).isTitle) {
                    list.add(i);
                }
            } else {
                continue;
            }
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            int position = list.get(i);
            if (position >= mImageWraps.size()) {
                continue;
            }
            mImageWraps.remove(position);
        }

        selectList.clear();
    }


    public boolean isSelectThePhoto(int position) {
        return selectList.contains(position);
    }


    public void saveSelectPhoto(int position) {
        selectList.add(Integer.valueOf(position));
        if (listener != null) {
            listener.onPhotoSelectChange(selectList.size());
        }
    }

    public int getSelectPhoto() {
        return selectList.size();
    }

    public void removeSelectPhoto(int position) {
        selectList.remove(Integer.valueOf(position));
        if (listener != null) {
            listener.onPhotoSelectChange(selectList.size());
        }
    }

    public void clearSelectPhoto() {
        selectList.clear();
    }


    public void setSelectState(boolean b) {
        isSelect = b;
    }

    public boolean getSelectState() {
        return isSelect;
    }

    @Override
    public int getItemCount() {
        return mImageWraps.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isFooterPosition(position)) {
            return BOTTOM_TYPE;
        }
        ImageWrap videoWrap = mImageWraps.get(position);
        if (videoWrap.isTitle) {
            return TITLE_TYPE;
        } else {
            return IMAGE_TYPE;
        }
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        public ImageView mThumbnail;
        public ImageView mPlayIcon;
        public TextView mTextView;
        public ImageView mIvCheck;
        public View view;

        public VideoViewHolder(View itemView) {
            super(itemView);
            mThumbnail = (ImageView) itemView.findViewById(R.id.imageView_thumbnail);
            mPlayIcon = (ImageView) itemView.findViewById(R.id.imageView_play_icon);
            mTextView = (TextView) itemView.findViewById(R.id.textView_desc);
            mIvCheck = (ImageView) itemView.findViewById(R.id.iv_check);
            view = (View) itemView.findViewById(R.id.view);
        }
    }


    public boolean isFooterPosition(int position) {
        return position >= getItemCount() - 1;
    }

    public static class TitleViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle;

        public TitleViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.textView_title);
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class ImageWrap {
        public Image mImage;
        public boolean isTitle;
        public boolean isNeedDrawLine;
        public String title;

        @Override
        public String toString() {
            return "ImageWrap{" +
                    "mImage=" + mImage +
                    ", isTitle=" + isTitle +
                    ", isNeedDrawLine=" + isNeedDrawLine +
                    ", title='" + title + '\'' +
                    '}';
        }
    }

    public List<ImageWrap> getImageWraps() {
        return mImageWraps;
    }

    public boolean isDrawLine(int position) {
        return mImageWraps.get(position).isNeedDrawLine;
    }

    public boolean isTitle(int position) {
        return mImageWraps.get(position).isTitle;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public Image getImage(int position) {
        return mImageWraps.get(position).mImage;
    }

    private List<ImageWrap> transformVideo(List<Image> images) {
        HashMap<String, List<Image>> map = new HashMap<>();
        for (Image image : images) {
            long createTime = image.createTime;
            String time = DateUtils.getFormaterTime(createTime, "yyyy年MM月dd日");
            if (map.get(time) != null) {
                map.get(time).add(image);
            } else {
                List<Image> mapList = new ArrayList<>();
                mapList.add(image);
                map.put(time, mapList);
            }
        }
        Set<String> stringSet = map.keySet();
        List<String> stringList = new ArrayList<>(stringSet);
        Collections.sort(stringList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.compareTo(o1);
            }
        });
        for (String key : stringList) {
            ImageWrap wrapTitle = new ImageWrap();
            wrapTitle.isTitle = true;
            wrapTitle.title = key;
            mImageWraps.add(wrapTitle);
            List<Image> imageList = map.get(key);
            for (int i = 0; i < imageList.size(); i++) {
                Image image = imageList.get(i);
                ImageWrap wrapVideo = new ImageWrap();
                wrapVideo.mImage = image;
                mImageWraps.add(wrapVideo);
            }
        }
        return mImageWraps;
    }
}
