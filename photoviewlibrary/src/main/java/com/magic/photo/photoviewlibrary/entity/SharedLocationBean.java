package com.magic.photo.photoviewlibrary.entity;

import java.util.List;

/**
 * description:
 * Created by luohaijun on 2016/10/18.
 */
public class SharedLocationBean {

    /**
     * time : 2016-10-18 11:05:35
     * locType : 161
     * Country : 中国
     * citycode : 340
     * city : 深圳市
     * addr : 中国广东省深圳市南山区新西路5号
     * locationdescribe: 在兰光科技大厦附近
     * Poi: 兰光科技大厦;兰光科技园;彩虹科技大厦;富华科技大厦;瑞声声学科技深圳公司;
     */
    private String country;
    private String city;
    private String addr;
    private String locationdescribe;
    private List<String> locPoi;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getLocationdescribe() {
        return locationdescribe;
    }

    public void setLocationdescribe(String locationdescribe) {
        this.locationdescribe = locationdescribe;
    }

    public List<String> getLocPoi() {
        return locPoi;
    }

    public void setLocPoi(List<String> locPoi) {
        this.locPoi = locPoi;
    }
}
