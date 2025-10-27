package com.example.prm392_android_app_frontend.presentation.fragment.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.presentation.activity.ChangePasswordActivity;
import com.example.prm392_android_app_frontend.presentation.activity.EditProfileActivity;
import com.example.prm392_android_app_frontend.presentation.activity.MainActivity;
import com.example.prm392_android_app_frontend.presentation.activity.AddUserAddressActivity;
import com.example.prm392_android_app_frontend.presentation.activity.UserAddressesActivity;
import com.example.prm392_android_app_frontend.presentation.viewmodel.UserViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.snackbar.Snackbar;

public class AccountFragment extends Fragment {

    private TextView txtName, txtFPoint, txtFreeship;
    private View rowLogout, rowChangePassword, rowUpdateProfile, rowUserAddress;

    private UserViewModel vm;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        txtName = v.findViewById(R.id.txtName);
        txtFPoint = v.findViewById(R.id.txtFPoint);
        txtFreeship = v.findViewById(R.id.txtFreeship);
        rowLogout = v.findViewById(R.id.rowLogout);
        rowUpdateProfile = v.findViewById(R.id.rowUpdateProfile);
        rowChangePassword = v.findViewById(R.id.rowChangePassword);
        rowUserAddress = v.findViewById(R.id.rowUserAddress);
        userId = TokenStore.getUserId(requireContext());
        vm = new ViewModelProvider(this).get(UserViewModel.class);
        vm.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                txtName.setText(user.username != null ? user.username : "");
            }
        });

        vm.error.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && getView() != null) {
                Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT).show();
            }
        });

        vm.loadUser(userId);
        rowLogout.setOnClickListener(v1 -> {
            TokenStore.clear(requireContext());
            Snackbar.make(v1, "Đã đăng xuất", Snackbar.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), MainActivity.class));
            requireActivity().finish();
        });

        rowChangePassword.setOnClickListener(v1 ->
                startActivity(new Intent(requireContext(), ChangePasswordActivity.class)));

        rowUpdateProfile.setOnClickListener(v1 ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class)));

        rowUserAddress.setOnClickListener(v1 ->
                startActivity(new Intent(requireContext(), UserAddressesActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        vm.loadUser(userId);
    }
}
