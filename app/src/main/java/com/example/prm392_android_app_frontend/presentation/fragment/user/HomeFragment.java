package com.example.prm392_android_app_frontend.presentation.fragment.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.presentation.adapter.ProductAdapter; // Tạo adapter này
import com.example.prm392_android_app_frontend.presentation.viewmodel.ProductViewModel; // Tạo ViewModel này

public class HomeFragment extends Fragment {

    private ProductViewModel productViewModel;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout cho fragment này
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo Views
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView = view.findViewById(R.id.recyclerView_products);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productAdapter = new ProductAdapter();
        recyclerView.setAdapter(productAdapter);

        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);

        // Bắt đầu quan sát dữ liệu
        observeViewModel();
        productViewModel.fetchAllProducts();

    }

    private void observeViewModel() {
        // Hiển thị loading
        progressBar.setVisibility(View.VISIBLE);

        // Lắng nghe danh sách sản phẩm
        productViewModel.getProductList().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                productAdapter.setProducts(products);
                // Ẩn loading khi có dữ liệu
                progressBar.setVisibility(View.GONE);
            }
        });

        // (Tùy chọn) Lắng nghe thông báo lỗi
        // productViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> { ... });
    }
}
