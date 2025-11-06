package com.example.prm392_android_app_frontend.presentation.activity;

import android.os.Bundle;
import android.util.Log;
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

import java.io.IOException;

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
        userApi = ApiClient.getAuthClient(this).create(UserApi.class);
        userId = TokenStore.getUserId(this);
        btnChange.setOnClickListener(v -> onChangeClick());
    }

    private void onChangeClick() {
        String oldPw = text(edtOld);
        String newPw = text(edtNew);
        String cfPw  = text(edtConfirm);
        if (oldPw.isEmpty()) {
            edtOld.setError("Vui lòng nhập mật khẩu hiện tại");
            edtOld.requestFocus();
            return;
        }
        if (newPw.length() < 6) {
            edtNew.setError("Mật khẩu mới phải từ 6 ký tự trở lên");
            edtNew.requestFocus();
            return;
        }
        if (!newPw.equals(cfPw)) {
            edtConfirm.setError("Xác nhận mật khẩu không khớp");
            edtConfirm.requestFocus();
            return;
        }
        if (newPw.equals(oldPw)) {
            edtNew.setError("Mật khẩu mới phải khác mật khẩu hiện tại");
            edtNew.requestFocus();
            return;
        }

        doChangePassword(oldPw, newPw);
    }

    private void doChangePassword(String oldPw, String newPw) {
        Log.d("ChangePw", "Bắt đầu đổi mật khẩu - userId=" + userId);

        if (userId <= 0) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        ChangePasswordRequest request = new ChangePasswordRequest(oldPw, newPw);
        Log.d("ChangePw", "Request created: " + request);

        Call<Void> call = userApi.changePassword(userId, request);
        Log.d("ChangePw", "Call object: " + (call == null ? "NULL" : "OK"));

        if (call == null) {
            showLoading(false);
            Toast.makeText(this, "Lỗi: Không thể tạo API call", Toast.LENGTH_SHORT).show();
            return;
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                Log.d("ChangePw", "onResponse called - code: " + res.code());
                showLoading(false);

                if (res.isSuccessful()) {
                    Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (res.code() == 400) {
                    Toast.makeText(ChangePasswordActivity.this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                } else if (res.code() == 401) {
                    Toast.makeText(ChangePasswordActivity.this, "Hết phiên đăng nhập. Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = res.errorBody() != null ? res.errorBody().string() : "No error body";
                        Log.e("ChangePw", "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("ChangePw", "Can't read error body", e);
                    }
                    Toast.makeText(ChangePasswordActivity.this, "Lỗi máy chủ (HTTP " + res.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ChangePw", "onFailure called", t);
                showLoading(false);
                Toast.makeText(ChangePasswordActivity.this,
                        "Lỗi kết nối: " + (t.getMessage() == null ? "Unknown error" : t.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });

        Log.d("ChangePw", "enqueue() completed");
    }
    //    private void doChangePassword(String oldPw, String newPw) {
//        Log.d("ChangePw", "Bắt đầu đổi mật khẩu - userId=" + userId);
//        showLoading(true);
//        retrofit2.Retrofit retrofit = ApiClient.getAuthClient(this);
//        okhttp3.RequestBody body = okhttp3.RequestBody.create(
//                okhttp3.MediaType.parse("application/json"),
//                "{ \"oldPassword\": \"" + oldPw + "\", \"newPassword\": \"" + newPw + "\" }"
//        );
//        okhttp3.Request request = new okhttp3.Request.Builder()
//                .url("https://plowable-nonlevel-sharie.ngrok-free.dev/api/users/" + userId)
//                .put(body)
//                .addHeader("Authorization", "Bearer " + TokenStore.getToken(this))
//                .addHeader("Content-Type", "application/json")
//                .build();
//        retrofit.callFactory().newCall(request).enqueue(new okhttp3.Callback() {
//            @Override
//            public void onFailure(okhttp3.Call call, IOException e) {
//                runOnUiThread(() -> {
//                    showLoading(false);
//                    Toast.makeText(ChangePasswordActivity.this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//            }
//            @Override
//            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
//                runOnUiThread(() -> {
//                    showLoading(false);
//                    if (response.isSuccessful()) {
//                        Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
//                        finish();
//                    } else if (response.code() == 400) {
//                        Toast.makeText(ChangePasswordActivity.this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
//                    } else if (response.code() == 401) {
//                        Toast.makeText(ChangePasswordActivity.this, "Hết phiên đăng nhập, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(ChangePasswordActivity.this, "Lỗi máy chủ (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });
//    }
    private void showLoading(boolean b) {
        progress.setVisibility(b ? View.VISIBLE : View.GONE);
        // Để tránh double-click khi đang gọi API, tạm khoá nút rồi mở lại khi xong
        btnChange.setEnabled(!b);
    }

    private static String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
