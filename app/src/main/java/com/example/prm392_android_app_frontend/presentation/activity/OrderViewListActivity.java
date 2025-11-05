package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.OrderDTO;
import com.example.prm392_android_app_frontend.presentation.adapter.OrderListAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.OrderViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.stream.Collectors;

public class OrderViewListActivity extends AppCompatActivity implements OrderListAdapter.OnOrderClickListener {

    private RecyclerView recyclerViewOrders;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private MaterialButton buttonShopNow;
    private TabLayout tabLayout;
    private MaterialToolbar toolbar;

    private OrderViewModel orderViewModel;
    private OrderListAdapter adapter;
    private List<OrderDTO> allOrders;
    private int currentTabPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_view_list);

        initViews();
        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        setupTabLayout();
        loadOrders();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewOrders = findViewById(R.id.recycler_view_orders);
        progressBar = findViewById(R.id.progress_bar);
        layoutEmpty = findViewById(R.id.layout_empty);
        buttonShopNow = findViewById(R.id.button_shop_now);
        tabLayout = findViewById(R.id.tab_layout);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViewModel() {
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        orderViewModel.getOrdersListLiveData().observe(this, orders -> {
            hideLoading();
            if (orders != null && !orders.isEmpty()) {
                allOrders = orders;
                showOrders(orders);
            } else {
                showEmpty();
            }
        });

        orderViewModel.getErrorMessage().observe(this, error -> {
            hideLoading();
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                showEmpty();
            }
        });

        orderViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new OrderListAdapter(this);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(adapter);
    }

    private void setupTabLayout() {
        // Get selected tab from intent
        int selectedTab = getIntent().getIntExtra("selected_tab", 0);
        currentTabPosition = selectedTab;
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPosition = tab.getPosition();
                loadOrdersForTab(currentTabPosition);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        buttonShopNow.setOnClickListener(v -> {
            // Navigate to MainActivity with Home tab
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("select_tab", R.id.nav_home);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        
        // Select the tab from intent
        if (selectedTab > 0 && selectedTab < tabLayout.getTabCount()) {
            TabLayout.Tab tab = tabLayout.getTabAt(selectedTab);
            if (tab != null) {
                tab.select();
            }
        }
    }

    private void loadOrders() {
        if (!TokenStore.isLoggedIn(this)) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadOrdersForTab(currentTabPosition);
    }

    private void loadOrdersForTab(int position) {
        int userId = TokenStore.getUserId(this);
        if (userId == -1) return;

        String status = getStatusForTab(position);
        orderViewModel.getOrdersByUserId(userId, status);
    }

    private String getStatusForTab(int position) {
        switch (position) {
            case 0: // Tất cả
                return null;
            case 1: // Chờ thanh toán
                return "PENDING";
            case 2: // Đã thanh toán
                return "PAID";
            case 3: // Đang xử lý
                return "PROCESSING";
            case 4: // Đang giao
                return "SHIPPING";
            case 5: // Hoàn thành
                return "COMPLETED";
            case 6: // Đã hủy
                return "CANCELLED";
            default:
                return null;
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewOrders.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showOrders(List<OrderDTO> orders) {
        adapter.setOrders(orders);
        recyclerViewOrders.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void showEmpty() {
        recyclerViewOrders.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public void onOrderClick(OrderDTO order) {
        Intent intent = new Intent(this, OrderViewDetailActivity.class);
        intent.putExtra("order_id", order.getId());
        startActivity(intent);
    }
}
