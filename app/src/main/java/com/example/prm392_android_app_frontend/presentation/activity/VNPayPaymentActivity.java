package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_android_app_frontend.R;
import com.google.android.material.appbar.MaterialToolbar;

public class VNPayPaymentActivity extends AppCompatActivity {

    private WebView webViewVNPay;
    private ProgressBar progressBar;
    private MaterialToolbar toolbar;

    private int orderId;
    private int paymentId;
    private double amount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpay_payment);

        initViews();
        setupToolbar();

        String paymentUrl = getIntent().getStringExtra("payment_url");
        orderId = getIntent().getIntExtra("order_id", 0);
        paymentId = getIntent().getIntExtra("payment_id", 0);
        amount = getIntent().getDoubleExtra("amount", 0.0);

        if (paymentUrl == null || paymentUrl.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có URL thanh toán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupWebView();
        loadPaymentUrl(paymentUrl);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_vnpay);
        webViewVNPay = findViewById(R.id.webview_vnpay);
        progressBar = findViewById(R.id.progress_bar_vnpay);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            // Khi người dùng nhấn nút back trên toolbar, coi như là hủy thanh toán
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void setupWebView() {
        webViewVNPay.getSettings().setJavaScriptEnabled(true);
        webViewVNPay.getSettings().setDomStorageEnabled(true);
        webViewVNPay.getSettings().setLoadWithOverviewMode(true);
        webViewVNPay.getSettings().setUseWideViewPort(true);

        webViewVNPay.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);

                // Kiểm tra return URL
                if (url.contains("/api/vnpay/return")) {
                    handlePaymentReturn(url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("/api/vnpay/return")) {
                    handlePaymentReturn(url);
                    return true; // Ngăn WebView tự điều hướng
                }
                return false;
            }
        });

        webViewVNPay.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadPaymentUrl(String url) {
        android.util.Log.d("VNPayPaymentActivity", "Loading URL: " + url);
        webViewVNPay.loadUrl(url);
    }

    private void handlePaymentReturn(String url) {
        android.util.Log.d("VNPayPaymentActivity", "Payment return URL: " + url);
        
        // Chuyển sang màn hình Processing với hiệu ứng loading
        Intent intent = new Intent(this, PaymentProcessingActivity.class);
        intent.putExtra("order_id", String.valueOf(orderId));
        intent.putExtra("payment_id", String.valueOf(paymentId));
        intent.putExtra("amount", amount);
        intent.putExtra("return_url", url);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        // Đóng Activity và trả kết quả về cho OrderCreateActivity
        finish();
    }

    @Override
    public void onBackPressed() {
        // Khi người dùng nhấn nút back cứng, coi như là hủy thanh toán
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
