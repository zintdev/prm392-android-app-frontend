package com.example.prm392_android_app_frontend.presentation.fragment.admin;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.databinding.FragmentAdminDashboardBinding;

public class AdminDashboardFragment extends Fragment {

    // 1. Tạo Interface
    public interface AdminDashboardListener {
        void navigateToProductManagement();
    }

    private FragmentAdminDashboardBinding binding;
    private AdminDashboardListener listener;

    // Kiểm tra xem Activity chứa fragment có implement interface không
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AdminDashboardListener) {
            listener = (AdminDashboardListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AdminDashboardListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.button1.setOnClickListener(v -> {
            // 3. Gọi phương thức của listener
            if (listener != null) {
                listener.navigateToProductManagement();
            }
        });

        binding.button2.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chuẩn bị gọi API Quản lý Nhà xuất bản", Toast.LENGTH_SHORT).show();
        });

        binding.button3.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chuẩn bị gọi API Quản lý Tác giả", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null; // Dọn dẹp listener
    }
}
