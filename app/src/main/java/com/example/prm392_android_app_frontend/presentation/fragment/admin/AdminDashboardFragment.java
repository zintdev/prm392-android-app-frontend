package com.example.prm392_android_app_frontend.presentation.fragment.admin;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.databinding.FragmentAdminDashboardBinding;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.presentation.viewmodel.ProductViewModel;
import com.example.prm392_android_app_frontend.presentation.viewmodel.OrderViewModel;
import com.example.prm392_android_app_frontend.utils.PriceFormatter;

import java.text.NumberFormat;
import java.util.List;

public class AdminDashboardFragment extends Fragment {

    // 1. Tạo Interface
    public interface AdminDashboardListener {
        void navigateToProductManagement();
        void navigateToPublisherManagement();
        void navigateToAuthorManagement();
        void navigateToCategoryManagement();
    }

    private FragmentAdminDashboardBinding binding;
    private AdminDashboardListener listener;

    // Kiểm tra xem Activity chứa fragment có implement interface không
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AdminDashboardListener) {
            listener = (AdminDashboardListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AdminDashboardListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load and display total product quantity
        ProductViewModel productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        productViewModel.getProductList().observe(getViewLifecycleOwner(), products -> updateTotalQuantity(products));
        productViewModel.fetchAllProducts();

        // Load and display total orders count
        OrderViewModel orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        orderViewModel.getOrdersListLiveData().observe(getViewLifecycleOwner(), orders -> {
            if (binding == null) return;
            int count = orders != null ? orders.size() : 0;
            binding.textViewOrderCount.setText(NumberFormat.getIntegerInstance().format(count));
            Log.d("OrderCountDebug", "orders.size() = " + (orders != null ? orders.size() : -1));

            // Tính tổng doanh thu các đơn đã thanh toán (PAID)
            double paidTotal = 0.0;
            if (orders != null) {
                for (com.example.prm392_android_app_frontend.data.dto.OrderDTO o : orders) {
                    if (o != null && o.getStatus() != null && o.getStatus().equalsIgnoreCase("PAID")) {
                        paidTotal += o.getTotalAmount();
                    }
                }
            }
            binding.textViewTotalRevenue.setText(PriceFormatter.formatPriceShort(paidTotal));
        });
        orderViewModel.getAllOrders();

        binding.buttonProductManagement.setOnClickListener(v -> {
            // 3. Gọi phương thức của listener
            if (listener != null) {
                listener.navigateToProductManagement();
            }
        });

        binding.buttonPublisherManagement.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chuẩn bị gọi API Quản lý Nhà xuất bản", Toast.LENGTH_SHORT).show();
        });

        binding.buttonAuthorManagement.setOnClickListener(v -> {
            if (listener != null) {
                listener.navigateToAuthorManagement();
            }
        });

        binding.buttonCategoryManagement.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chuẩn bị gọi API Quản lý Danh mục", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null; // Dọn dẹp listener
    }

    private void updateTotalQuantity(@Nullable List<ProductDto> products) {
        if (binding == null) return;
        int total = 0;
        if (products != null) {
            for (ProductDto p : products) {
                total += p != null ? p.getQuantity() : 0;
            }
        }
        // Format with grouping (e.g., 1,234)
        binding.textViewProductCount.setText(NumberFormat.getIntegerInstance().format(total));
    }
}
