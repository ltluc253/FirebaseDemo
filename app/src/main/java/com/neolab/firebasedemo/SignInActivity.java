package com.neolab.firebasedemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.neolab.firebasedemo.handler.UserHandler;
import com.neolab.firebasedemo.models.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.neolab.firebasedemo.R.id.edt_password;

public class SignInActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private SignInButton mSignInButton;
    private Button mBtnRegister, mBtnLogin;
    private EditText mEdtEmail, mEdtPassword;

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initialized();
    }

    @Override
    protected void initViews() {
        // Assign fields
        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mBtnRegister = (Button) findViewById(R.id.btn_register);
        mEdtEmail = (EditText) findViewById(R.id.edt_email_address);
        mEdtPassword = (EditText) findViewById(R.id.edt_password);
        mBtnLogin = (Button) findViewById(R.id.btn_log_in);

    }

    @Override
    protected void initData() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void setViewListeners() {
        // Set click listeners
        mSignInButton.setOnClickListener(this);
        mBtnRegister.setOnClickListener(this);
        mBtnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.btn_register:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.btn_log_in:
                String email = mEdtEmail.getText().toString().trim();
                if (email.isEmpty() || !validateEmail(email)) {
                    Toast.makeText(this, "Please input email", Toast.LENGTH_SHORT).show();
                    return;
                }
                String password = mEdtPassword.getText().toString().trim();
                if (password.isEmpty()) {
                    Toast.makeText(this, "Please input password", Toast.LENGTH_SHORT).show();
                    return;
                }
                loginEmail(email, password);
                break;
            default:
                return;
        }
    }

    private void loginEmail(String email, String password) {
        showProgressDialog();
        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "Log in task " + (task.isSuccessful() ? "success" : "fail"));
                        if (task.isSuccessful()) {
                            loginSuccess();
                        } else {
                            Toast.makeText(SignInActivity.this, "Log in fail " + (task.getException() !=  null ? task.getException().getMessage() : ", please try again"), Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed
                Toast.makeText(this, "Login fail: " + result.getStatus(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            loginSuccess();
                        }
                    }
                });
    }

    private void loginSuccess() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            if (user.isEmailVerified()) {
                String fcmToken = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).getString(Constants.PREF_FCM_TOKEN, null);
                UserHandler handler = new UserHandler();
                String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
                handler.addUser(new User(user.getUid(), user.getDisplayName(), user.getEmail(), photoUrl, fcmToken));
                // Subcribe topic when login
                FirebaseMessaging.getInstance().subscribeToTopic(Constants.FRIENDLY_ENGAGE_TOPIC);
                startActivity(new Intent(SignInActivity.this, ChatActivity.class));
                finish();
            } else {
                Toast.makeText(this, "You have to confirm verification email before log in", Toast.LENGTH_SHORT).show();
                mFirebaseAuth.signOut();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }
}