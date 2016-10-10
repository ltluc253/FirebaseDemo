package com.neolab.firebasedemo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.Window;

import com.google.firebase.auth.FirebaseAuth;


public abstract class BaseActivity extends AppCompatActivity {

    private Dialog mProgressDialog;

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
            mProgressDialog = buildProgressDialog();
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private Dialog buildProgressDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_progress);
        dialog.setCancelable(false);
        return dialog;
    }
}
