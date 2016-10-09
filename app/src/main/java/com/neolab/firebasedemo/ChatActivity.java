package com.neolab.firebasedemo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.neolab.firebasedemo.handler.UserHandler;
import com.neolab.firebasedemo.models.Message;
import com.neolab.firebasedemo.models.User;
import com.neolab.firebasedemo.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private static final String NODE_MESSAGE = "public_message";
    private static final String EVENT_SEND_MESSAGE = "Send Message";
    private static final String EVENT_UPDATE_MESSAGE = "Update Message";
    private static final int REQUEST_INVITE = 101;
    private static final String TAG = "ChatActivity";

    private RecyclerView mRvMessage;
    private EditText mEdtMessage;
    private Button mBtnSend;
    private MessageAdapter mMessageAdapter;
    private Message mUpdateMessage;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mMessageDb;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private UserHandler mUserHandler;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else {
            initialized();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite_menu:
                sendInvitation();
                return true;
            case R.id.crash_menu:
                causeCrash();
                return true;
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mFirebaseUser = null;
                // Unsubcribe topic after log out
                FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.FRIENDLY_ENGAGE_TOPIC);
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void initViews() {
        mRvMessage = (RecyclerView) findViewById(R.id.rv_message_list);
        mRvMessage.setLayoutManager(new LinearLayoutManager(this));
        mMessageAdapter = new MessageAdapter(this, mFirebaseUser.getUid());
        mRvMessage.setAdapter(mMessageAdapter);
        mEdtMessage = (EditText) findViewById(R.id.edt_message);
        mBtnSend = (Button) findViewById(R.id.btn_send);
        mUserHandler.setListener(mMessageAdapter);
    }

    @Override
    protected void initData() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mUserHandler = new UserHandler();
        initMessageList();
    }

    @Override
    protected void setViewListeners() {
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEdtMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    String messageId;
                    if (mUpdateMessage != null) {
                        messageId = mUpdateMessage.getId();
                        mUpdateMessage = null;
                        mBtnSend.setText("Send");
                        mFirebaseAnalytics.logEvent(EVENT_UPDATE_MESSAGE, null);
                    } else {
                        messageId = mDatabaseReference.child(NODE_MESSAGE).push().getKey();
                        mFirebaseAnalytics.logEvent(EVENT_SEND_MESSAGE, null);
                    }
                    Message messageSend = new Message(messageId, mFirebaseUser.getUid(), message);
                    mDatabaseReference.child(NODE_MESSAGE).child(messageId).setValue(messageSend);
                }
                mEdtMessage.setText("");
            }
        });

        mEdtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String message = mEdtMessage.getText().toString().trim();
                mBtnSend.setEnabled(!message.isEmpty());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Use Firebase Measurement to log that invitation was sent.
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_sent");

                // Check how many invitations were sent and log.
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                Log.d(TAG, "Invitations sent: " + ids.length);
            } else {
                // Use Firebase Measurement to log that invitation was not sent
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_not_sent");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, payload);

                // Sending failed or it was canceled, show failure message to the user
                Log.d(TAG, "Failed to send invitation.");
            }
        }
    }

    private void initMessageList() {
        mMessageDb = FirebaseDatabase.getInstance().getReference().child(NODE_MESSAGE);
        mMessageDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    message.setUser(mUserHandler.getUser(message.getUserId()));
                    mMessageAdapter.addMessage(message);
                    mRvMessage.scrollToPosition(mMessageAdapter.getItemCount());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    message.setUser(mUserHandler.getUser(message.getUserId()));
                    mMessageAdapter.updateMessage(message);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    mMessageAdapter.remove(message);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void editMessage(Message message) {
        mUpdateMessage = message;
        mBtnSend.setText("Update");
        mEdtMessage.setText(message.getMessage());
    }

    private void causeCrash() {
        throw new RuntimeException("Test for crashing report");
    }

    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class MessageHolder extends RecyclerView.ViewHolder {

        private ImageView mImgAvatar;
        private TextView mTvMessage;
        private Message mMessage;

        public MessageHolder(View itemView) {
            super(itemView);
            mImgAvatar = (ImageView) itemView.findViewById(R.id.img_user_avatar);
            mTvMessage = (TextView) itemView.findViewById(R.id.tv_message);
            mTvMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String currentUserId = mFirebaseUser.getUid();
                    if (!mMessage.getUserId().equals(currentUserId)) return false;
                    String[] options = {"Edit message", "Delete message"};
                    new AlertDialog.Builder(ChatActivity.this)
                            .setTitle(R.string.app_name)
                            .setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        editMessage(mMessage);
                                    } else {
                                        mMessageDb.child(mMessage.getId()).removeValue();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                }
            });
        }

        public void setMessage(Message message) {
            mMessage = message;
        }
    }

    private class MessageAdapter extends RecyclerView.Adapter<MessageHolder> implements UserHandler.OnUserListener {

        private static final int TYPE_OTHER = 0;
        private static final int TYPE_ME = 1;

        private List<Message> mMessageList;
        private Context mContext;
        private String mCurrentUserId;

        public MessageAdapter(Context context, String currentUserId) {
            mContext = context;
            mCurrentUserId = currentUserId;
            mMessageList = new ArrayList<>();
        }

        public void addMessage(Message message) {
            if (!mMessageList.contains(message)) {
                mMessageList.add(message);
                notifyItemInserted(mMessageList.size());
            }
        }

        public void remove(Message message) {
            int index = mMessageList.indexOf(message);
            if (index != -1) {
                mMessageList.remove(index);
                notifyItemRemoved(index);
            }
        }

        public void updateMessage(Message message) {
            int index = mMessageList.indexOf(message);
            if (index != -1) {
                mMessageList.set(index, message);
                notifyItemChanged(index);
            }
        }

        @Override
        public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layoutId = viewType == TYPE_ME ? R.layout.item_message_from_me : R.layout.item_message_from_others;
            View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
            return new MessageHolder(view);
        }

        @Override
        public void onBindViewHolder(MessageHolder holder, int position) {
            Message message = getItem(position);
            ImageUtils.loadThumbnail(mContext, holder.mImgAvatar, message.getPhotoUrl(), R.drawable.ic_account_circle_black_36dp);
            holder.mTvMessage.setText(message.getMessage());
            holder.setMessage(message);
        }

        @Override
        public int getItemCount() {
            return mMessageList.size();
        }

        @Override
        public int getItemViewType(int position) {
            Message message = getItem(position);
            return mCurrentUserId.equals(message.getUserId()) ? TYPE_ME : TYPE_OTHER;
        }

        public Message getItem(int pos) {
            return mMessageList.get(pos);
        }

        @Override
        public void onUserInfoReady(User user) {
            if (user != null) {
                for (Message message : mMessageList) {
                    if (user.getUserId().equals(message.getUserId())) {
                        message.setUser(user);
                    }
                }
                notifyDataSetChanged();
            }
        }
    }
}
