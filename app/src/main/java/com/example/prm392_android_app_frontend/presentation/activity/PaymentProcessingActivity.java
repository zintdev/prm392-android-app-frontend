package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_android_app_frontend.R;

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentProcessingActivity extends AppCompatActivity {

    private TextView tvOrderIdValue;
    private TextView tvPaymentIdValue;
    private TextView tvAmountValue;

    private String orderId;
    private String paymentId;
    private double amount;
    private String returnUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_processing);

        // Initialize views
        tvOrderIdValue = findViewById(R.id.tvOrderIdValue);
        tvPaymentIdValue = findViewById(R.id.tvPaymentIdValue);
        tvAmountValue = findViewById(R.id.tvAmountValue);

        // Get data from intent
        orderId = getIntent().getStringExtra("order_id");
        paymentId = getIntent().getStringExtra("payment_id");
        amount = getIntent().getDoubleExtra("amount", 0.0);
        returnUrl = getIntent().getStringExtra("return_url");

        // Display payment information
        loadDataFromIntent();

        // Simulate processing for 2-3 seconds then check result
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            processPaymentResult();
        }, 2500); // 2.5 seconds delay
    }

    private void loadDataFromIntent() {
        // Display Order ID
        if (orderId != null && !orderId.isEmpty()) {
            tvOrderIdValue.setText(orderId);
        }

        // Display Payment ID
        if (paymentId != null && !paymentId.isEmpty()) {
            tvPaymentIdValue.setText(paymentId);
        }

        // Display Amount with currency format
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvAmountValue.setText(currencyFormat.format(amount));
    }

    private void processPaymentResult() {
        // Parse the return URL to check payment status
        if (returnUrl != null && !returnUrl.isEmpty()) {
            Uri uri = Uri.parse(returnUrl);
            String responseCode = uri.getQueryParameter("vnp_ResponseCode");

            if ("00".equals(responseCode)) {
                // Payment successful
                navigateToSuccess();
            } else {
                // Payment failed
                navigateToFailed(responseCode);
            }
        } else {
            // No return URL, treat as failed
            navigateToFailed(null);
        }
    }

    private void navigateToSuccess() {
        Intent intent = new Intent(this, PaymentSuccessActivity.class);
        intent.putExtra("order_id", orderId);
        intent.putExtra("payment_id", paymentId);
        intent.putExtra("amount", amount);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToFailed(String responseCode) {
        Intent intent = new Intent(this, PaymentFailedActivity.class);
        intent.putExtra("order_id", orderId);
        intent.putExtra("payment_id", paymentId);
        intent.putExtra("amount", amount);
        
        // Get error reason based on response code
        String errorReason = getErrorReason(responseCode);
        intent.putExtra("error_reason", errorReason);
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String getErrorReason(String code) {
        if (code == null) {
            return "Không nhận được phản hồi từ cổng thanh toán";
        }

        switch (code) {
            case "07":
                return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).";
            case "09":
                return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.";
            case "10":
                return "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11":
                return "Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch.";
            case "12":
                return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa.";
            case "13":
                return "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP). Xin quý khách vui lòng thực hiện lại giao dịch.";
            case "24":
                return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51":
                return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
            case "65":
                return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.";
            case "75":
                return "Ngân hàng thanh toán đang bảo trì.";
            case "79":
                return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định. Xin quý khách vui lòng thực hiện lại giao dịch";
            case "99":
                return "Các lỗi khác (lỗi còn lại, không có trong danh sách mã lỗi đã liệt kê)";
            default:
                return "Giao dịch thất bại. Mã lỗi: " + code;
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button during processing
        // Do nothing
    }
}
