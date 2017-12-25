package com.magic.photo.photoviewlibrary.entity;

import java.io.Serializable;
import java.util.List;

/**
 * description:
 * Created by luohaijun on 2016/10/20.
 */

public class FamilyPhoto implements Serializable{

    private String userName;
    private String userHead;//头像
    private List<String> photoUrls;
    private String date;
    private String address;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserHead() {
        return userHead;
    }

    public void setUserHead(String userHead) {
        this.userHead = userHead;
    }

    public List<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
