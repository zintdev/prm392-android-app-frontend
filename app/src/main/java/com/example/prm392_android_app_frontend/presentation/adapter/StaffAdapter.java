package com.example.prm392_android_app_frontend.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    public interface OnStaffClickListener {
        void onStaffClick(UserDto staff);
    }

    private final List<UserDto> data = new ArrayList<>();
    private final OnStaffClickListener listener;

    public StaffAdapter(OnStaffClickListener listener) {
        this.listener = listener;
    }

    public StaffAdapter() {
        this(null);
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
    holder.bind(data.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void submit(List<UserDto> staff) {
        data.clear();
        if (staff != null) {
            data.addAll(staff);
        }
        notifyDataSetChanged();
    }

    static class StaffViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtName;
        private final TextView txtEmail;
        private final TextView txtPhone;
        private final TextView txtStore;

        StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtPhone = itemView.findViewById(R.id.txtPhone);
            txtStore = itemView.findViewById(R.id.txtStore);
        }

        void bind(UserDto dto, OnStaffClickListener listener) {
            txtName.setText(dto.username);
            txtEmail.setText(dto.email != null ? dto.email : "");
            txtPhone.setText(dto.phoneNumber != null ? dto.phoneNumber : "");
            if (dto.storeName != null) {
                txtStore.setText("Cửa hàng: " + dto.storeName);
            } else {
                txtStore.setText("Chưa gán cửa hàng");
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStaffClick(dto);
                }
            });
        }
    }
}
