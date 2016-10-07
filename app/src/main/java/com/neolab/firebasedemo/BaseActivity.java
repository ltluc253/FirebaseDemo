package com.neolab.firebasedemo;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.Window;

import com.google.firebase.auth.FirebaseAuth;


public abstract class BaseActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    protected abstract void initViews();

    protected abstract void initData();

    protected abstract void setViewListeners();

    protected void initialized() {
        initData();
        initViews();
        setViewListeners();
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);

        }
        mProgressDialog.show();
        Window window = mProgressDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
