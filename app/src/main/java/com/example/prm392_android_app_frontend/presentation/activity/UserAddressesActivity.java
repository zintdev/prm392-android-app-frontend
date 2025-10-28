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
    import com.example.prm392_android_app_frontend.data.repository.AddressRepository;
    import com.example.prm392_android_app_frontend.presentation.adapter.AddressAdapter;
    import com.example.prm392_android_app_frontend.storage.TokenStore;
    import com.google.android.material.appbar.MaterialToolbar;
    import com.google.android.material.button.MaterialButton;
    import com.google.android.material.dialog.MaterialAlertDialogBuilder;
    import com.google.android.material.textfield.TextInputEditText;

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
        private AddressRepository addressRepository;

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
            addressRepository = new AddressRepository(this);

            adapter = new AddressAdapter(new AddressAdapter.OnAddressActionListener() {
                @Override public void onEdit(AddressDto a) { showEditDialog(a); }
                @Override public void onDelete(AddressDto a) { confirmDelete(a); }
            });
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
        private void confirmDelete(AddressDto a) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Xoá địa chỉ")
                    .setMessage("Bạn có chắc muốn xoá địa chỉ #" + a.id + " không?")
                    .setNegativeButton("Huỷ", null)
                    .setPositiveButton("Xoá", (d, which) -> {
                        showLoading(true);
                        addressRepository.deleteAddress(a.id, new AddressRepository.CallbackResult<Boolean>() {
                            @Override public void onSuccess(Boolean ok) {
                                showLoading(false);
                                Toast.makeText(UserAddressesActivity.this, "Đã xoá", Toast.LENGTH_SHORT).show();
                                loadAddresses(); // reload list
                            }
                            @Override public void onError(String message, int code) {
                                showLoading(false);
                                Toast.makeText(UserAddressesActivity.this, "Xoá thất bại: " + message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).show();
        }

        // Sửa
        private void showEditDialog(AddressDto a) {
            View content = getLayoutInflater().inflate(R.layout.dialog_edit_address, null, false);
            TextInputEditText edtLine1 = content.findViewById(R.id.edtLine1);
            TextInputEditText edtLine2 = content.findViewById(R.id.edtLine2);
            TextInputEditText edtCityState = content.findViewById(R.id.edtCityState);

            edtLine1.setText(a.shippingAddressLine1);
            edtLine2.setText(a.shippingAddressLine2);
            edtCityState.setText(a.shippingCityState);

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Sửa địa chỉ #" + a.id)
                    .setView(content)
                    .setNegativeButton("Huỷ", null)
                    .setPositiveButton("Lưu", (d, w) -> {
                        int userId = TokenStore.getUserId(this);
                        String line1 = textOf(edtLine1);
                        String line2 = textOf(edtLine2);
                        String cs    = textOf(edtCityState);

                        if (line1.isEmpty() || cs.isEmpty()) {
                            Toast.makeText(this, "Line1 và Tỉnh/Thành không được trống", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        showLoading(true);
                        addressRepository.updateAddress(a.id, userId, line1, line2, cs,
                                new AddressRepository.CallbackResult<AddressDto>() {
                                    @Override public void onSuccess(AddressDto data) {
                                        showLoading(false);
                                        Toast.makeText(UserAddressesActivity.this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                                        loadAddresses();
                                    }
                                    @Override public void onError(String message, int code) {
                                        showLoading(false);
                                        Toast.makeText(UserAddressesActivity.this, "Cập nhật thất bại: " + message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }).show();
        }

        private String textOf(TextInputEditText e){
            return e.getText()==null? "" : e.getText().toString().trim();
        }
    }
