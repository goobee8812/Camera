package com.cloudring.magic.camera.photograph.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by 羊柯 on 2017/5/31.
 */

public class PhotoEntity implements Serializable, Parcelable {
    public String url;
    public int isSelect;
    public String time;//具现化时间数据
    public Date timeDate;//时间date数据
    public Calendar calendar;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeInt(this.isSelect);
    }

    public PhotoEntity() {
    }

    protected PhotoEntity(Parcel in) {
        this.url = in.readString();
        this.isSelect = in.readInt();
    }

    public static final Creator<PhotoEntity> CREATOR = new Creator<PhotoEntity>() {
        @Override
        public PhotoEntity createFromParcel(Parcel source) {
            return new PhotoEntity(source);
        }

        @Override
        public PhotoEntity[] newArray(int size) {
            return new PhotoEntity[size];
        }
    };

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SelectPhotoEntity{");
        sb.append("url='").append(url).append('\'');
        sb.append(", isSelect=").append(isSelect);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {//使用hashcode和equals方法防止重复
        if (url != null) return url.hashCode();
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PhotoEntity) {
            return o.hashCode() == this.hashCode();
        }
        return super.equals(o);
    }
}
