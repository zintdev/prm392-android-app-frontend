package com.example.prm392_android_app_frontend.presentation.fragment.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.presentation.activity.SearchProductActivity;
import com.example.prm392_android_app_frontend.presentation.adapter.ProductAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.CartViewModel;
import com.example.prm392_android_app_frontend.presentation.viewmodel.ProductViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment {

    private ProductViewModel productViewModel;
    private CartViewModel cartViewModel;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private ProgressBar progressBar;
    private View searchBar;
    private MaterialButton buttonToggleLayout;
    
    // Filter chips
    private ChipGroup chipGroup;
    private Chip chipAll, chipNew;
    
    // Layout mode: 0 = Grid 2 cột, 1 = List (horizontal), 2 = Grid 3 cột
    private int currentLayoutMode = 0;
    
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
        buttonToggleLayout = view.findViewById(R.id.buttonToggleLayout);
        
        // Filter chips
        chipGroup = view.findViewById(R.id.chipGroup);
        chipAll = view.findViewById(R.id.chipAll);
        chipNew = view.findViewById(R.id.chipNew);

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
        
        // Setup toggle layout button
        setupToggleLayoutButton();

        // ViewModel (dùng viewLifecycleOwner để observe đúng vòng đời)
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        // Setup adapter listener để xử lý thêm vào giỏ hàng
        productAdapter.setOnAddToCartClickListener((productId, quantity) -> {
            cartViewModel.addProductToCart(productId, quantity);
        });

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

        // Lắng nghe kết quả thêm vào giỏ hàng
        cartViewModel.getCartLiveData().observe(getViewLifecycleOwner(), cartDto -> {
            if (cartDto != null) {
                android.widget.Toast.makeText(requireContext(), 
                    "Đã thêm sản phẩm vào giỏ hàng thành công!", 
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // Lắng nghe lỗi từ CartViewModel
        cartViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), 
                    "Lỗi: " + error, 
                    android.widget.Toast.LENGTH_SHORT).show();
            }
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

    private void setupToggleLayoutButton() {
        if (buttonToggleLayout != null) {
            buttonToggleLayout.setOnClickListener(v -> {
                showLayoutModeMenu(v);
            });
        }
    }
    
    private void showLayoutModeMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        
        // Thêm các tùy chọn vào menu
        popup.getMenu().add(0, 0, 0, "Lưới 2 cột");
        popup.getMenu().add(0, 1, 1, "Danh sách ngang");
        popup.getMenu().add(0, 2, 2, "Lưới 3 cột");
        
        // Đánh dấu item hiện tại
        popup.getMenu().getItem(currentLayoutMode).setChecked(true);
        
        // Xử lý khi chọn
        popup.setOnMenuItemClickListener(item -> {
            int selectedMode = item.getItemId();
            if (selectedMode != currentLayoutMode) {
                currentLayoutMode = selectedMode;
                toggleLayout();
            }
            return true;
        });
        
        popup.show();
    }

    private void toggleLayout() {
        switch (currentLayoutMode) {
            case 0:
                // Grid 2 cột (item_product)
                recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
                productAdapter.setViewType(0);  // VIEW_TYPE_GRID_2
                if (buttonToggleLayout != null) {
                    buttonToggleLayout.setIcon(requireContext().getDrawable(android.R.drawable.ic_menu_sort_by_size));
                }
                break;
            case 1:
                // List (item_product_2)
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                productAdapter.setViewType(1);  // VIEW_TYPE_LIST
                if (buttonToggleLayout != null) {
                    buttonToggleLayout.setIcon(requireContext().getDrawable(android.R.drawable.ic_menu_view));
                }
                break;
            case 2:
                // Grid 3 cột (item_product_3)
                recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
                productAdapter.setViewType(2);  // VIEW_TYPE_GRID_3
                if (buttonToggleLayout != null) {
                    buttonToggleLayout.setIcon(requireContext().getDrawable(android.R.drawable.ic_dialog_dialer));
                }
                break;
        }
        
        // Adapter sẽ tự động refresh khi setViewType() được gọi
    }

    private void applyCurrentFilter() {
        filteredProducts.clear();
        
        if (chipAll.isChecked() || chipGroup.getCheckedChipId() == View.NO_ID) {
            // Hiển thị tất cả sản phẩm
            filteredProducts.addAll(allProducts);
        } else if (chipNew.isChecked()) {
            // Sắp xếp sản phẩm theo ID mới nhất (từ cao đến thấp)
            filteredProducts.addAll(allProducts);
            Collections.sort(filteredProducts, new Comparator<ProductDto>() {
                @Override
                public int compare(ProductDto p1, ProductDto p2) {
                    // Sắp xếp theo ID giảm dần (ID cao nhất = sản phẩm mới nhất)
                    return Integer.compare(p2.getId(), p1.getId());
                }
            });
        }
        
        productAdapter.setProducts(filteredProducts);
    }

    private boolean isNewProduct(ProductDto product) {
        // Logic để xác định sản phẩm mới
        // Ví dụ: sản phẩm được tạo trong 30 ngày gần đây
        return false; // Placeholder
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
        buttonToggleLayout = null;
        chipGroup = null;
        chipAll = null;
        chipNew = null;
        allProducts.clear();
        filteredProducts.clear();
    }
}
