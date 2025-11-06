package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Legacy entry point kept for backward compatibility. It immediately forwards
 * to {@link StaffMainActivity} where the actual staff experience now lives.
 */
public class StaffActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, StaffMainActivity.class);
        intent.putExtras(getIntent());
        startActivity(intent);
        finish();
    }
}
