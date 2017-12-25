package com.magic.photo.photoviewlibrary.manager;



/**
 * description:
 * Created by luohaijun on 2016/11/16.
 */

public class UserManager {

    private static UserManager mUserManager;

    private UserManager() {

    }

    public static UserManager getInstance() {
        if (mUserManager == null) {

            mUserManager = new UserManager();
        }

        return mUserManager;
    }

    public String getToken() {
//        String json = SpUtil.readString("token");
        return "";
    }

    public String getUserId() {
//        String json = SpUtil.readString("user_id");
        return "";
    }
}
