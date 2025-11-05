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
        String orderId = getIntent().getStringExtra("order_id");
        String paymentId = getIntent().getStringExtra("payment_id");
        double amount = getIntent().getDoubleExtra("amount", 0.0);
        String paymentMethod = getIntent().getStringExtra("payment_method");
        String vnpTransactionNo = getIntent().getStringExtra("vnp_transaction_no");
        String vnpBankTranNo = getIntent().getStringExtra("vnp_bank_tran_no");

        // Display data
        textOrderId.setText("#" + (orderId != null ? orderId : "0"));
        
        // Hiển thị số giao dịch VNPay nếu có, nếu không thì hiển thị payment ID
        if (vnpTransactionNo != null && !vnpTransactionNo.isEmpty()) {
            textPaymentId.setText("VNP" + vnpTransactionNo);
        } else {
            textPaymentId.setText("PAY" + (paymentId != null ? paymentId : "0"));
        }
        
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        textAmount.setText(formatter.format(amount) + "đ");
        
        textPaymentMethod.setText(paymentMethod != null ? paymentMethod : "VNPay");
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        textPaymentTime.setText(sdf.format(new Date()));
        
        // Log transaction details for debugging
        android.util.Log.d("PaymentSuccess", "Order ID: " + orderId);
        android.util.Log.d("PaymentSuccess", "Payment ID: " + paymentId);
        android.util.Log.d("PaymentSuccess", "VNPay Transaction No: " + vnpTransactionNo);
        android.util.Log.d("PaymentSuccess", "VNPay Bank Tran No: " + vnpBankTranNo);
    }

    private void setupButtons() {
        buttonViewOrder.setOnClickListener(v -> {
            // TODO: Navigate to Order Details
            Intent intent = new Intent(this, OrderViewListActivity.class);
            intent.putExtra("selected_tab", 1); // Tab "Đã thanh toán" (PAID)
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
        // Disable back button - user must choose an option
        navigateToCart();
    }

}
