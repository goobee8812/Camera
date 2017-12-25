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
import com.magic.photo.photoviewlibrary.utils.DateUtils;
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
    private int count;
    private boolean isEdit; //编辑模式的标志位；
    private List<VideoWrap> mVideoWraps;
    private SparseBooleanArray mSparseBooleanArray = new SparseBooleanArray();
    private Context mContext;

    public VideoAdapter(Context context, int count) {
        this.count = count;
        this.mContext = context;
        mVideoWraps = new ArrayList<>();
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
        } else {
            rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_title_layout, parent, false);
            return new TitleViewHolder(rootView);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VideoViewHolder) {
            Glide.with(mContext).load(Uri.fromFile(new File(mVideoWraps.get(position).mVideo.path))).into(((VideoViewHolder) holder).mThumbnail);
            ((VideoViewHolder) holder).mTextView.setText(TimeUtil.formatDuration((int) mVideoWraps.get(position).mVideo.duration));
        } else if (holder instanceof TitleViewHolder) {
            ((TitleViewHolder) holder).mTitle.setText(mVideoWraps.get(position).title);
        }
    }

    @Override
    public int getItemCount() {
        return mVideoWraps.size();
    }

    @Override
    public int getItemViewType(int position) {
        VideoWrap videoWrap = mVideoWraps.get(position);
        if (videoWrap.isTitle) {
            return TITLE_TYPE;
        } else {
            return VIDEO_TYPE;
        }
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        public ImageView mThumbnail;
        public ImageView mPlayIcon;
        public TextView mTextView;

        public VideoViewHolder(View itemView) {
            super(itemView);
            mThumbnail = (ImageView) itemView.findViewById(R.id.imageView_thumbnail);
            mPlayIcon = (ImageView) itemView.findViewById(R.id.imageView_play_icon);
            mTextView = (TextView) itemView.findViewById(R.id.textView_desc);
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
