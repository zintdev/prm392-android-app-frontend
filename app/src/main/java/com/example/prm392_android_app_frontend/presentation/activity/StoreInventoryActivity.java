package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.prm392_android_app_frontend.data.dto.store.StoreLocationResponse;
import com.example.prm392_android_app_frontend.presentation.adapter.StoreInventoryStoreAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.StoreInventoryViewModel;

import java.util.List;

public class StoreInventoryActivity extends AppCompatActivity {

    private StoreInventoryViewModel viewModel;
    private StoreInventoryStoreAdapter adapter;
    private ProgressBar progressBar;
    private TextView txtEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_inventory);
        setupToolbar();
        initViews();
        setupRecycler();
        setupViewModel();
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
        RecyclerView recyclerView = findViewById(R.id.recyclerStores);
        adapter = new StoreInventoryStoreAdapter(this::openStoreDetail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(StoreInventoryViewModel.class);

        viewModel.getStores().observe(this, this::renderStores);
        viewModel.getLoading().observe(this, loading -> {
            boolean visible = loading != null && loading;
            progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        });
        viewModel.getError().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadStores();
    }

    private void renderStores(List<StoreLocationResponse> stores) {
        adapter.submit(stores);
        boolean empty = stores == null || stores.isEmpty();
        txtEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void openStoreDetail(StoreLocationResponse store) {
        if (store == null || store.getId() == null) {
            return;
        }
        Intent intent = new Intent(this, StoreInventoryDetailActivity.class);
        intent.putExtra(StoreInventoryDetailActivity.EXTRA_STORE_ID, store.getId());
        intent.putExtra(StoreInventoryDetailActivity.EXTRA_STORE_NAME, store.getStoreName());
        intent.putExtra(StoreInventoryDetailActivity.EXTRA_STORE_ADDRESS, store.getAddress());
        startActivity(intent);
    }
}
