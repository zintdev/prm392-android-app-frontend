package com.example.prm392_android_app_frontend.presentation.activity;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_android_app_frontend.data.dto.changePassword.ChangePasswordRequest;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputLayout tilOld, tilNew, tilConfirm;
    private TextInputEditText edtOld, edtNew, edtConfirm;
    private MaterialButton btnChange;
    private ProgressBar progress;


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



        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validate(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        edtOld.addTextChangedListener(watcher);
        edtNew.addTextChangedListener(watcher);
        edtConfirm.addTextChangedListener(watcher);

        btnChange.setOnClickListener(v -> submit());
    }

//    private ApiService buildApiWithAuth() {
//        String token = TokenStore.getToken(this); "
//        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
//        log.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//        OkHttpClient client = ApiClient.defaultOkHttpBuilder()
//                .addInterceptor(chain -> {
//                    return chain.proceed(
//                            chain.request().newBuilder()
//                                    .addHeader("Authorization", "Bearer " + token)
//                                    .build()
//                    );
//                })
//                .addInterceptor(log)
//                .build();
//
//        return ApiClient.retrofit(client).create(ApiService.class);
//    }

    private void validate() {
        String oldPw = text(edtOld);
        String newPw = text(edtNew);
        String confirm = text(edtConfirm);

        tilOld.setError(null);
        tilNew.setError(null);
        tilConfirm.setError(null);

        boolean ok = true;

        if (oldPw.isEmpty()) {
            tilOld.setError("Vui lòng nhập mật khẩu cũ");
            ok = false;
        }


        if (newPw.length() < 8 || !newPw.matches(".*[A-Za-z].*") || !newPw.matches(".*\\d.*")) {
            tilNew.setError("Ít nhất 8 ký tự và gồm chữ & số");
            ok = false;
        } else if (newPw.equals(oldPw)) {
            tilNew.setError("Mật khẩu mới không được trùng mật khẩu cũ");
            ok = false;
        }

        if (!confirm.equals(newPw)) {
            tilConfirm.setError("Xác nhận không khớp");
            ok = false;
        }

        btnChange.setEnabled(ok);
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnChange.setEnabled(!loading);
        edtOld.setEnabled(!loading);
        edtNew.setEnabled(!loading);
        edtConfirm.setEnabled(!loading);
    }

    private void submit() {
        validate();
        if (!btnChange.isEnabled()) return;

        setLoading(true);

        ChangePasswordRequest body = new ChangePasswordRequest(
                text(edtOld), text(edtNew)
        );

//        api.changePassword(body).enqueue(new Callback<Void>() {
//            @Override public void onResponse(Call<Void> call, Response<Void> resp) {
//                setLoading(false);
//                if (resp.isSuccessful()) {
//                    Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
//                    finish();
//                } else if (resp.code() == 400 || resp.code() == 409 || resp.code() == 422) {
//                    tilOld.setError("Mật khẩu cũ không đúng");
//                } else {
//                    Toast.makeText(ChangePasswordActivity.this, "Lỗi máy chủ ("+resp.code()+")", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override public void onFailure(Call<Void> call, Throwable t) {
//                setLoading(false);
//                Toast.makeText(ChangePasswordActivity.this, "Không thể kết nối. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
