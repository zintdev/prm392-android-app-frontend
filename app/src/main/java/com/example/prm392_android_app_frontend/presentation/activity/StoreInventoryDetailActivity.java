package com.example.prm392_android_app_frontend.presentation.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.store.StoreInventoryChange;
import com.example.prm392_android_app_frontend.data.dto.store.StoreInventoryItemDto;
import com.example.prm392_android_app_frontend.presentation.adapter.StoreInventoryAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.StoreInventoryViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;

import java.util.List;

public class StoreInventoryDetailActivity extends AppCompatActivity {

    public static final String EXTRA_STORE_ID = "extra_store_id";
    public static final String EXTRA_STORE_NAME = "extra_store_name";
    public static final String EXTRA_STORE_ADDRESS = "extra_store_address";

    private StoreInventoryViewModel viewModel;
    private StoreInventoryAdapter adapter;
    private ProgressBar progressBar;
    private TextView txtEmpty;
    private int storeId;
    private boolean loadingState;
    private boolean updatingState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_inventory_detail);
        storeId = getIntent().getIntExtra(EXTRA_STORE_ID, -1);
        if (storeId <= 0) {
            Toast.makeText(this, "Không tìm thấy thông tin cửa hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setupToolbar();
        initViews();
        setupRecycler();
        setupViewModel();
        bindStoreInfo();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        txtEmpty = findViewById(R.id.txtEmpty);
    }

    private void setupRecycler() {
        RecyclerView recyclerView = findViewById(R.id.recyclerInventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new StoreInventoryAdapter((item, quantity) -> {
            if (item == null || item.getProductId() == null) {
                return;
            }
            int actorId = TokenStore.getUserId(this);
            if (actorId <= 0) {
                Toast.makeText(this, R.string.inventory_actor_missing, Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.updateInventory(storeId, item, quantity, actorId);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(StoreInventoryViewModel.class);

        viewModel.getInventory().observe(this, this::renderInventory);
        viewModel.getLoading().observe(this, this::onLoadingChanged);
        viewModel.getUpdating().observe(this, this::onUpdatingChanged);
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
        viewModel.getUpdatedItem().observe(this, item -> {
            if (item != null && item.getProductId() != null) {
                adapter.markSynced(item);
            }
        });

        viewModel.loadInventory(storeId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_store_inventory_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_save_all) {
            handleSaveAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleSaveAll() {
        List<StoreInventoryChange> pending = adapter.getDirtyItems();
        if (pending.isEmpty()) {
            Toast.makeText(this, R.string.inventory_no_changes, Toast.LENGTH_SHORT).show();
            return;
        }
        int actorId = TokenStore.getUserId(this);
        if (actorId <= 0) {
            Toast.makeText(this, R.string.inventory_actor_missing, Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.updateInventoryBatch(storeId, pending, actorId);
    }

    private void onLoadingChanged(Boolean loading) {
        loadingState = loading != null && loading;
        updateProgressState();
    }

    private void onUpdatingChanged(Boolean updating) {
        updatingState = updating != null && updating;
        updateProgressState();
        if (adapter != null) {
            adapter.setUpdating(updatingState);
        }
    }

    private void updateProgressState() {
        boolean show = loadingState || updatingState;
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void renderInventory(List<StoreInventoryItemDto> items) {
        adapter.submit(items);
        boolean empty = items == null || items.isEmpty();
        txtEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void bindStoreInfo() {
        TextView txtName = findViewById(R.id.txtStoreName);
        TextView txtAddress = findViewById(R.id.txtStoreAddress);
        String name = getIntent().getStringExtra(EXTRA_STORE_NAME);
        String address = getIntent().getStringExtra(EXTRA_STORE_ADDRESS);
        txtName.setText(name != null ? name : "Cửa hàng");
        txtAddress.setText(address != null && !address.isEmpty() ? address : "Chưa cập nhật địa chỉ");
    }
}
