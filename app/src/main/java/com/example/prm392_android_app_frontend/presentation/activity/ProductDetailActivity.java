package com.example.prm392_android_app_frontend.presentation.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.presentation.viewmodel.ProductViewModel;
import com.google.android.material.appbar.MaterialToolbar;
public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "PRODUCT_ID";

    private ProductViewModel productViewModel;
    private ImageView detailImage;
    private TextView detailName, detailArtist, detailPrice, detailDescription, quantity ,  publisherName, categoryName, releaseDate;
    private ProgressBar progressBar;
    private int productId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_productdetail);

        // Lấy ID sản phẩm từ Intent
        productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
        if (productId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity nếu không có ID
            return;
        }

        setupToolbar();
        initViews();

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        observeViewModel();

        // Gọi API để lấy chi tiết sản phẩm
        productViewModel.getProductDetails(productId);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar_detail);
        toolbar.setNavigationOnClickListener(v -> onBackPressed()); // Thêm nút back
    }

    private void initViews() {
        detailImage = findViewById(R.id.detail_image);
        detailName = findViewById(R.id.detail_name);
        detailArtist = findViewById(R.id.detail_artist);
        detailPrice = findViewById(R.id.detail_price);
        detailDescription = findViewById(R.id.detail_description);
        progressBar = findViewById(R.id.detail_progress_bar);
        detailDescription = findViewById(R.id.detail_description);
        quantity = findViewById(R.id.detail_quantity);
        publisherName = findViewById(R.id.detail_publisher);
        categoryName = findViewById(R.id.detail_category);
        releaseDate = findViewById(R.id.detail_release_date);
//        // Ánh xạ các nút mới
////        buttonAddToCart = findViewById(R.id.button_add_to_cart);
////        buttonCartIcon = findViewById(R.id.button_cart_icon);
//
//        // Gán sự kiện click
//        buttonAddToCart.setOnClickListener(v -> {
//            // Logic thêm sản phẩm vào giỏ hàng
//            // Ví dụ: gọi một phương thức trong CartViewModel
//            Toast.makeText(this, "Đã thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
//        });
//
//        buttonCartIcon.setOnClickListener(v -> {
//            // Mở màn hình giỏ hàng (CartFragment hoặc CartActivity)
//            // Ví dụ:
//            // Intent intent = new Intent(this, CartActivity.class);
//            // startActivity(intent);
//            Toast.makeText(this, "Mở giỏ hàng", Toast.LENGTH_SHORT).show();
//        });

    }

    private void observeViewModel() {
        progressBar.setVisibility(View.VISIBLE);
        productViewModel.getProductDetails(productId).observe(this, productDto -> {
            progressBar.setVisibility(View.GONE);
            if (productDto != null) {
                populateUI(productDto);
            } else {
                Toast.makeText(this, "Lỗi khi tải dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI(ProductDto product) {
        detailName.setText(product.getName());
        detailArtist.setText(product.getArtistName());
        detailPrice.setText(String.format("$%.2f", product.getPrice()));
        detailDescription.setText(product.getDescription());
        quantity.setText(String.valueOf(product.getQuantity()));
        publisherName.setText(product.getPublisherName());
        categoryName.setText(product.getCategoryName());
        releaseDate.setText(product.getReleaseDate());


        Glide.with(this)
                .load(product.getImageUrl())
                .into(detailImage);
    }
}
    