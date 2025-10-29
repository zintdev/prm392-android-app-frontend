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

public class PaymentSuccessActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView textOrderId;
    private TextView textPaymentId;
    private TextView textAmount;
    private TextView textPaymentMethod;
    private TextView textPaymentTime;
    private MaterialButton buttonViewOrder;
    private MaterialButton buttonBackHome;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        initViews();
        setupToolbar();
        loadDataFromIntent();
        setupButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_payment_success);
        textOrderId = findViewById(R.id.text_order_id);
        textPaymentId = findViewById(R.id.text_payment_id);
        textAmount = findViewById(R.id.text_amount);
        textPaymentMethod = findViewById(R.id.text_payment_method);
        textPaymentTime = findViewById(R.id.text_payment_time);
        buttonViewOrder = findViewById(R.id.button_view_order);
        buttonBackHome = findViewById(R.id.button_back_home);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        // Không cho phép back - phải chọn button
    }

    private void loadDataFromIntent() {
        int orderId = getIntent().getIntExtra("order_id", 0);
        int paymentId = getIntent().getIntExtra("payment_id", 0);
        double amount = getIntent().getDoubleExtra("amount", 0.0);
        String paymentMethod = getIntent().getStringExtra("payment_method");

        // Display data
        textOrderId.setText("#" + orderId);
        textPaymentId.setText("PAY" + paymentId);
        
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        textAmount.setText(formatter.format(amount) + "đ");
        
        textPaymentMethod.setText(paymentMethod != null ? paymentMethod : "VNPay");
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        textPaymentTime.setText(sdf.format(new Date()));
    }

    private void setupButtons() {
        buttonViewOrder.setOnClickListener(v -> {
            // TODO: Navigate to Order Details
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
        // Disable back button - user must choose an option
        navigateToCart();
    }
}
