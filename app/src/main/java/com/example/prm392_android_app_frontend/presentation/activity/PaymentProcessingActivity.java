package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_android_app_frontend.R;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PaymentProcessingActivity extends AppCompatActivity {

    private TextView tvOrderIdValue;
    private TextView tvPaymentIdValue;
    private TextView tvAmountValue;

    private String orderId;
    private String paymentId;
    private double amount;
    private String returnUrl;
    
    // VNPay response parameters
    private String vnpResponseCode;
    private String vnpTransactionNo;
    private String vnpBankTranNo;
    
    private OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_processing);

        // Initialize HTTP client
        httpClient = new OkHttpClient();

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
            vnpResponseCode = uri.getQueryParameter("vnp_ResponseCode");
            String transactionStatus = uri.getQueryParameter("vnp_TransactionStatus");
            vnpTransactionNo = uri.getQueryParameter("vnp_TransactionNo");
            vnpBankTranNo = uri.getQueryParameter("vnp_BankTranNo");
            
            // Log payment result details
            android.util.Log.d("PaymentProcessing", "=== VNPay Callback ===");
            android.util.Log.d("PaymentProcessing", "Response Code: " + vnpResponseCode);
            android.util.Log.d("PaymentProcessing", "Transaction Status: " + transactionStatus);
            android.util.Log.d("PaymentProcessing", "Transaction No: " + vnpTransactionNo);
            android.util.Log.d("PaymentProcessing", "Bank Trans No: " + vnpBankTranNo);
            android.util.Log.d("PaymentProcessing", "Full URL: " + returnUrl);

            // Kiểm tra cả ResponseCode và TransactionStatus
            // Cả 2 đều phải là "00" mới được coi là thành công
            if ("00".equals(vnpResponseCode) && "00".equals(transactionStatus)) {
                android.util.Log.d("PaymentProcessing", "Payment SUCCESS");
                // Gọi callback URL về BE trước khi navigate
                notifyBackendPaymentResult();
            } else {
                android.util.Log.d("PaymentProcessing", "Payment FAILED - Code: " + vnpResponseCode);
                // Gọi callback URL về BE trước khi navigate
                notifyBackendPaymentResult();
            }
        } else {
            // No return URL, treat as failed
            android.util.Log.e("PaymentProcessing", "No return URL received");
            navigateToFailed(null);
        }
    }

    /**
     * Gọi HTTP GET request đến BE callback URL để BE cập nhật trạng thái thanh toán
     */
    private void notifyBackendPaymentResult() {
        if (returnUrl == null || returnUrl.isEmpty()) {
            android.util.Log.e("PaymentProcessing", "Return URL is null, skipping backend notification");
            proceedToResultScreen();
            return;
        }

        android.util.Log.d("PaymentProcessing", "Notifying backend: " + returnUrl);

        Request request = new Request.Builder()
                .url(returnUrl)
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                android.util.Log.e("PaymentProcessing", "Failed to notify backend: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(PaymentProcessingActivity.this, 
                            "Không thể kết nối với server. Vui lòng kiểm tra lại đơn hàng.", 
                            Toast.LENGTH_SHORT).show();
                    // Vẫn tiếp tục navigate dù gọi API thất bại
                    proceedToResultScreen();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                android.util.Log.d("PaymentProcessing", "Backend response code: " + response.code());
                android.util.Log.d("PaymentProcessing", "Backend response body: " + responseBody);

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        android.util.Log.d("PaymentProcessing", "Backend notified successfully");
                    } else {
                        android.util.Log.w("PaymentProcessing", "Backend returned error: " + response.code());
                    }
                    // Navigate dù response thành công hay thất bại
                    proceedToResultScreen();
                });
            }
        });
    }

    /**
     * Tiến hành navigate đến màn hình kết quả dựa trên response code
     */
    private void proceedToResultScreen() {
        if ("00".equals(vnpResponseCode)) {
            navigateToSuccess();
        } else {
            navigateToFailed(vnpResponseCode);
        }
    }

    private void navigateToSuccess() {
        Intent intent = new Intent(this, PaymentSuccessActivity.class);
        intent.putExtra("order_id", orderId);
        intent.putExtra("payment_id", paymentId);
        intent.putExtra("amount", amount);
        intent.putExtra("payment_method", "VNPay");
        intent.putExtra("vnp_transaction_no", vnpTransactionNo);
        intent.putExtra("vnp_bank_tran_no", vnpBankTranNo);
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
