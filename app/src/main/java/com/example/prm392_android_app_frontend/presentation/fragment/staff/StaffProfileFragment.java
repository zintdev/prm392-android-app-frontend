package com.example.prm392_android_app_frontend.presentation.fragment.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.UserDto;
import com.example.prm392_android_app_frontend.presentation.activity.LoginActivity;
import com.example.prm392_android_app_frontend.presentation.viewmodel.StaffSharedViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;

/**
 * Displays profile information for the currently logged-in staff member.
 */
public class StaffProfileFragment extends Fragment {

    private StaffSharedViewModel sharedViewModel;
    private ProgressBar progressBar;
    private TextView txtName;
    private TextView txtEmail;
    private TextView txtPhone;
    private TextView txtStore;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = view.findViewById(R.id.profileProgress);
        txtName = view.findViewById(R.id.txtProfileName);
        txtEmail = view.findViewById(R.id.txtProfileEmail);
        txtPhone = view.findViewById(R.id.txtProfilePhone);
        txtStore = view.findViewById(R.id.txtProfileStore);
        btnLogout = view.findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> logout());

        sharedViewModel = new ViewModelProvider(requireActivity()).get(StaffSharedViewModel.class);
        sharedViewModel.getStaffInfo().observe(getViewLifecycleOwner(), this::renderProfile);
        sharedViewModel.getLoading().observe(getViewLifecycleOwner(), this::toggleLoading);
        sharedViewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        if (sharedViewModel.getStaffInfo().getValue() == null) {
            sharedViewModel.loadCurrentStaff();
        }
    }

    private void renderProfile(@Nullable UserDto userDto) {
        if (userDto == null) {
            txtName.setText(R.string.staff_profile_name_placeholder);
            txtEmail.setText(R.string.staff_profile_email_placeholder);
            txtPhone.setText(R.string.staff_profile_phone_placeholder);
            txtStore.setText(R.string.staff_inventory_store_unknown);
            return;
        }
        txtName.setText(userDto.username != null ? userDto.username : getString(R.string.staff_profile_name_placeholder));
        txtEmail.setText(userDto.email != null ? userDto.email : getString(R.string.staff_profile_email_placeholder));
        txtPhone.setText(userDto.phoneNumber != null ? userDto.phoneNumber : getString(R.string.staff_profile_phone_placeholder));
        txtStore.setText(userDto.storeName != null ? userDto.storeName : getString(R.string.staff_inventory_store_unknown));
    }

    private void toggleLoading(Boolean loading) {
        boolean show = loading != null && loading;
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void logout() {
        if (getContext() == null) {
            return;
        }
        TokenStore.clear(requireContext());
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
