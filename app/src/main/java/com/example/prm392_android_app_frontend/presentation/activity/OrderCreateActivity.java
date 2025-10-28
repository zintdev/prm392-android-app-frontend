package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.CartItemDto;
import com.example.prm392_android_app_frontend.data.dto.CreateOrderRequestDto;
import com.example.prm392_android_app_frontend.data.dto.OrderDTO;
import com.example.prm392_android_app_frontend.presentation.adapter.OrderItemAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.OrderViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderCreateActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewOrderItems;
    private OrderItemAdapter orderItemAdapter;
    private TextView textViewTotalPrice;
    private MaterialButton buttonPlaceOrder;
    private ProgressBar progressBar;

    // Form inputs
    private TextInputLayout inputLayoutFullName;
    private TextInputLayout inputLayoutPhone;
    private TextInputLayout inputLayoutAddressLine1;
    private TextInputLayout inputLayoutAddressLine2;
    private TextInputLayout inputLayoutCityState;
    private RadioGroup radioGroupShipment;

    private TextInputEditText editTextFullName;
    private TextInputEditText editTextPhone;
    private TextInputEditText editTextAddressLine1;
    private TextInputEditText editTextAddressLine2;
    private TextInputEditText editTextCityState;

    private OrderViewModel orderViewModel;
    private List<CartItemDto> selectedItems;
    private double totalAmount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_create);

        // Nhận dữ liệu từ Cart
        selectedItems = getIntent().getParcelableArrayListExtra("selected_items");
        totalAmount = getIntent().getDoubleExtra("total_amount", 0.0);

        if (selectedItems == null || selectedItems.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào được chọn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        updateTotalPrice();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_order_create);
        recyclerViewOrderItems = findViewById(R.id.recycler_view_order_items);
        textViewTotalPrice = findViewById(R.id.text_view_total_price);
        buttonPlaceOrder = findViewById(R.id.button_place_order);
        progressBar = findViewById(R.id.progress_bar_order);

        // Form inputs
        inputLayoutFullName = findViewById(R.id.input_layout_full_name);
        inputLayoutPhone = findViewById(R.id.input_layout_phone);
        inputLayoutAddressLine1 = findViewById(R.id.input_layout_address_line1);
        inputLayoutAddressLine2 = findViewById(R.id.input_layout_address_line2);
        inputLayoutCityState = findViewById(R.id.input_layout_city_state);
        radioGroupShipment = findViewById(R.id.radio_group_shipment);

        editTextFullName = findViewById(R.id.edit_text_full_name);
        editTextPhone = findViewById(R.id.edit_text_phone);
        editTextAddressLine1 = findViewById(R.id.edit_text_address_line1);
        editTextAddressLine2 = findViewById(R.id.edit_text_address_line2);
        editTextCityState = findViewById(R.id.edit_text_city_state);

        buttonPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        orderItemAdapter = new OrderItemAdapter(selectedItems);
        recyclerViewOrderItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrderItems.setAdapter(orderItemAdapter);
    }

    private void setupViewModel() {
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        orderViewModel.getOrderLiveData().observe(this, order -> {
            hideLoading();
            if (order != null) {
                Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                // Có thể chuyển về trang chủ hoặc trang đơn hàng
                finish();
            }
        });

        orderViewModel.getErrorMessage().observe(this, error -> {
            hideLoading();
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void placeOrder() {
        if (!validateInputs()) {
            return;
        }

        // Kiểm tra đăng nhập
        if (!TokenStore.isLoggedIn(this)) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        showLoading();

        // Lấy userId từ session
        int userId = TokenStore.getUserId(this);
        if (userId == -1) {
            Toast.makeText(this, "Không thể xác định người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Tạo request object
        CreateOrderRequestDto request = new CreateOrderRequestDto(
                userId, // Lấy từ session thay vì hardcode
                getShipmentMethod(),
                editTextFullName.getText().toString().trim(),
                editTextPhone.getText().toString().trim(),
                editTextAddressLine1.getText().toString().trim(),
                editTextAddressLine2.getText().toString().trim(),
                editTextCityState.getText().toString().trim()
        );

        // Gọi API tạo order
        orderViewModel.placeOrder(request);
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate tên
        if (editTextFullName.getText().toString().trim().isEmpty()) {
            inputLayoutFullName.setError("Vui lòng nhập họ tên");
            isValid = false;
        } else {
            inputLayoutFullName.setError(null);
        }

        // Validate số điện thoại
        String phone = editTextPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            inputLayoutPhone.setError("Vui lòng nhập số điện thoại");
            isValid = false;
        } else if (!phone.matches("^[0-9]{10,11}$")) {
            inputLayoutPhone.setError("Số điện thoại không hợp lệ");
            isValid = false;
        } else {
            inputLayoutPhone.setError(null);
        }

        // Validate địa chỉ dòng 1
        if (editTextAddressLine1.getText().toString().trim().isEmpty()) {
            inputLayoutAddressLine1.setError("Vui lòng nhập địa chỉ");
            isValid = false;
        } else {
            inputLayoutAddressLine1.setError(null);
        }

        // Validate thành phố
        if (editTextCityState.getText().toString().trim().isEmpty()) {
            inputLayoutCityState.setError("Vui lòng nhập thành phố/tỉnh");
            isValid = false;
        } else {
            inputLayoutCityState.setError(null);
        }

        return isValid;
    }

    private String getShipmentMethod() {
        int selectedId = radioGroupShipment.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_delivery) {
            return "DELIVERY";
        } else {
            return "PICKUP";
        }
    }

    private void updateTotalPrice() {
        try {
            DecimalFormat formatter = new DecimalFormat("###,###,###");
            String formattedPrice = formatter.format(totalAmount);
            textViewTotalPrice.setText(String.format("%sđ", formattedPrice));
        } catch (Exception e) {
            textViewTotalPrice.setText("0đ");
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        buttonPlaceOrder.setEnabled(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        buttonPlaceOrder.setEnabled(true);
    }
}