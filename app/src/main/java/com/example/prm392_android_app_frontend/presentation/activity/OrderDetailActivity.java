package com.example.prm392_android_app_frontend.presentation.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.order.OrderDto;
import com.example.prm392_android_app_frontend.presentation.adapter.OrderDetailItemAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.OrderViewModel;

public class OrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "EXTRA_ORDER_ID";

    private OrderViewModel viewModel;
    private OrderDetailItemAdapter itemAdapter;

    private TextView orderIdTextView, orderStatusTextView, customerNameTextView, customerPhoneTextView, shippingAddressTextView, grandTotalTextView;
    private RecyclerView itemsRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        initViews();
        setupRecyclerView();

        viewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        int orderId = getIntent().getIntExtra(EXTRA_ORDER_ID, -1);
        if (orderId == -1) {
            Toast.makeText(this, "Invalid Order ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        observeViewModel();
        viewModel.fetchOrderById(orderId);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_order_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        orderIdTextView = findViewById(R.id.detail_order_id);
        orderStatusTextView = findViewById(R.id.detail_order_status);
        customerNameTextView = findViewById(R.id.detail_customer_name);
        customerPhoneTextView = findViewById(R.id.detail_customer_phone);
        shippingAddressTextView = findViewById(R.id.detail_shipping_address);
        grandTotalTextView = findViewById(R.id.detail_grand_total);
        itemsRecyclerView = findViewById(R.id.detail_items_recyclerview);
    }

    private void setupRecyclerView() {
        itemAdapter = new OrderDetailItemAdapter();
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemsRecyclerView.setAdapter(itemAdapter);
    }

    private void observeViewModel() {
        viewModel.getOrderDetail().observe(this, resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        // You can show a progress bar here
                        break;
                    case SUCCESS:
                        if (resource.getData() != null) {
                            populateOrderDetails(resource.getData());
                        }
                        break;
                    case ERROR:
                        Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    private void populateOrderDetails(OrderDto order) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Order #" + order.getId());
        }
        orderIdTextView.setText("Order #" + order.getId());
        orderStatusTextView.setText(order.getOrderStatus());
        customerNameTextView.setText("Customer: " + order.getShippingFullName());
        customerPhoneTextView.setText("Phone: " + order.getShippingPhone());
        String fullAddress = order.getShippingAddressLine1() + ", " + order.getShippingAddressLine2() + ", " + order.getShippingCityState();
        shippingAddressTextView.setText("Address: " + fullAddress);
        grandTotalTextView.setText(String.format("Total: %,.0f VND", order.getGrandTotal()));

        if (order.getItems() != null) {
            itemAdapter.setItems(order.getItems());
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
