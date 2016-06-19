package com.pebblego;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Arjun.
 */
public class SettingsAtivity extends BaseActivity {
    private EditText goalCount, age, weight, height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        goalCount = (EditText) findViewById(R.id.steps_count);
        age = (EditText) findViewById(R.id.age);
        weight = (EditText) findViewById(R.id.weight);
        height = (EditText) findViewById(R.id.height);
        setDatatoViews();
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDataToSharedPreferences();
            }
        });
    }

    private void setDatatoViews() {
        goalCount.setText(SharedPreferenceUtils.readString(SettingsAtivity.this, "count", ""));
        weight.setText(SharedPreferenceUtils.readString(SettingsAtivity.this, "weight", ""));
        height.setText(SharedPreferenceUtils.readString(SettingsAtivity.this, "height", ""));
        age.setText(SharedPreferenceUtils.readString(SettingsAtivity.this, "age", ""));
    }

    private void setDataToSharedPreferences() {
        if (!TextUtils.isEmpty(goalCount.getText().toString()) && !TextUtils.isEmpty(age.getText().toString()) && !TextUtils.isEmpty(height.getText().toString()) && !TextUtils.isEmpty(weight.getText().toString())) {
            SharedPreferenceUtils.writeString(SettingsAtivity.this, "age", age.getText().toString());
            SharedPreferenceUtils.writeString(SettingsAtivity.this, "count", goalCount.getText().toString());
            SharedPreferenceUtils.writeString(SettingsAtivity.this, "weight", weight.getText().toString());
            SharedPreferenceUtils.writeString(SettingsAtivity.this, "height", height.getText().toString());
            finish();
        } else {
            Toast.makeText(SettingsAtivity.this, "please fill all details", Toast.LENGTH_LONG).show();
        }
    }
}
