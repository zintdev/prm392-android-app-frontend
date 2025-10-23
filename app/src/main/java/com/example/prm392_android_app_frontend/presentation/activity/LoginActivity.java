package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.login.LoginResponse;
import com.example.prm392_android_app_frontend.presentation.viewmodel.AuthViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmailOrUsername;
    private EditText edtPassword;
    private Button btnLogin;
    private ImageButton btnBackToMain;
    private ProgressBar progress;

    private AuthViewModel viewModel;
    private boolean isSubmitting = false; // chống double-click

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        edtEmailOrUsername = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnBackToMain = findViewById(R.id.btnBackToMain);
        progress = findViewById(R.id.progress);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observe state từ ViewModel
        viewModel.getLoginState().observe(this, res -> {
            if (res == null) return;
            switch (res.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    isSubmitting = false;
                    onLoginSuccess(res.getData());
                    break;
                case ERROR:
                    setLoading(false);
                    isSubmitting = false;
                    toast(orElse(res.getMessage(), "Đăng nhập thất bại."));
                    break;
            }
        });

        btnLogin.setOnClickListener(v -> doLogin());

        findViewById(R.id.tvSignUp).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        btnBackToMain.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void doLogin() {
        if (isSubmitting) return; // tránh bấm liên tục

        String usernameOrEmail = edtEmailOrUsername.getText().toString().trim();
        String pass = edtPassword.getText().toString();

        if (TextUtils.isEmpty(usernameOrEmail)) {
            edtEmailOrUsername.setError("Nhập username/email");
            edtEmailOrUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            edtPassword.setError("Nhập mật khẩu");
            edtPassword.requestFocus();
            return;
        }

        hideKeyboard();
        isSubmitting = true;
        setLoading(true);

        // ✅ Gọi ViewModel (thay cho api.login(...))
        viewModel.login(usernameOrEmail, pass);
    }

    private void onLoginSuccess(LoginResponse body) {
        if (body == null || TextUtils.isEmpty(body.getToken())) {
            toast("Thiếu token trong phản hồi.");
            return;
        }
        LoginResponse.User user = body.getUser();
        if (user == null) {
            toast("Phản hồi không có thông tin người dùng.");
            return;
        }

        // Lưu token + thông tin user
        TokenStore.saveLogin(
                this,
                body.getToken(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );

        // Điều hướng về Main
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void setLoading(boolean isLoading) {
        if (progress != null) progress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (btnLogin != null) btnLogin.setEnabled(!isLoading);
        if (edtEmailOrUsername != null) edtEmailOrUsername.setEnabled(!isLoading);
        if (edtPassword != null) edtPassword.setEnabled(!isLoading);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }
    private String orElse(String v, String fallback) { return TextUtils.isEmpty(v) ? fallback : v; }
}
