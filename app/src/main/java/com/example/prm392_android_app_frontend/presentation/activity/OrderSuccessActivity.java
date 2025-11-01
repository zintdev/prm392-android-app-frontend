package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_android_app_frontend.R;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderSuccessActivity extends AppCompatActivity {

    private TextView tvOrderIdCod;
    private TextView tvTotalAmountCod;
    private TextView tvCreatedDateCod;
    private MaterialButton btnViewOrderCod;
    private MaterialButton btnHomeCod;

    private String orderId;
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        // Initialize views
        tvOrderIdCod = findViewById(R.id.tv_order_id_cod);
        tvTotalAmountCod = findViewById(R.id.tv_total_amount_cod);
        tvCreatedDateCod = findViewById(R.id.tv_created_date_cod);
        btnViewOrderCod = findViewById(R.id.btn_view_order_cod);
        btnHomeCod = findViewById(R.id.btn_home_cod);

        // Get data from intent
        orderId = getIntent().getStringExtra("order_id");
        totalAmount = getIntent().getDoubleExtra("total_amount", 0.0);

        // Display order information
        loadOrderData();

        // Setup button listeners
        setupButtons();
    }

    private void loadOrderData() {
        // Display Order ID
        if (orderId != null && !orderId.isEmpty()) {
            tvOrderIdCod.setText(orderId);
        }

        // Display Total Amount
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalAmountCod.setText(currencyFormat.format(totalAmount));

        // Display Created Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvCreatedDateCod.setText(dateFormat.format(new Date()));
    }

    private void setupButtons() {
        // View Order button - Chuyển đến trang danh sách đơn hàng với tab "Đã thanh toán"
        btnViewOrderCod.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderViewListActivity.class);
            intent.putExtra("selected_tab", 1); // Tab "Đã thanh toán" (PAID)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Cart button
        btnHomeCod.setOnClickListener(v -> {
            navigateToCart();
        });
    }

    private void navigateToCart() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("select_tab", R.id.nav_cart); // Use correct extra key and value
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Prevent going back, force user to use buttons
        navigateToCart();
    }
}
