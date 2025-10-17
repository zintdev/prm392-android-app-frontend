package com.example.prm392_android_app_frontend.presentation.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // Thêm import này
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.presentation.viewmodel.CartViewModel;
import com.example.prm392_android_app_frontend.presentation.viewmodel.ProductViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "PRODUCT_ID";

    // ViewModels
    private ProductViewModel productViewModel;
    private CartViewModel cartViewModel;

    // UI Components
    private ImageView detailImage;
    private TextView detailName, detailArtist, detailPrice, detailDescription, quantity, publisherName, categoryName, releaseDate;
    private ProgressBar progressBar;
    private MaterialButton buttonAddToCart;
    private ImageButton buttonCartIcon;

    private int productId;
    private ProductDto currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_productdetail);

        productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
        if (productId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        initViewModels();
        setupClickListeners();
        observeViewModels(); // << SỬA: Chuyển observe lên trước khi gọi fetch

        // Bắt đầu tải dữ liệu chi tiết sản phẩm
        // Sửa lại cách gọi theo ViewModel mới
        productViewModel.fetchProductById(productId);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar_detail);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        detailImage = findViewById(R.id.detail_image);
        detailName = findViewById(R.id.detail_name);
        detailArtist = findViewById(R.id.detail_artist);
        detailPrice = findViewById(R.id.detail_price);
        detailDescription = findViewById(R.id.detail_description);
        quantity = findViewById(R.id.detail_quantity);
        publisherName = findViewById(R.id.detail_publisher);
        categoryName = findViewById(R.id.detail_category);
        releaseDate = findViewById(R.id.detail_release_date);
        progressBar = findViewById(R.id.detail_progress_bar);

        buttonAddToCart = findViewById(R.id.button_add_to_cart);
        buttonCartIcon = findViewById(R.id.button_cart_icon);
    }

    private void initViewModels() {
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
    }

    private void setupClickListeners() {
        buttonAddToCart.setOnClickListener(v -> {
            if (currentProduct != null && currentProduct.getQuantity() > 0) {
                showLoading(true);
                cartViewModel.addProductToCart(productId, 1);
            } else {
                Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCartIcon.setOnClickListener(v -> {
            Toast.makeText(this, "Mở giỏ hàng", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, CartActivity.class);
            // startActivity(intent);
        });
    }

    private void observeViewModels() {
        showLoading(true);

        // === SỬA LẠI HOÀN TOÀN CÁCH LẮNG NGHE PRODUCTVIEWMODEL ===

        // 1. Lắng nghe dữ liệu sản phẩm thành công
        productViewModel.getSelectedProduct().observe(this, productDto -> {
            // Kiểm tra null để chắc chắn có dữ liệu trả về
            if (productDto != null) {
                hideLoading();
                this.currentProduct = productDto;
                populateUI(productDto);
            }
        });

        // 2. Lắng nghe lỗi từ ProductViewModel
        productViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                hideLoading();
                Toast.makeText(this, "Lỗi tải sản phẩm: " + error, Toast.LENGTH_LONG).show();
            }
        });


        // === Lắng nghe CartViewModel (Giữ nguyên, đã đúng) ===

        cartViewModel.getCartLiveData().observe(this, cartDto -> {
            hideLoading();
            Toast.makeText(this, "Đã thêm vào giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
        });

        cartViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                hideLoading();
                Toast.makeText(this, "Lỗi giỏ hàng: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populateUI(@NonNull ProductDto product) {
        detailName.setText(product.getName());
        detailArtist.setText(product.getArtistName());
        detailDescription.setText(product.getDescription());
        quantity.setText(String.valueOf(product.getQuantity()));
        publisherName.setText(product.getPublisherName());
        categoryName.setText(product.getCategoryName());

        DecimalFormat formatter = new DecimalFormat("###,###,###");
        detailPrice.setText(formatter.format(product.getPrice()) + "đ");

        releaseDate.setText(formatDate(product.getReleaseDate()));

        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(detailImage);

        if (product.getQuantity() <= 0) {
            buttonAddToCart.setText("Hết hàng");
            buttonAddToCart.setEnabled(false);
        } else {
            buttonAddToCart.setText("Thêm vào giỏ hàng");
            buttonAddToCart.setEnabled(true);
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            buttonAddToCart.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            if (currentProduct != null && currentProduct.getQuantity() > 0) {
                buttonAddToCart.setEnabled(true);
            }
        }
    }

    private void hideLoading() {
        showLoading(false);
    }

    private String formatDate(String dateString) {
        if (dateString == null) return "N/A";
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = inputFormat.parse(dateString);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            if (dateString.contains("T")) {
                return dateString.split("T")[0];
            }
        }
        return dateString;
    }
}
