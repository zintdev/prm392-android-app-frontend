package com.example.prm392_android_app_frontend.presentation.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.changePassword.ChangePasswordRequest;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.UserApi;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputLayout tilOld, tilNew, tilConfirm;
    private TextInputEditText edtOld, edtNew, edtConfirm;
    private MaterialButton btnChange;
    private ProgressBar progress;

    private UserApi userApi;
    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilOld = findViewById(R.id.tilOld);
        tilNew = findViewById(R.id.tilNew);
        tilConfirm = findViewById(R.id.tilConfirm);
        edtOld = findViewById(R.id.edtOld);
        edtNew = findViewById(R.id.edtNew);
        edtConfirm = findViewById(R.id.edtConfirm);
        btnChange = findViewById(R.id.btnChange);
        progress = findViewById(R.id.progress);

        // Nếu trong XML lỡ để enabled="false" thì bật lại:
        btnChange.setEnabled(true);

        userId = TokenStore.getUserId(this);
        userApi = ApiClient.getAuthClient(this).create(UserApi.class);

        btnChange.setOnClickListener(v -> onChangeClick());
    }

    private void onChangeClick() {
        String oldPw = text(edtOld);
        String newPw = text(edtNew);
        String cfPw  = text(edtConfirm);

        // ✅ Chỉ cần rule: mật khẩu mới < 6 thì không cho đổi
        if (newPw.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        // (khuyến nghị) Kiểm tra confirm cho an toàn
        if (!newPw.equals(cfPw)) {
            Toast.makeText(this, "Xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi API
        doChangePassword(oldPw, newPw);
    }

    private void doChangePassword(String oldPw, String newPw) {
        showLoading(true);
        userApi.changePassword(userId, new ChangePasswordRequest(oldPw, newPw))
                .enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> call, Response<Void> res) {
                        showLoading(false);
                        if (res.isSuccessful()) {
                            Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        } else if (res.code() == 400) {
                            // tuỳ backend, có thể là old password sai
                            Toast.makeText(ChangePasswordActivity.this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                        } else if (res.code() == 401) {
                            Toast.makeText(ChangePasswordActivity.this, "Hết phiên đăng nhập. Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                            // điều hướng sang màn đăng nhập nếu cần
                        } else {
                            Toast.makeText(ChangePasswordActivity.this, "Lỗi máy chủ (HTTP " + res.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<Void> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(ChangePasswordActivity.this, "Lỗi kết nối: " + (t.getMessage()==null?"":t.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean b) {
        progress.setVisibility(b ? View.VISIBLE : View.GONE);
        // Để tránh double-click khi đang gọi API, tạm khoá nút rồi mở lại khi xong
        btnChange.setEnabled(!b);
    }

    private static String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
