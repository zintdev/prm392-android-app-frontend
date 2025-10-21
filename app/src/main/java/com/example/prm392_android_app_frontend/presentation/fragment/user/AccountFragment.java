package com.example.prm392_android_app_frontend.presentation.fragment.user;

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
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.example.prm392_android_app_frontend.presentation.activity.LoginActivity;
import com.example.prm392_android_app_frontend.presentation.activity.ProfileActivity;
import com.google.android.material.snackbar.Snackbar;

public class AccountFragment extends Fragment {

    private TextView txtName, txtFPoint, txtFreeship;
    private View rowProfile, rowLogout;

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
        rowProfile = v.findViewById(R.id.rowProfile);
        rowLogout = v.findViewById(R.id.rowLogout);
        String username = TokenStore.getUsername(requireContext());
        txtName.setText(username);
            ((TextView) rowProfile.findViewById(R.id.title)).setText("Hồ sơ cá nhân");
            ((ImageView) rowProfile.findViewById(R.id.icon)).setImageResource(R.drawable.ic_person);

            ((TextView) rowLogout.findViewById(R.id.title)).setText("Đăng xuất");
            ((ImageView) rowLogout.findViewById(R.id.icon)).setImageResource(R.drawable.ic_logout);

        // Click: mở trang cập nhật hồ sơ
        rowProfile.setOnClickListener(v12 -> {
            if (!TokenStore.isLoggedIn(requireContext())) {
                Snackbar.make(v12, "Bạn cần đăng nhập", Snackbar.LENGTH_SHORT).show();
                startActivity(new Intent(requireContext(), LoginActivity.class));
                return;
            }
            Intent i = new Intent(requireContext(), ProfileActivity.class);
            i.putExtra("userId", TokenStore.getUserId(requireContext()));
            startActivity(i);
        });

        // Click: đăng xuất
        rowLogout.setOnClickListener(v1 -> {
            TokenStore.clear(requireContext());
            Snackbar.make(v1, "Đã đăng xuất", Snackbar.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

//        v.findViewById(R.id.btnOrderWaitingPay).setOnClickListener(x ->
//                Snackbar.make(x, "Chờ thanh toán", Snackbar.LENGTH_SHORT).show());
//        v.findViewById(R.id.btnOrderProcessing).setOnClickListener(x ->
//                Snackbar.make(x, "Đang xử lý", Snackbar.LENGTH_SHORT).show());
////        v.findViewById(R.id.btnOrderShipping).setOnClickListener(x ->
////                Snackbar.make(x, "Đang giao hàng", Snackbar.LENGTH_SHORT).show());
//        v.findViewById(R.id.btnOrderCompleted).setOnClickListener(x ->
//                Snackbar.make(x, "Hoàn tất", Snackbar.LENGTH_SHORT).show());
    }
}
