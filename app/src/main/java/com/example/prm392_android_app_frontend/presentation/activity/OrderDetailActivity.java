package com.example.prm392_android_app_frontend.presentation.activity;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.prm392_android_app_frontend.presentation.viewmodel.OrderManagementViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "EXTRA_ORDER_ID";

    private OrderManagementViewModel viewModel;
    private OrderDetailItemAdapter itemAdapter;

    private TextView orderIdTextView;
    private TextView orderStatusTextView;
    private TextView customerNameTextView;
    private TextView customerPhoneTextView;
    private TextView shippingAddressTextView;
    private TextView grandTotalTextView;
    private RecyclerView itemsRecyclerView;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        initViews();
        setupRecyclerView();

    viewModel = new ViewModelProvider(this).get(OrderManagementViewModel.class);

        int orderId = getIntent().getIntExtra(EXTRA_ORDER_ID, -1);
        if (orderId == -1) {
            Toast.makeText(this, getString(R.string.order_detail_invalid_id), Toast.LENGTH_SHORT).show();
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
            getSupportActionBar().setTitle(getString(R.string.order_detail_title_format, order.getId()));
        }
        int orderId = order.getId() != null ? order.getId() : 0;
        orderIdTextView.setText(getString(R.string.order_detail_title_format, orderId));
        orderStatusTextView.setText(safeText(order.getOrderStatus()));
        customerNameTextView.setText(getString(R.string.order_detail_customer_prefix, safeText(order.getShippingFullName())));
        customerPhoneTextView.setText(getString(R.string.order_detail_phone_prefix, safeText(order.getShippingPhone())));
        shippingAddressTextView.setText(getString(R.string.order_detail_address_prefix, buildFullAddress(order)));

        double grandTotal = order.getGrandTotal() != null ? order.getGrandTotal() : 0d;
        grandTotalTextView.setText(getString(R.string.order_detail_total_prefix, currencyFormat.format(grandTotal)));

        if (order.getItems() != null) {
            itemAdapter.setItems(order.getItems());
        } else {
            itemAdapter.setItems(Collections.emptyList());
        }
    }

    private String safeText(String value) {
        return TextUtils.isEmpty(value) ? getString(R.string.staff_order_unknown_field) : value;
    }

    private String buildFullAddress(OrderDto order) {
        List<String> parts = new ArrayList<>();
        addIfNotEmpty(parts, order.getShippingAddressLine1());
        addIfNotEmpty(parts, order.getShippingAddressLine2());
        addIfNotEmpty(parts, order.getShippingCityState());
        if (parts.isEmpty()) {
            return getString(R.string.staff_order_unknown_field);
        }
        return TextUtils.join(", ", parts);
    }

    private void addIfNotEmpty(List<String> list, String value) {
        if (!TextUtils.isEmpty(value)) {
            list.add(value);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
