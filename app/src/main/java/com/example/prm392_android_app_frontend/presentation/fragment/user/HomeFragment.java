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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.presentation.activity.SearchProductActivity;
import com.example.prm392_android_app_frontend.presentation.adapter.ProductAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.ProductViewModel;

public class HomeFragment extends Fragment {

    private ProductViewModel productViewModel;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private ProgressBar progressBar;
    private View searchBar;

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

        // Search bar → mở SearchActivity (màn riêng)
        if (searchBar != null) {
            searchBar.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), SearchProductActivity.class))
            );
        }

        // RecyclerView + Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        productAdapter = new ProductAdapter();
        recyclerView.setAdapter(productAdapter);

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
                productAdapter.setProducts(products);
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
    }
}
