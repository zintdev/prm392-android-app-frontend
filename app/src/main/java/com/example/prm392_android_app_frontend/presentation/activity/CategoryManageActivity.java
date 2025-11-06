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
import com.example.prm392_android_app_frontend.data.dto.CategoryDto;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.CategoryApi;
import com.example.prm392_android_app_frontend.data.repository.CategoryRepository;
import com.example.prm392_android_app_frontend.presentation.adapter.CategoryManageAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import android.text.Editable;
import android.text.TextWatcher;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryManageActivity extends AppCompatActivity implements CategoryManageAdapter.Listener {

    private CategoryRepository repository;
    private CategoryManageAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_category);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.recyclerViewProducts);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryManageAdapter(this);
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddProduct);
        fab.setOnClickListener(v -> showAddDialog());

        CategoryApi api = ApiClient.get().create(CategoryApi.class);
        repository = new CategoryRepository(api);

        loadCategories();
    }

    private void loadCategories() {
        repository.getAll(new Callback<List<CategoryDto>>() {
            @Override
            public void onResponse(Call<List<CategoryDto>> call, Response<List<CategoryDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.submitList(response.body());
                } else {
                    Toast.makeText(CategoryManageActivity.this, "Không tải được danh mục", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CategoryDto>> call, Throwable t) {
                Toast.makeText(CategoryManageActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_category, null);
        TextInputEditText et = view.findViewById(R.id.edit_text);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        btnSave.setOnClickListener(v -> {
            String name = et.getText() != null ? et.getText().toString().trim() : "";
            if (name.isEmpty()) {
                Toast.makeText(this, "Tên không được trống", Toast.LENGTH_SHORT).show();
                return;
            }
            CategoryDto dto = new CategoryDto();
            dto.setName(name);
            repository.create(dto, new Callback<CategoryDto>() {
                @Override
                public void onResponse(Call<CategoryDto> call, Response<CategoryDto> response) {
                    if (response.isSuccessful()) {
                        dialog.dismiss();
                        loadCategories();
                        Toast.makeText(CategoryManageActivity.this, "Thêm danh mục thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CategoryManageActivity.this, "Không thể tạo danh mục", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<CategoryDto> call, Throwable t) {
                    Toast.makeText(CategoryManageActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showEditDialog(CategoryDto item) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_category, null);
        TextInputEditText et = view.findViewById(R.id.edit_text);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        
        String originalName = item.getName();
        et.setText(originalName);
        btnSave.setEnabled(false);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String currentText = s.toString().trim();
                btnSave.setEnabled(!currentText.equals(originalName) && !currentText.isEmpty());
            }
        });

        btnSave.setOnClickListener(v -> {
            String name = et.getText() != null ? et.getText().toString().trim() : "";
            if (name.isEmpty()) {
                Toast.makeText(this, "Tên không được trống", Toast.LENGTH_SHORT).show();
                return;
            }
            CategoryDto dto = new CategoryDto();
            dto.setId(item.getId());
            dto.setName(name);
            repository.update(item.getId(), dto, new Callback<CategoryDto>() {
                @Override
                public void onResponse(Call<CategoryDto> call, Response<CategoryDto> response) {
                    if (response.isSuccessful()) {
                        dialog.dismiss();
                        loadCategories();
                        Toast.makeText(CategoryManageActivity.this, "Cập nhật danh mục thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CategoryManageActivity.this, "Không thể cập nhật", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<CategoryDto> call, Throwable t) {
                    Toast.makeText(CategoryManageActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onEdit(CategoryDto category) {
        showEditDialog(category);
    }

    @Override
    public void onDelete(CategoryDto category) {
        showDeleteDialog(category);
    }
    private void showDeleteDialog(CategoryDto item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc muốn xóa danh mục \"" + item.getName() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    repository.delete(item.getId(), new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                loadCategories();
                                Toast.makeText(CategoryManageActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CategoryManageActivity.this, "Không thể xóa danh mục", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(CategoryManageActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
