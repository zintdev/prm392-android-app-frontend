package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.presentation.viewmodel.ProductViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminProductActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "PRODUCT_ID";

    // ViewModels
    private ProductViewModel productViewModel;

    // UI Components
    private ImageView detailImage;
    private TextView detailName, detailArtist, detailPrice, detailDescription, quantity, publisherName, categoryName, releaseDate;
    private ProgressBar progressBar;
    private MaterialButton buttonUpdateImage;

    private MaterialButton buttonEditProduct;

    private int productId;
    private ProductDto currentProduct;

    // ActivityResultLauncher để xử lý kết quả chọn ảnh
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri imageUri = result.getData().getData();
                    // Gọi ViewModel để thực hiện việc upload ảnh
                    productViewModel.uploadProductImage(productId, imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_admin_product_detail);

        productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
        if (productId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        initViewModel();
        setupClickListeners();
        observeViewModel();

        // Tải dữ liệu sản phẩm
        productViewModel.fetchProductById(productId);
    }


    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar_detail);
        toolbar.setTitle("Chi Tiết Sản Phẩm (Admin)");
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

        // ID của button trong layout item_admin_product_detail là button_update_product_image
        buttonUpdateImage = findViewById(R.id.button_update_product_image);
    }

    private void initViewModel() {
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
    }

    private void setupClickListeners() {
        buttonUpdateImage.setOnClickListener(v -> openImageChooser());
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageLauncher.launch(intent);
    }

    private void observeViewModel() {
        showLoading(true);

        // Lắng nghe dữ liệu sản phẩm chi tiết
        productViewModel.getSelectedProduct().observe(this, productDto -> {
            if (productDto != null) {
                hideLoading(true); // Ẩn loading nhưng vẫn giữ nút "Cập nhật" bật
                this.currentProduct = productDto;
                populateUI(productDto);
            }
        });

        // Lắng nghe trạng thái upload ảnh
        productViewModel.getImageUploadStatus().observe(this, isUploading -> {
            if (isUploading) {
                Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();
                showLoading(true); // Hiển thị loading khi đang upload
            }
        });

        // Lắng nghe lỗi
        productViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                hideLoading(true);
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
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
                .placeholder(R.drawable.ic_placeholder) // Ảnh giữ chỗ
                .error(R.drawable.ic_error)         // Ảnh khi lỗi
                .into(detailImage);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        // Nút cập nhật chỉ bị vô hiệu hóa khi đang loading
        buttonUpdateImage.setEnabled(!isLoading);
    }

    private void hideLoading(boolean keepButtonEnabled) {
        progressBar.setVisibility(View.GONE);
        buttonUpdateImage.setEnabled(keepButtonEnabled);
    }

    private String formatDate(String dateString) {
        if (dateString == null) return "N/A";
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = inputFormat.parse(dateString);
            return (date != null) ? outputFormat.format(date) : dateString;
        } catch (ParseException e) {
            // Nếu parse lỗi, thử trả về phần ngày tháng
            if (dateString.contains("T")) {
                return dateString.split("T")[0];
            }
            return dateString;
        }
    }
}
