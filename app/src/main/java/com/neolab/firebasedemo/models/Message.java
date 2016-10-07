package com.neolab.firebasedemo.models;

/**
 * Created by LucLe on 06/10/2016.
 */

public class Message {

    private String mId;

    private String mUserId;

    private User mUser;

    private String mMessage;

    public Message() {
    }

    public Message(String id, String userId, String message) {
        mId = id;
        mUserId = userId;
        mMessage = message;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getPhotoUrl () {
        if (mUser != null) {
            return mUser.getPhotoUrl();
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Message) {
            return mId.equals(((Message) obj).mId);
        }
        return false;
    }
}
