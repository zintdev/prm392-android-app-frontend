package com.example.prm392_android_app_frontend.presentation.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.UpdateUserRequest.UpdateUserRequest;
import com.example.prm392_android_app_frontend.data.dto.UserDto;
import com.example.prm392_android_app_frontend.presentation.viewmodel.UserViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText edtFullName, edtEmail, edtPhone;
    private MaterialButton btnSave;
    private ProgressBar progress;

    private UserViewModel vm;
    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail    = findViewById(R.id.edtEmail);
        edtPhone    = findViewById(R.id.edtPhone);
        btnSave     = findViewById(R.id.btnSave);
        progress    = findViewById(R.id.progress);

        userId = TokenStore.getUserId(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // ViewModel
        vm = new ViewModelProvider(this).get(UserViewModel.class);

        // Observe
        vm.loading.observe(this, isLoading -> {
            progress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnSave.setEnabled(!Boolean.TRUE.equals(isLoading));
        });

        vm.user.observe(this, this::populateForm);

        vm.updated.observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        vm.error.observe(this, msg ->
                Toast.makeText(this, msg != null ? msg : "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
        );

        // Load lần đầu
        vm.loadUser(userId);

        btnSave.setOnClickListener(v -> attemptSave());
    }

    private void populateForm(UserDto u) {
        if (u == null) return;
        edtFullName.setText(u.username != null ? u.username : "");
        edtEmail.setText(u.email != null ? u.email : "");
        edtPhone.setText(u.phoneNumber != null ? u.phoneNumber : "");
    }

    private void attemptSave() {
        String name  = textOf(edtFullName);
        String email = textOf(edtEmail);
        String phone = textOf(edtPhone);

        if (TextUtils.isEmpty(name))  {
            edtFullName.setError("Vui lòng nhập họ tên");
            edtFullName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }
        if (!isValidGmail(email)) {
            edtEmail.setError("Email phải là địa chỉ Gmail hợp lệ (ví dụ: ten@gmail.com)");
            edtEmail.requestFocus();
            return;
        }
        if (!TextUtils.isEmpty(phone) && !isValidPhoneVN(phone)) {
            edtPhone.setError("Số điện thoại không hợp lệ (VD: 0XXXXXXXXX, gồm 10 chữ số)");
            edtPhone.requestFocus();
            return;
        }

        // 4️⃣ Gọi API khi hợp lệ
        UpdateUserRequest req = new UpdateUserRequest();
        req.username = name;
        req.email = email.trim();
        req.phoneNumber = phone;
        req.role = null;

        vm.updateUser(userId, req);
    }


    private String textOf(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
    private boolean isValidGmail(String email) {
        if (email == null) return false;
        String e = email.trim().toLowerCase();
        if (!e.endsWith("@gmail.com")) return false;
        return e.matches("^[a-z0-9._%+-]{3,64}@gmail\\.com$");
    }

    private boolean isValidPhoneVN(String phone) {
        if (phone == null) return false;
        String p = phone.trim();
        return p.matches("^0\\d{9}$");
    }

}
