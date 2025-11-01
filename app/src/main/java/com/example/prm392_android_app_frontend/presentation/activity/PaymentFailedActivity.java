package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_android_app_frontend.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PaymentFailedActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView textOrderId;
    private TextView textAmount;
    private TextView textErrorReason;
    private TextView textFailedTime;
    private MaterialButton buttonViewOrder;
    private MaterialButton buttonBackHome;

    private int orderId;
    private int paymentId;
    private double amount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_failed);

        initViews();
        setupToolbar();
        loadDataFromIntent();
        setupButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_payment_failed);
        textOrderId = findViewById(R.id.text_order_id);
        textAmount = findViewById(R.id.text_amount);
        textErrorReason = findViewById(R.id.text_error_reason);
        textFailedTime = findViewById(R.id.text_failed_time);
        buttonViewOrder = findViewById(R.id.button_view_order);
        buttonBackHome = findViewById(R.id.button_back_home);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void loadDataFromIntent() {
        String orderIdStr = getIntent().getStringExtra("order_id");
        String paymentIdStr = getIntent().getStringExtra("payment_id");
        amount = getIntent().getDoubleExtra("amount", 0.0);
        String errorReason = getIntent().getStringExtra("error_reason");
        
        // Parse to int for later use
        try {
            orderId = orderIdStr != null ? Integer.parseInt(orderIdStr) : 0;
            paymentId = paymentIdStr != null ? Integer.parseInt(paymentIdStr) : 0;
        } catch (NumberFormatException e) {
            orderId = 0;
            paymentId = 0;
        }

        // Display data
        textOrderId.setText("#" + (orderIdStr != null ? orderIdStr : "0"));
        
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        textAmount.setText(formatter.format(amount) + "đ");
        
        textErrorReason.setText(errorReason != null ? errorReason : "Giao dịch bị hủy");
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        textFailedTime.setText(sdf.format(new Date()));
    }

    private void setupButtons() {
        buttonViewOrder.setOnClickListener(v -> {
            // Navigate to Order List - Tất cả đơn hàng (tab 0)
            Intent intent = new Intent(this, OrderViewListActivity.class);
            intent.putExtra("selected_tab", 0); // Tab "Tất cả"
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        buttonBackHome.setOnClickListener(v -> {
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
        // Navigate to cart instead of default back
        navigateToCart();
    }
}
