package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.address.AddressDto;
import com.example.prm392_android_app_frontend.data.remote.api.AddressApi;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.presentation.adapter.AddressAdapter;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserAddressesActivity extends AppCompatActivity {

    private RecyclerView rvAddresses;
    private ProgressBar progress;
    private MaterialButton btnAddAddress, btnSaveChanges;
    private AddressAdapter adapter;

    private AddressApi api;
    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_address);

        rvAddresses = findViewById(R.id.rvAddresses);
        progress = findViewById(R.id.progress);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        userId = TokenStore.getUserId(this);
        api = ApiClient.getAuthClient(this).create(AddressApi.class);

        adapter = new AddressAdapter(new ArrayList<>(), this);
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        rvAddresses.setAdapter(adapter);

        btnAddAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddUserAddressActivity.class);
            startActivity(intent);
        });

        btnSaveChanges.setOnClickListener(v ->
                Toast.makeText(this, "Tính năng lưu thay đổi (nếu có) đang được phát triển", Toast.LENGTH_SHORT).show()
        );

        loadAddresses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Khi thêm địa chỉ mới xong quay lại -> refresh
        loadAddresses();
    }

    private void loadAddresses() {
        showLoading(true);
        api.getAddressesByUserId(userId).enqueue(new Callback<List<AddressDto>>() {
            @Override
            public void onResponse(Call<List<AddressDto>> call, Response<List<AddressDto>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setAddresses(response.body());
                } else {
                    Toast.makeText(UserAddressesActivity.this, "Không thể tải danh sách địa chỉ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AddressDto>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(UserAddressesActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean b) {
        progress.setVisibility(b ? View.VISIBLE : View.GONE);
    }
}
