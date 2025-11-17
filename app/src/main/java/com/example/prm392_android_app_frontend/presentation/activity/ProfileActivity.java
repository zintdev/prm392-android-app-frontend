package com.example.prm392_android_app_frontend.presentation.activity;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.snackbar.Snackbar;

public class ProfileActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        edtName  = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);

        // Preload từ TokenStore
        edtName.setText(TokenStore.getUsername(this));
        edtEmail.setText(TokenStore.getEmail(this));

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            String newUsername = edtName.getText().toString().trim();
            String newEmail    = edtEmail.getText().toString().trim();

            // Lấy các giá trị còn lại từ TokenStore để không bị mất
            String token = TokenStore.getToken(this);
            int userId   = TokenStore.getUserId(this);
            String role  = TokenStore.getRole(this);

            // Lưu lại đầy đủ theo chữ ký mới của TokenStore
            TokenStore.saveLogin(
                    this,
                    token,
                    userId,
                    newUsername,
                    newEmail,
                    role
            );

            Snackbar.make(v, "Đã lưu hồ sơ (demo)", Snackbar.LENGTH_SHORT).show();
            finish();
        });
    }
}
