package com.example.prm392_android_app_frontend.presentation.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.UserDto;
import com.example.prm392_android_app_frontend.data.dto.store.StoreLocationResponse;
import com.example.prm392_android_app_frontend.presentation.adapter.StaffAdapter;
import com.example.prm392_android_app_frontend.presentation.adapter.StoreAssignAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.StaffManageViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class StaffManagementActivity extends AppCompatActivity {

    private StaffManageViewModel viewModel;
    private StaffAdapter adapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private final List<StoreLocationResponse> allStores = new ArrayList<>();
    private StoreAssignAdapter storeAdapter;
    private AlertDialog storeDialog;
    private UserDto pendingStaff;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_management);
        setupToolbar();
        setupViewModel();
        initViews();
        setupRecycler();
        observeViewModel();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        emptyView = findViewById(R.id.emptyView);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> showCreateDialog());

        swipeRefreshLayout.setOnRefreshListener(() -> viewModel.loadStaff());
    }

    private void setupRecycler() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StaffAdapter(this::onStaffClicked);
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(StaffManageViewModel.class);
        viewModel.loadStaff();
    }

    private void observeViewModel() {
        viewModel.getStaffList().observe(this, staff -> {
            swipeRefreshLayout.setRefreshing(false);
            adapter.submit(staff);
            emptyView.setVisibility(staff == null || staff.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getStores().observe(this, stores -> {
            allStores.clear();
            if (stores != null) {
                allStores.addAll(stores);
            }
            if (pendingStaff != null && !allStores.isEmpty()) {
                showStoreSelectionDialog(pendingStaff);
            }
        });

        viewModel.getLoading().observe(this, loading -> {
            if (loading != null) {
                progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
                if (!loading) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        viewModel.getError().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSuccess().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_staff, null, false);
        TextInputEditText inputUsername = dialogView.findViewById(R.id.inputUsername);
        TextInputEditText inputEmail = dialogView.findViewById(R.id.inputEmail);
        TextInputEditText inputPhone = dialogView.findViewById(R.id.inputPhone);
        TextInputEditText inputPassword = dialogView.findViewById(R.id.inputPassword);

        new AlertDialog.Builder(this)
                .setTitle("Tạo nhân viên mới")
                .setView(dialogView)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String username = toString(inputUsername);
                    String email = toString(inputEmail);
                    String phone = toString(inputPhone);
                    String password = toString(inputPassword);

                    if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.createStaff(username, email, password, phone);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String toString(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void onStaffClicked(UserDto staff) {
        pendingStaff = staff;
        if (!allStores.isEmpty()) {
            showStoreSelectionDialog(staff);
        } else {
            viewModel.loadStores();
        }
    }

    private void showStoreSelectionDialog(UserDto staff) {
        if (allStores.isEmpty()) {
            Toast.makeText(this, "Chưa có cửa hàng để gán", Toast.LENGTH_SHORT).show();
            pendingStaff = null;
            return;
        }

        if (storeDialog != null && storeDialog.isShowing()) {
            storeDialog.dismiss();
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_store, null, false);
        TextInputLayout layoutSearch = dialogView.findViewById(R.id.layoutSearchStore);
        TextInputEditText inputSearch = dialogView.findViewById(R.id.inputSearchStore);
        RecyclerView recyclerStores = dialogView.findViewById(R.id.recyclerStores);

        recyclerStores.setLayoutManager(new LinearLayoutManager(this));
    storeAdapter = new StoreAssignAdapter(store -> {
            if (storeDialog != null) {
                storeDialog.dismiss();
            }
            int actorId = TokenStore.getUserId(this);
            if (actorId <= 0) {
                Toast.makeText(this, "Không tìm thấy thông tin quản trị", Toast.LENGTH_SHORT).show();
                pendingStaff = null;
                return;
            }
            Integer storeId = store.getId();
            if (storeId == null) {
                Toast.makeText(this, "Không xác định được cửa hàng", Toast.LENGTH_SHORT).show();
                pendingStaff = null;
                return;
            }
            viewModel.assignStaffToStore(staff.id, storeId, actorId);
            pendingStaff = null;
        });
        recyclerStores.setAdapter(storeAdapter);
        applyStoreFilter("");

        if (layoutSearch != null) {
            layoutSearch.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        }
        if (inputSearch != null) {
            inputSearch.addTextChangedListener(new SimpleTextWatcher(text -> applyStoreFilter(text)));
        }

        storeDialog = new AlertDialog.Builder(this)
                .setTitle("Chọn cửa hàng cho " + staff.username)
                .setView(dialogView)
                .setNegativeButton("Hủy", (dialog, which) -> {
                    pendingStaff = null;
                    dialog.dismiss();
                })
                .create();
        storeDialog.show();
    }

    private void applyStoreFilter(String query) {
        if (storeAdapter == null) {
            return;
        }
        String normalized = query == null ? "" : query.trim().toLowerCase();
        List<StoreLocationResponse> filtered = new ArrayList<>();
        for (StoreLocationResponse store : allStores) {
            String name = store.getStoreName() != null ? store.getStoreName().toLowerCase() : "";
            String address = store.getAddress() != null ? store.getAddress().toLowerCase() : "";
            if (normalized.isEmpty() || name.contains(normalized) || address.contains(normalized)) {
                filtered.add(store);
            }
        }
        storeAdapter.submit(filtered);
    }

    private static class SimpleTextWatcher implements TextWatcher {

        interface OnTextChanged {
            void onTextChanged(String text);
        }

        private final OnTextChanged callback;

        SimpleTextWatcher(OnTextChanged callback) {
            this.callback = callback;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // no-op
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (callback != null) {
                callback.onTextChanged(s != null ? s.toString() : "");
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            // no-op
        }
    }
}
