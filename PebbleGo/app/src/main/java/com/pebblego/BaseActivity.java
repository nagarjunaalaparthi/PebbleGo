package com.pebblego;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arjun.
 */
public class BaseActivity extends AppCompatActivity {
    Dialog progressDialog = null;


    public void showProgressDialogue(boolean isCancelable) {
        if (progressDialog == null) {
            progressDialog = new Dialog(BaseActivity.this, R.style.CustomDialogTheme);
            progressDialog.setContentView(R.layout.dialogue_progress);
        }
        progressDialog.setCancelable(isCancelable);
        progressDialog.setCanceledOnTouchOutside(isCancelable);
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    public void dismissProgressDialogue() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
