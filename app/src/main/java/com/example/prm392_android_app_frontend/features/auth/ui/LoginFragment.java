package com.example.prm392_android_app_frontend.features.auth.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.features.auth.data.dto.LoginResponse;
import com.example.prm392_android_app_frontend.features.auth.vm.AuthViewModel;
import com.example.prm392_android_app_frontend.prefs.SessionManager;
import com.example.prm392_android_app_frontend.util.Resource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {

    private AuthViewModel vm;
    private TextInputLayout emailTil, passwordTil;
    private TextInputEditText emailEdt, passwordEdt;
    private MaterialButton loginBtn;

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_login, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        vm = new ViewModelProvider(this).get(AuthViewModel.class);

        emailTil = v.findViewById(R.id.email_text_input_layout);
        passwordTil = v.findViewById(R.id.password_text_input_layout);
        emailEdt = v.findViewById(R.id.email_edit_text);
        passwordEdt = v.findViewById(R.id.password_edit_text);
        loginBtn = v.findViewById(R.id.login_button);

        loginBtn.setOnClickListener(view -> {
            String user = safeText(emailEdt);
            String pass = safeText(passwordEdt);
            if (!validate(user, pass)) return;

            setLoading(true);
            vm.login(user, pass);
        });

        vm.getLoginState().observe(getViewLifecycleOwner(), res -> {
            if (res == null) return;
            if (res.status == Resource.Status.LOADING) {
                setLoading(true);
            } else if (res.status == Resource.Status.SUCCESS) {
                setLoading(false);
                handleSuccess(res.data, v);
            } else {
                setLoading(false);
                showError(res.message);
            }
        });
    }

    private void handleSuccess(LoginResponse data, View v) {
        if (data == null || data.token == null) {
            showError("Empty response");
            return;
        }
        SessionManager sm = new SessionManager(requireContext());
        String username = data.user != null ? data.user.username : "";
        String email = data.user != null ? data.user.email : "";
        String role = data.user != null ? data.user.role : "";
        sm.save(data.token, username, email, role);

        Toast.makeText(requireContext(), "Login success!", Toast.LENGTH_SHORT).show();
        // Điều hướng sang màn hình chính (sửa theo nav_graph của bạn)
        Navigation.findNavController(v).navigate(R.id.btnDirections);
    }

    private boolean validate(String user, String pass) {
        boolean ok = true;
        emailTil.setError(null);
        passwordTil.setError(null);

        if (TextUtils.isEmpty(user)) {
            emailTil.setError("Required");
            ok = false;
        }
        if (TextUtils.isEmpty(pass)) {
            passwordTil.setError("Required");
            ok = false;
        }
        return ok;
    }

    private void setLoading(boolean loading) {
        loginBtn.setEnabled(!loading);
        loginBtn.setText(loading ? "Loading..." : "Login");
    }

    private void showError(String msg) {
        Toast.makeText(requireContext(), msg != null ? msg : "Login failed", Toast.LENGTH_LONG).show();
    }

    private String safeText(TextInputEditText edt) {
        return edt.getText() == null ? "" : edt.getText().toString().trim();
    }
}
