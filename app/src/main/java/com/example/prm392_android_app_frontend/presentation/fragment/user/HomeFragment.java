package com.example.prm392_android_app_frontend.presentation.fragment.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.presentation.activity.SearchProductActivity;
import com.example.prm392_android_app_frontend.presentation.adapter.ProductAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.ProductViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ProductViewModel productViewModel;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private ProgressBar progressBar;
    private View searchBar;
    
    // Filter chips
    private ChipGroup chipGroup;
    private Chip chipAll, chipNew, chipPromotion;
    
    // Data
    private List<ProductDto> allProducts = new ArrayList<>();
    private List<ProductDto> filteredProducts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // View refs
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView = view.findViewById(R.id.recyclerView_products);
        searchBar = view.findViewById(R.id.searchBar);
        
        // Filter chips
        chipGroup = view.findViewById(R.id.chipGroup);
        chipAll = view.findViewById(R.id.chipAll);
        chipNew = view.findViewById(R.id.chipNew);
        chipPromotion = view.findViewById(R.id.chipPromotion);

        // Search bar → mở SearchActivity (màn riêng)
        if (searchBar != null) {
            searchBar.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), SearchProductActivity.class))
            );
        }

        // RecyclerView + Adapter
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        productAdapter = new ProductAdapter();
        recyclerView.setAdapter(productAdapter);

        // Setup filter chips
        setupFilterChips();

        // ViewModel (dùng viewLifecycleOwner để observe đúng vòng đời)
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        observeViewModel();

        // show loading & fetch
        showLoading(true);
        productViewModel.fetchAllProducts();
    }

    private void observeViewModel() {
        // Dữ liệu sản phẩm
        productViewModel.getProductList().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                allProducts.clear();
                allProducts.addAll(products);
                applyCurrentFilter();
            }
            showLoading(false);
        });

        // Nếu ViewModel có expose trạng thái lỗi, có thể bật đoạn sau:
        // productViewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
        //     showLoading(false);
        //     if (msg != null && !msg.isEmpty()) {
        //         Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        //     }
        // });
    }

    private void setupFilterChips() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            applyCurrentFilter();
        });
    }

    private void applyCurrentFilter() {
        filteredProducts.clear();
        
        if (chipAll.isChecked() || chipGroup.getCheckedChipId() == View.NO_ID) {
            // Hiển thị tất cả sản phẩm
            filteredProducts.addAll(allProducts);
        } else if (chipNew.isChecked()) {
            // Lọc sản phẩm mới (có thể dựa vào ngày tạo hoặc flag)
            for (ProductDto product : allProducts) {
                // Logic lọc sản phẩm mới - ví dụ dựa vào tên có chứa "NEW" hoặc logic khác
                if (product.getName().toLowerCase().contains("new") || isNewProduct(product)) {
                    filteredProducts.add(product);
                }
            }
        } else if (chipPromotion.isChecked()) {
            // Lọc sản phẩm khuyến mãi
            for (ProductDto product : allProducts) {
                // Logic lọc sản phẩm khuyến mãi - có thể dựa vào giá giảm hoặc flag
                if (product.getName().toLowerCase().contains("sale") || isPromotionProduct(product)) {
                    filteredProducts.add(product);
                }
            }
        }
        
        productAdapter.setProducts(filteredProducts);
    }

    private boolean isNewProduct(ProductDto product) {
        // Logic để xác định sản phẩm mới
        // Ví dụ: sản phẩm được tạo trong 30 ngày gần đây
        return false; // Placeholder
    }

    private boolean isPromotionProduct(ProductDto product) {
        // Logic để xác định sản phẩm khuyến mãi
        // Ví dụ: sản phẩm có giá < 100000 hoặc có flag promotion
        return product.getPrice() < 100000;
    }

    private void showLoading(boolean show) {
        if (progressBar != null) progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Tránh memory leak
        if (recyclerView != null) recyclerView.setAdapter(null);
        recyclerView = null;
        productAdapter = null;
        progressBar = null;
        searchBar = null;
        chipGroup = null;
        chipAll = null;
        chipNew = null;
        chipPromotion = null;
        allProducts.clear();
        filteredProducts.clear();
    }
}
