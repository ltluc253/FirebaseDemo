package com.neolab.firebasedemo.handler;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.neolab.firebasedemo.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by LucLe on 06/10/2016.
 */

public class UserHandler {

    private static final String USER_NODE = "users";

    private DatabaseReference mDatabase;
    private HashMap<String, User> mCacheUser;
    private OnUserListener mListener;
    private List<String> mQueueIds = new ArrayList<>();

    public UserHandler() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCacheUser = new HashMap<>();
    }

    public void setListener(OnUserListener listener) {
        mListener = listener;
    }

    public void addUser(final User user) {
        mDatabase.child(USER_NODE).child(user.getUserId()).setValue(user);
    }

    public User getUser(final String userId) {
        User user = mCacheUser.get(userId);
        if (user != null) {
            return user;
        } else {
            retrieveUser(userId);
        }
        return null;
    }

    private void retrieveUser(final String userId) {
        if (!mQueueIds.contains(userId)) {
            mQueueIds.add(userId);
            Query query = mDatabase.child(USER_NODE).child(userId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user1 = dataSnapshot.getValue(User.class);
                    if (user1 != null) {
                        mCacheUser.put(user1.getUserId(), user1);
                        if (mListener != null) {
                            mListener.onUserInfoReady(user1);
                        }
                    }
                    mQueueIds.remove(userId);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    mQueueIds.remove(userId);
                }
            });
        }
    }

    public interface OnUserListener {
        public void onUserInfoReady(User user);
    }
}
