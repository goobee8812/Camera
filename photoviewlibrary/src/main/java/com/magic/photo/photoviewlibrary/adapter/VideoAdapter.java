package com.magic.photo.photoviewlibrary.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.magic.photo.photoviewlibrary.R;
import com.magic.photo.photoviewlibrary.entity.Video;
import com.magic.photo.photoviewlibrary.fragments.VideoFragment;
import com.magic.photo.photoviewlibrary.utils.DateUtils;
import com.magic.photo.photoviewlibrary.utils.FileUtils;
import com.magic.photo.photoviewlibrary.utils.TimeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 2017/6/24 16
 */
public class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "VideoAdapter";
    private static final int VIDEO_TYPE = 0;
    private static final int TITLE_TYPE = 1;
    private static final int BOTTOM_TYPE = 2;
    private VideoFragment.OnVideoSelectChangeListener listener;
    private int count;
    private boolean isEdit; //编辑模式的标志位；
    private List<VideoWrap> mVideoWraps;
    private SparseBooleanArray mSparseBooleanArray = new SparseBooleanArray();
    private Context mContext;
    private List<Integer> selectList = new ArrayList<>();
    private boolean isSelect=false;

    public VideoAdapter(Context context, int count, VideoFragment.OnVideoSelectChangeListener listener) {
        this.count = count;
        this.mContext = context;
        mVideoWraps = new ArrayList<>();
        this.listener = listener;
    }

    public void setVideos(List<Video> videos) {
        mVideoWraps.clear();
        mVideoWraps = transformVideo(videos);

        notifyDataSetChanged();
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView;
        if (viewType == VIDEO_TYPE) {
            rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_layout, parent, false);
            return new VideoViewHolder(rootView);
        }  else if (viewType == BOTTOM_TYPE) {
            rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_footer_layout, parent, false);
            return new FooterViewHolder(rootView);
        }else {
            rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_title_layout, parent, false);
            return new TitleViewHolder(rootView);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VideoViewHolder) {
            VideoViewHolder viewHolder = ((VideoViewHolder) holder);

            Glide.with(mContext).load(Uri.fromFile(new File(mVideoWraps.get(position).mVideo.path))).into(viewHolder.mThumbnail);
            if (isSelect) {
                if (isSelectTheVideo(position)) {
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
            viewHolder.mTextView.setText(TimeUtil.formatDuration((int) mVideoWraps.get(position).mVideo.duration));
        } else if (holder instanceof TitleViewHolder) {
            ((TitleViewHolder) holder).mTitle.setText(mVideoWraps.get(position).title);
        }
    }

    public boolean isAllVideoSelect() {
        int size = mVideoWraps.size();
        int title = 0;
        for (int i = 0; i < size; i++) {
            if (mVideoWraps.get(i).isTitle) {
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

    public void allSelectVideos() {
        if (isAllVideoSelect()) {
            selectList.clear();
            if (listener != null) {
                listener.onVideoSelectChange(selectList.size());
            }
            notifyDataSetChanged();
        } else {
            selectList.clear();
            for (int i = 0; i < mVideoWraps.size(); i++) {
                if (mVideoWraps.get(i).isTitle) {
                    continue;
                }
                selectList.add(i);
            }
            System.out.println(selectList);
            if (listener != null) {
                listener.onVideoSelectChange(selectList.size());
            }
            notifyDataSetChanged();
        }

    }


    public void delectVideos() {
        List<Integer> list = new ArrayList<>();
        for (int position : selectList) {
            System.out.println(position);
            if (position >= mVideoWraps.size()) {
                System.out.println("continue");
                continue;
            }
            boolean deleteFile = FileUtils.deleteFile(mContext, mVideoWraps.get(position).mVideo.path);
            if (deleteFile) {
                list.add(position);
            }
        }

        for (int i = list.size() - 1; i >= 0; i--) {
            int position = list.get(i);
            if (position >= mVideoWraps.size()) {
                continue;
            }
            mVideoWraps.remove(position);
        }

        list.clear();
        for (int i = 0; i < mVideoWraps.size(); i++) {
            VideoWrap videoWrap = mVideoWraps.get(i);
            if (videoWrap.isTitle) {
                if ((i + 1) == mVideoWraps.size() || mVideoWraps.get(i + 1).isTitle) {
                    list.add(i);
                }
            } else {
                continue;
            }
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            int position = list.get(i);
            if (position >= mVideoWraps.size()) {
                continue;
            }
            mVideoWraps.remove(position);
        }

        selectList.clear();
    }


    public boolean isSelectTheVideo(int position) {
        return selectList.contains(position);
    }


    public void saveSelectVideo(int position) {
        selectList.add(Integer.valueOf(position));
        if (listener != null) {
            listener.onVideoSelectChange(selectList.size());
        }
    }

    public int getSelectVideo() {
        return selectList.size();
    }

    public void removeSelectVideo(int position) {
        selectList.remove(Integer.valueOf(position));
        if (listener != null) {
            listener.onVideoSelectChange(selectList.size());
        }
    }

    public void clearSelectVideo() {
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
        return mVideoWraps.size() + 1;
    }

    public boolean isFooterPosition(int position) {
        return position >= getItemCount() - 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isFooterPosition(position)) {
            return BOTTOM_TYPE;
        }
        VideoWrap videoWrap = mVideoWraps.get(position);
        if (videoWrap.isTitle) {
            return TITLE_TYPE;
        } else {
            return VIDEO_TYPE;
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View itemView) {
            super(itemView);
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

    public static class TitleViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle;

        public TitleViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.textView_title);
        }
    }

    public static class VideoWrap {
        public Video mVideo;
        public boolean isTitle;
        public boolean isNeedDrawLine;
        public String title;

        @Override
        public String toString() {
            return "ImageWrap{" +
                    "mImage=" + mVideo +
                    ", isTitle=" + isTitle +
                    ", isNeedDrawLine=" + isNeedDrawLine +
                    ", title='" + title + '\'' +
                    '}';
        }
    }

    public List<VideoWrap> getVideoWraps() {
        return mVideoWraps;
    }

    public boolean isDrawLine(int position) {
        return mVideoWraps.get(position).isNeedDrawLine;
    }

    public boolean isTitle(int position) {
        return mVideoWraps.get(position).isTitle;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public Video getVideo(int position) {
        return mVideoWraps.get(position).mVideo;
    }

    private List<VideoWrap> transformVideo(List<Video> videos) {
        HashMap<String, List<Video>> map = new HashMap<>();
        for (Video video : videos) {
            long createTime = video.createTime;
            String time = DateUtils.getFormaterTime(createTime, "yyyy年MM月dd日");
            if (map.get(time) != null) {
                map.get(time).add(video);
            } else {
                List<Video> mapList = new ArrayList<>();
                mapList.add(video);
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
            VideoWrap wrapTitle = new VideoWrap();
            wrapTitle.isTitle = true;
            wrapTitle.title = key;
            mVideoWraps.add(wrapTitle);
            List<Video> videoList = map.get(key);
            for (int i = 0; i < videoList.size(); i++) {
                Video video = videoList.get(i);
                VideoWrap wrapVideo = new VideoWrap();
                wrapVideo.mVideo = video;
                mVideoWraps.add(wrapVideo);
            }
        }
        return mVideoWraps;
    }
}
