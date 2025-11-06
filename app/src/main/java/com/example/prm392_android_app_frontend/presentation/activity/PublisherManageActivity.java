package com.example.prm392_android_app_frontend.presentation.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.PublisherDto;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.PublisherApi;
import com.example.prm392_android_app_frontend.data.repository.PublisherRepository;
import com.example.prm392_android_app_frontend.presentation.adapter.PublisherManageAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import android.text.Editable;
import android.text.TextWatcher;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublisherManageActivity extends AppCompatActivity implements PublisherManageAdapter.Listener {

    private PublisherRepository repository;
    private PublisherManageAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_publisher);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.recyclerViewPublisher);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PublisherManageAdapter(this);
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddPublisher);
        fab.setOnClickListener(v -> showAddDialog());

        PublisherApi api = ApiClient.get().create(PublisherApi.class);
        repository = new PublisherRepository(api);

        loadPublishers();
    }

    private void loadPublishers() {
        repository.getAll(new Callback<List<PublisherDto>>() {
            @Override
            public void onResponse(Call<List<PublisherDto>> call, Response<List<PublisherDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.submitList(response.body());
                } else {
                    Toast.makeText(PublisherManageActivity.this, "Không tải được nhà xuất bản", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PublisherDto>> call, Throwable t) {
                Toast.makeText(PublisherManageActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_publisher, null);
        TextInputEditText etName = view.findViewById(R.id.edit_text_1);
        TextInputEditText etYear = view.findViewById(R.id.edit_text_2);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        btnSave.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String yearStr = etYear.getText() != null ? etYear.getText().toString().trim() : "";

            // 🔍 Kiểm tra rỗng
            if (name.isEmpty()) {
                Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }
            if (yearStr.isEmpty()) {
                Toast.makeText(this, "Năm thành lập không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            Integer year;
            try {
                year = Integer.parseInt(yearStr);
                if (year <= 0) {
                    Toast.makeText(this, "Năm thành lập phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Năm thành lập không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            PublisherDto dto = new PublisherDto(null, name, year);
            repository.create(dto, new Callback<PublisherDto>() {
                @Override
                public void onResponse(Call<PublisherDto> call, Response<PublisherDto> response) {
                    if (response.isSuccessful()) {
                        dialog.dismiss();
                        loadPublishers();
                        Toast.makeText(PublisherManageActivity.this, "Thêm nhà xuất bản thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PublisherManageActivity.this, "Không thể tạo nhà xuất bản", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PublisherDto> call, Throwable t) {
                    Toast.makeText(PublisherManageActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });


        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showEditDialog(PublisherDto item) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_publisher, null);
        TextInputEditText etName = view.findViewById(R.id.edit_text_1);
        TextInputEditText etYear = view.findViewById(R.id.edit_text_2);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        
        String originalName = item.getName() != null ? item.getName() : "";
        String originalYear = item.getFoundedYear() != null ? String.valueOf(item.getFoundedYear()) : "";
        
        // Prefill ngay sau khi inflate
        etName.setText(originalName);
        etYear.setText(originalYear);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        
        dialog.setOnShowListener(d -> {
            // Đảm bảo prefill lại khi dialog hiển thị
            etName.setText(originalName);
            etYear.setText(originalYear);
            if (etYear.getText() != null && etYear.getText().length() > 0) {
                etYear.setSelection(etYear.getText().length());
            }
            btnSave.setEnabled(false);
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String currentName = etName.getText() != null ? etName.getText().toString().trim() : "";
                String currentYear = etYear.getText() != null ? etYear.getText().toString().trim() : "";
                boolean hasChanged = !currentName.equals(originalName) || !currentYear.equals(originalYear);
                btnSave.setEnabled(hasChanged && !currentName.isEmpty());
            }
        };

        etName.addTextChangedListener(textWatcher);
        etYear.addTextChangedListener(textWatcher);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String yearStr = etYear.getText() != null ? etYear.getText().toString().trim() : "";
            Integer year = null;
            if (!yearStr.isEmpty()) {
                try {
                    year = Integer.parseInt(yearStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Năm thành lập không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (name.isEmpty()) {
                Toast.makeText(this, "Tên không được trống", Toast.LENGTH_SHORT).show();
                return;
            }
            PublisherDto dto = new PublisherDto(item.getId(), name, year);
            int id = item.getId() != null ? item.getId() : 0;
            repository.update(id, dto, new Callback<PublisherDto>() {
                @Override
                public void onResponse(Call<PublisherDto> call, Response<PublisherDto> response) {
                    if (response.isSuccessful()) {
                        dialog.dismiss();
                        loadPublishers();
                        Toast.makeText(PublisherManageActivity.this, "Cập nhật nhà xuất bản thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PublisherManageActivity.this, "Không thể cập nhật", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PublisherDto> call, Throwable t) {
                    Toast.makeText(PublisherManageActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onEdit(PublisherDto publisher) {
        showEditDialog(publisher);
    }

    @Override
    public void onDelete(PublisherDto publisher) {
        showDeleteDialog(publisher);
    }

    private void showDeleteDialog(PublisherDto item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa nhà xuất bản")
                .setMessage("Bạn có chắc muốn xóa nhà xuất bản \"" + item.getName() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    repository.delete(
                            item.getId() != null ? item.getId() : 0,
                            new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                loadPublishers();
                                Toast.makeText(PublisherManageActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(PublisherManageActivity.this, "Không thể xóa nhà xuất bản", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(PublisherManageActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

}
