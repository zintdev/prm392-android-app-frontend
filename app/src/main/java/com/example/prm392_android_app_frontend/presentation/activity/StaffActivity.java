package com.example.prm392_android_app_frontend.presentation.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.presentation.adapter.OrderAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.OrderViewModel;

public class StaffActivity extends AppCompatActivity {

    private OrderViewModel orderViewModel;
    private OrderAdapter orderAdapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        RecyclerView recyclerView = findViewById(R.id.ordersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter();
        recyclerView.setAdapter(orderAdapter);

        orderViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(OrderViewModel.class);

        orderViewModel.getOrders().observe(this, resource -> {
            if (resource != null) {
                swipeRefreshLayout.setRefreshing(resource.getStatus() == com.example.prm392_android_app_frontend.core.util.Resource.Status.LOADING);
                progressBar.setVisibility(resource.getStatus() == com.example.prm392_android_app_frontend.core.util.Resource.Status.LOADING && !swipeRefreshLayout.isRefreshing() ? View.VISIBLE : View.GONE);

                switch (resource.getStatus()) {
                    case SUCCESS:
                        if (resource.getData() != null) {
                            orderAdapter.setOrders(resource.getData());
                        }
                        break;
                    case ERROR:
                        Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> orderViewModel.refreshOrders());
    }
}
