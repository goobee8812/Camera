package com.magic.photo.photoviewlibrary.entity;

/**
 * description:
 * Created by luohaijun on 2016/9/22.
 */
public class FamilyPhotoInfo {

    private String id;
    private String userId;
    private String face;
    private String name;
    private String nickName;
    private String uploadSite;
    private String uploadTime;
    private String imageUrls;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUploadSite() {
        return uploadSite;
    }

    public void setUploadSite(String uploadSite) {
        this.uploadSite = uploadSite;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }
}
