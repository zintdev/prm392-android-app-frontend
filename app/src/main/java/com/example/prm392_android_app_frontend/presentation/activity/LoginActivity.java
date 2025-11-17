package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.prm392_android_app_frontend.storage.TokenStore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmailOrUsername;
    private EditText edtPassword;
    private Button btnLogin;
    private ProgressBar progress;

    private AuthViewModel viewModel;
    private boolean isSubmitting = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        edtEmailOrUsername = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.progress);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);


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
    }

    private void doLogin() {
        if (isSubmitting) return;

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

        TokenStore.saveLogin(
                this,
                body.getToken(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
        Intent i;
        if (TokenStore.isAdmin(this)) {
            i = new Intent(this, AdminMainActivity.class);
        } else if (TokenStore.isStaff(this)) {
            i = new Intent(this, StaffMainActivity.class);
        } else {
            i = new Intent(this, MainActivity.class);
        }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        toast("Đăng nhập thành công");
        finish();
    }

    private boolean isAdmin(String role) {
        if (role == null) return false;
        String r = role.trim().toUpperCase();
        return r.equals("ADMIN");
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

    // ✅ Hàm tiện ích bật/tắt loading + khóa nút
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
