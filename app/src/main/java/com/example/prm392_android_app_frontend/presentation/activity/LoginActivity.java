package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.AuthApi;
import com.example.prm392_android_app_frontend.data.remote.ErrorUtils;
import com.example.prm392_android_app_frontend.data.dto.ApiError;
import com.example.prm392_android_app_frontend.data.dto.login.LoginRequest;
import com.example.prm392_android_app_frontend.data.dto.login.LoginResponse;
import com.example.prm392_android_app_frontend.presentation.viewmodel.AuthViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmailOrUsername;
    private EditText edtPassword;
    private Button btnLogin;
    private ImageButton btnBackToMain;
    private ProgressBar progress;

    private AuthApi api;
    private Retrofit retrofitNoAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        edtEmailOrUsername = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnBackToMain = findViewById(R.id.btnBackToMain);
        progress = findViewById(R.id.progress);

        retrofitNoAuth = ApiClient.get();
        api = retrofitNoAuth.create(AuthApi.class);

        btnLogin.setOnClickListener(v -> doLogin());
        findViewById(R.id.tvSignUp).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
        btnBackToMain.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void doLogin() {
        String usernameOrEmail = edtEmailOrUsername.getText().toString().trim();
        String pass = edtPassword.getText().toString();

        if (TextUtils.isEmpty(usernameOrEmail)) {
            edtEmailOrUsername.setError("Nhập username/email");
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            edtPassword.setError("Nhập mật khẩu");
            return;
        }

        setLoading(true);
        api.login(new LoginRequest(usernameOrEmail, pass)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> resp) {
                setLoading(false); //

                if (resp.isSuccessful()) {
                    LoginResponse body = resp.body();
                    if (body == null || TextUtils.isEmpty(body.getToken())) {
                        toast("Thiếu token trong phản hồi.");
                        return;
                    }

                    // ✅ Lưu đầy đủ thông tin user và token
                    LoginResponse.User user = body.getUser();
                    if (user != null) {
                        TokenStore.saveLogin(
                                LoginActivity.this,
                                body.getToken(),
                                user.getId(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getRole()
                        );
                    } else {
                        toast("Phản hồi không có thông tin người dùng.");
                        return;
                    }

                    // ✅ Chuyển sang trang chính
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }

                handleHttpError(resp);
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false); // 👈 tắt loading khi lỗi mạng
                toast("Không thể kết nối máy chủ: " + t.getMessage());
            }
        });
    }

    private void handleHttpError(Response<?> resp) {
        int code = resp.code();
        ApiError apiError = ErrorUtils.parseError(retrofitNoAuth, resp);
        String apiMsg = (apiError != null && !TextUtils.isEmpty(apiError.getMessage()))
                ? apiError.getMessage() : null;

        String message;
        switch (code) {
            case 400: message = orElse(apiMsg, "Yêu cầu không hợp lệ (400)."); break;
            case 401: message = orElse(apiMsg, "Sai tài khoản hoặc mật khẩu (401)."); break;
            case 403: message = orElse(apiMsg, "Bạn không có quyền truy cập."); break;
            case 404: message = orElse(apiMsg, "Mất kết nối. Vui lòng thử lại sau"); break;
            case 500: message = orElse(apiMsg, "Lỗi máy chủ (500)."); break;
            default:  message = orElse(apiMsg, "Lỗi không xác định (HTTP " + code + ").");
        }
        toast(message);
    }
    private void setLoading(boolean isLoading) {
        if (progress != null) {
            progress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnLogin != null) {
            btnLogin.setEnabled(!isLoading);
        }
        if (edtEmailOrUsername != null) {
            edtEmailOrUsername.setEnabled(!isLoading);
        }
        if (edtPassword != null) {
            edtPassword.setEnabled(!isLoading);
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private String orElse(String v, String fallback) {
        return TextUtils.isEmpty(v) ? fallback : v;
    }
}
