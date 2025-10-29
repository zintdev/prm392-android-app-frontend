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
    private MaterialButton buttonRetryPayment;
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
        buttonRetryPayment = findViewById(R.id.button_retry_payment);
        buttonViewOrder = findViewById(R.id.button_view_order);
        buttonBackHome = findViewById(R.id.button_back_home);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void loadDataFromIntent() {
        orderId = getIntent().getIntExtra("order_id", 0);
        paymentId = getIntent().getIntExtra("payment_id", 0);
        amount = getIntent().getDoubleExtra("amount", 0.0);
        String errorReason = getIntent().getStringExtra("error_reason");

        // Display data
        textOrderId.setText("#" + orderId);
        
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        textAmount.setText(formatter.format(amount) + "đ");
        
        textErrorReason.setText(errorReason != null ? errorReason : "Giao dịch bị hủy");
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        textFailedTime.setText(sdf.format(new Date()));
    }

    private void setupButtons() {
        buttonRetryPayment.setOnClickListener(v -> {
            // Return to payment with order info to retry
            Intent resultIntent = new Intent();
            resultIntent.putExtra("retry_payment", true);
            resultIntent.putExtra("order_id", orderId);
            resultIntent.putExtra("payment_id", paymentId);
            resultIntent.putExtra("amount", amount);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        buttonViewOrder.setOnClickListener(v -> {
            // Navigate to Cart
            navigateToCart();
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
