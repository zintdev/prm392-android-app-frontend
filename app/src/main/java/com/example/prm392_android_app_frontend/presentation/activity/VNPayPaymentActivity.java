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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpay_payment);

        initViews();
        setupToolbar();

        String paymentUrl = getIntent().getStringExtra("payment_url");

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
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Bắt các URL đặc biệt của VNPay
                if (url.contains("vnp_ResponseCode")) {
                    handlePaymentReturn(url);
                    return true; // Ngăn WebView tự điều hướng
                }
                return false; // Cho phép WebView tải các URL khác
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
        Uri uri = Uri.parse(url);
        String responseCode = uri.getQueryParameter("vnp_ResponseCode");

        Intent resultIntent = new Intent();
        
        // Mã "00" là thanh toán thành công
        if ("00".equals(responseCode)) {
            android.util.Log.d("VNPayPaymentActivity", "Payment SUCCESS");
            setResult(RESULT_OK, resultIntent);
        } else {
            android.util.Log.d("VNPayPaymentActivity", "Payment FAILED or CANCELED. Code: " + responseCode);
            setResult(RESULT_CANCELED, resultIntent);
        }
        
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
