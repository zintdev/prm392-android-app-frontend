package com.example.prm392_android_app_frontend.presentation.fragment.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.presentation.activity.MainActivity;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.snackbar.Snackbar;

public class AdminSettingFragment extends Fragment {
    private View rowProfile, rowLogout;
    private TextView txtName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        txtName = v.findViewById(R.id.txtName1);
        rowProfile = v.findViewById(R.id.rowProfile);
        rowLogout = v.findViewById(R.id.rowLogout);
        String username = TokenStore.getUsername(requireContext());
        txtName.setText(username);
        ((TextView) rowProfile.findViewById(R.id.title)).setText("Hồ sơ cá nhân");
        ((ImageView) rowProfile.findViewById(R.id.icon)).setImageResource(R.drawable.ic_person);

        ((TextView) rowLogout.findViewById(R.id.title)).setText("Đăng xuất");
        ((ImageView) rowLogout.findViewById(R.id.icon)).setImageResource(R.drawable.ic_logout);
        rowLogout.setOnClickListener(v1 -> {
            TokenStore.clear(requireContext());
            Snackbar.make(v1, "Đã đăng xuất", Snackbar.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), MainActivity.class));
            requireActivity().finish();
        });
    }
}
