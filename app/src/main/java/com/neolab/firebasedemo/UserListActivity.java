package com.neolab.firebasedemo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.neolab.firebasedemo.handler.UserHandler;
import com.neolab.firebasedemo.models.User;
import com.neolab.firebasedemo.utils.ImageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserListActivity extends BaseActivity {

    private RecyclerView mRvUserList;
    private UserAdapter mUserAdapter;

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        initialized();
    }

    @Override
    protected void initViews() {
        mRvUserList = (RecyclerView) findViewById(R.id.rv_user_list);
        mRvUserList.setLayoutManager(new LinearLayoutManager(this));
        mUserAdapter = new UserAdapter(this);
        mRvUserList.setAdapter(mUserAdapter);
    }

    @Override
    protected void initData() {
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        loadUserList();
    }

    @Override
    protected void setViewListeners() {

    }

    private void loadUserList() {
        showProgressDialog();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null && !mFirebaseUser.getUid().equals(user.getUserId())) {
                    mUserAdapter.addUser(user);
                }
                hideProgressDialog();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                hideProgressDialog();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                hideProgressDialog();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressDialog();
            }
        });
    }

    private class UserHolder extends RecyclerView.ViewHolder {

        private ImageView mImgAvatar;
        private TextView mTvUserName;

        public UserHolder(View itemView) {
            super(itemView);
            mImgAvatar = (ImageView) itemView.findViewById(R.id.img_user_avatar);
            mTvUserName = (TextView) itemView.findViewById(R.id.tv_user_name);
        }
    }

    private class UserAdapter extends RecyclerView.Adapter<UserHolder> {

        private List<User> mUserList;
        private Context mContext;

        public UserAdapter(Context context) {
            mContext = context;
            mUserList = new ArrayList<>();
        }

        public void addUser(User user) {
            mUserList.add(user);
            notifyItemInserted(mUserList.size());
        }

        @Override
        public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false);
            return new UserHolder(view);
        }

        @Override
        public void onBindViewHolder(UserHolder holder, int position) {
            User user = getItem(position);
            ImageUtils.loadThumbnail(mContext, holder.mImgAvatar, user.getPhotoUrl(), R.drawable.ic_account_circle_black_36dp);
            holder.mTvUserName.setText(user.getUserName());
        }

        @Override
        public int getItemCount() {
            return mUserList.size();
        }

        public User getItem(int pos) {
            return mUserList.get(pos);
        }
    }
}