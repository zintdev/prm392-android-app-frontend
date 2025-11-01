package com.example.prm392_android_app_frontend.presentation.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.databinding.ActivityManageProductBinding;
import com.example.prm392_android_app_frontend.presentation.adapter.ProductManageAdapter;
import com.example.prm392_android_app_frontend.presentation.fragment.admin.AddAndUpdateProductFragment;
import com.example.prm392_android_app_frontend.presentation.viewmodel.ProductViewModel;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.google.android.material.appbar.MaterialToolbar;

public class ProductManagementActivity extends AppCompatActivity {

    private ActivityManageProductBinding binding;
    private ProductViewModel productViewModel;
    private ProductManageAdapter productManageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        initViewModel();
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
        
        // Fetch products
        productViewModel.fetchAllProducts();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViewModel() {
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
    }

    private void setupRecyclerView() {
        productManageAdapter = new ProductManageAdapter();
        binding.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewProducts.setAdapter(productManageAdapter);

        // Set callbacks for adapter
        productManageAdapter.setOnEditClickListener(product -> {
            showEditProductDialog(product);
        });

        productManageAdapter.setOnDeleteClickListener((productId, productName) -> {
            showDeleteConfirmDialog(productId, productName);
        });
    }

    private void setupClickListeners() {
        binding.fabAddProduct.setOnClickListener(v -> {
            // Mở DialogFragment để thêm sản phẩm
            AddAndUpdateProductFragment dialog = new AddAndUpdateProductFragment();
            dialog.setOnProductCreatedListener(product -> {
                // Refresh product list after creation
                productViewModel.fetchAllProducts();
            });
            dialog.show(getSupportFragmentManager(), "AddProductDialog");
        });
    }

    private void observeViewModel() {
        // Observe product list
        productViewModel.getProductList().observe(this, products -> {
            if (products != null) {
                productManageAdapter.setProducts(products);
            }
        });

        // Observe error messages
        productViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Hiển thị dialog xác nhận xóa sản phẩm
     */
    private void showDeleteConfirmDialog(int productId, String productName) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm \"" + productName + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    productViewModel.deleteProduct(productId);
                    Toast.makeText(this, "Đang xóa sản phẩm...", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Hiển thị dialog chỉnh sửa sản phẩm
     */
    private void showEditProductDialog(ProductDto product) {
        // Sử dụng AddAndUpdateProductFragment cho edit mode
        AddAndUpdateProductFragment editDialog = new AddAndUpdateProductFragment();
        Bundle args = new Bundle();
        args.putSerializable("product_to_edit", product);
        editDialog.setArguments(args);
        editDialog.setOnProductCreatedListener(updatedProduct -> {
            // Refresh product list after update
            productViewModel.fetchAllProducts();
        });
        editDialog.show(getSupportFragmentManager(), "edit_product");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

