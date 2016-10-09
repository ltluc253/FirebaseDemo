package com.neolab.firebasedemo;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends BaseActivity {

    private static final String TAG = "RegisterWithEmail";

    private EditText mEdtEmail, mEdtPassword, mEdtConfirmPass, mEdtDisplayName;
    private Button mBtnRegister;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initialized();
    }

    @Override
    protected void initViews() {
        mEdtEmail = (EditText) findViewById(R.id.edt_email_address);
        mEdtPassword = (EditText) findViewById(R.id.edt_password);
        mEdtConfirmPass = (EditText) findViewById(R.id.edt_confirm_password);
        mEdtDisplayName = (EditText) findViewById(R.id.edt_display_name);
        mBtnRegister = (Button) findViewById(R.id.btn_register);
    }

    @Override
    protected void initData() {
        mAuth = FirebaseAuth.getInstance();
/*        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                Log.d(TAG, "Authentication status " + (user != null ? "logged in" : "fail"));
            }
        };
        mAuth.addAuthStateListener(mAuthStateListener);*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void setViewListeners() {
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEdtEmail.getText().toString().trim();
                if (email.isEmpty() || !validateEmail(email)) {
                    Toast.makeText(RegisterActivity.this, "Please input email", Toast.LENGTH_SHORT).show();
                    return;
                }
                String password = mEdtPassword.getText().toString().trim();
                if (password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please input password", Toast.LENGTH_SHORT).show();
                    return;
                }
                String passwordConfirm = mEdtConfirmPass.getText().toString().trim();
                if (password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please input password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!passwordConfirm.equals(password)) {
                    Toast.makeText(RegisterActivity.this, "Password does not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                String name = mEdtDisplayName.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please input name", Toast.LENGTH_SHORT).show();
                    return;
                }
                registerAccount(email, password, name);
            }
        });
    }

    private void registerAccount(String email, String password, final String name) {
        showProgressDialog();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "Register complete with status " + (task.isSuccessful() ? "success" : "fail"));
                        if (!task.isSuccessful()) {
                            hideProgressDialog();
                            Toast.makeText(RegisterActivity.this, "Register account fail please try again", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            updateName(name);
                        }
                        sendVerificationEmail();
                    }
                });
    }

    private void updateName(String name) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Log.d(TAG, "Update profile");
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "Update user name " + (task.isSuccessful() ? "success" : "fail"));
                        }
                    });
        }
    }

    private void sendVerificationEmail() {
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Log.d(TAG, "Send email verification");
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Email sent.");
                                Toast.makeText(RegisterActivity.this, "An Verification email has been sent to you.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Send Verification email error", Toast.LENGTH_SHORT).show();
                            }
                            mAuth.signOut();
                            hideProgressDialog();
                            finish();
                        }
                    });
        } else {
            hideProgressDialog();
            Toast.makeText(RegisterActivity.this, "Send Verification email error", Toast.LENGTH_SHORT).show();
        }
    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }
}
