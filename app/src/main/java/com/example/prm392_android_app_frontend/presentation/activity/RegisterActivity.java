package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.register.RegisterRequest;
import com.example.prm392_android_app_frontend.data.dto.register.RegisterResponse;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.AuthApi;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editTextUsername;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextPhone;
    private Button buttonRegister;
    private View progress;

    private AuthApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        editTextPhone = findViewById(R.id.editTextPhone);

        api = ApiClient.get().create(AuthApi.class);

        buttonRegister.setOnClickListener(v -> doRegister());
        View backToLogin = findViewById(R.id.btnBackToLogin);
        if (backToLogin != null) {
            backToLogin.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        }
    }

    private void doRegister() {
        String username = safeText(editTextUsername);
        String email = safeText(editTextEmail);
        String password = safeText(editTextPassword);
        String phone = safeText(editTextPhone);

        if (TextUtils.isEmpty(username)) {
            toast("Enter username");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            toast("Enter email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            toast("Enter password");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            toast("Enter phone number");
            return;
        }

        setLoading(true);
        api.register(new RegisterRequest(username, email, password, phone)).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    toast("Register success. Please login.");
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else {
                    toast("Register failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                setLoading(false);
                toast("Network error: " + t.getMessage());
            }
        });
    }

    private void setLoading(boolean loading) {
        if (progress != null) progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (buttonRegister != null) buttonRegister.setEnabled(!loading);
        if (editTextUsername != null) editTextUsername.setEnabled(!loading);
        if (editTextEmail != null) editTextEmail.setEnabled(!loading);
        if (editTextPassword != null) editTextPassword.setEnabled(!loading);
    }

    private String safeText(TextInputEditText et) {
        return et == null || et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}


