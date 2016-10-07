package com.neolab.firebasedemo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by LucLe on 06/10/2016.
 */

public class User implements Parcelable {

    @SerializedName("id")
    private String mUserId;

    @SerializedName("user_name")
    private String mUserName;

    @SerializedName("email")
    private String mEmail;

    @SerializedName("photo_url")
    private String mPhotoUrl;

    @SerializedName("fcm_token")
    private String mFcmToken;

    public User() {
    }

    public User(String userId, String userName, String email, String photoUrl, String fcmToken) {
        mUserId = userId;
        mUserName = userName;
        mEmail = email;
        mPhotoUrl = photoUrl;
        mFcmToken = fcmToken;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        mPhotoUrl = photoUrl;
    }

    public String getFcmToken() {
        return mFcmToken;
    }

    public void setFcmToken(String fcmToken) {
        mFcmToken = fcmToken;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUserId);
        dest.writeString(this.mUserName);
        dest.writeString(this.mEmail);
        dest.writeString(this.mPhotoUrl);
        dest.writeString(this.mFcmToken);
    }

    protected User(Parcel in) {
        this.mUserId = in.readString();
        this.mUserName = in.readString();
        this.mEmail = in.readString();
        this.mPhotoUrl = in.readString();
        this.mFcmToken = in.readString();
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
