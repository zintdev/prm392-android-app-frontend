package com.example.prm392_android_app_frontend.presentation.fragment.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.data.dto.CartDto;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.CartItemDto;
import com.example.prm392_android_app_frontend.presentation.adapter.CartAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.CartViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemActionListener {

    private CartViewModel cartViewModel;
    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;
    private ProgressBar progressBar;
    private View textViewEmptyCart; // Đổi từ TextView thành View vì layout dùng LinearLayout
    private TextView textViewTotalPrice;
    private View bottomBar; // Thanh chứa tổng tiền và nút thanh toán
    private MaterialButton buttonCheckout;
    private MaterialButton buttonClearCart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout "fragment_cart.xml" vào Fragment này.
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo các view từ layout
        initViews(view);

        // Khởi tạo ViewModel
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        // Thiết lập RecyclerView và Adapter
        setupRecyclerView();

        // Bắt đầu lắng nghe dữ liệu từ ViewModel
        observeViewModel();

        // Gửi yêu cầu để ViewModel tải dữ liệu giỏ hàng
        cartViewModel.fetchCart();
    }

    private void initViews(@NonNull View view) {
        recyclerViewCart = view.findViewById(R.id.recycler_view_cart);
        progressBar = view.findViewById(R.id.progress_bar_cart);
        textViewEmptyCart = view.findViewById(R.id.text_view_empty_cart);
        textViewTotalPrice = view.findViewById(R.id.text_view_total_price);
        bottomBar = view.findViewById(R.id.bottom_bar_cart);
        buttonCheckout = view.findViewById(R.id.button_checkout);
        buttonClearCart = view.findViewById(R.id.button_clear_cart);

        android.widget.CheckBox checkboxSelectAll = view.findViewById(R.id.checkbox_select_all);
        checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                showLoading(true);
                cartViewModel.selectAllItems(isChecked);
                cartAdapter.setSelectAll(isChecked);
                String message = isChecked ? "Đã chọn tất cả sản phẩm" : "Đã bỏ chọn tất cả sản phẩm";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý sự kiện khi người dùng nhấn nút "Thanh toán"
        buttonCheckout.setOnClickListener(v -> {
            CartDto currentCart = cartViewModel.getCartLiveData().getValue();

            if (currentCart != null && currentCart.getItems() != null && !currentCart.getItems().isEmpty()) {
                List<CartItemDto> selectedItems = new ArrayList<>();
                double totalAmount = 0;

                for (CartItemDto item : currentCart.getItems()) {
                    if (cartAdapter.isItemChecked(item.getCartItemId())) {
                        selectedItems.add(item);
                        totalAmount += item.getUnitPrice() * item.getQuantity();
                    }
                }

                if (!selectedItems.isEmpty()) {
                    // Chuyển sang trang tạo đơn hàng (LOGIC GỐC)
                    Intent intent = new Intent(getActivity(), com.example.prm392_android_app_frontend.presentation.activity.OrderCreateActivity.class);
                    intent.putParcelableArrayListExtra("selected_items", new ArrayList<>(selectedItems));
                    intent.putExtra("total_amount", totalAmount);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Vui lòng chọn ít nhất một sản phẩm để thanh toán!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Giỏ hàng của bạn đang trống!", Toast.LENGTH_SHORT).show();
            }
        });

        buttonClearCart.setOnClickListener(v -> {
            CartDto currentCart = cartViewModel.getCartLiveData().getValue();
            if (currentCart != null && currentCart.getItems() != null && !currentCart.getItems().isEmpty()) {
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa tất cả sản phẩm trong giỏ hàng?")
                        .setPositiveButton("Xóa tất cả", (dialog, which) -> {
                            cartViewModel.deleteCart();
                            Toast.makeText(getContext(), "Đang xóa tất cả sản phẩm...", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            } else {
                Toast.makeText(getContext(), "Giỏ hàng của bạn đang trống!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this);
        recyclerViewCart.setAdapter(cartAdapter);
    }

    private void observeViewModel() {
        showLoading(true);

        cartViewModel.getCartLiveData().observe(getViewLifecycleOwner(), cartDto -> {
            showLoading(false);
            try {
                if (cartDto != null && cartDto.getItems() != null && !cartDto.getItems().isEmpty()) {
                    textViewEmptyCart.setVisibility(View.GONE);
                    bottomBar.setVisibility(View.VISIBLE);
                    recyclerViewCart.setVisibility(View.VISIBLE);
                    buttonClearCart.setVisibility(View.VISIBLE);

                    cartAdapter.submitList(cartDto.getItems());
                    updateTotalPrice();

                } else {
                    textViewEmptyCart.setVisibility(View.VISIBLE);
                    bottomBar.setVisibility(View.GONE);
                    recyclerViewCart.setVisibility(View.GONE);
                    buttonClearCart.setVisibility(View.GONE);
                    cartAdapter.submitList(Collections.emptyList());
                    textViewTotalPrice.setText("0đ");
                }
            } catch (Exception e) {
                android.util.Log.e("CartFragment", "Error updating cart UI: " + e.getMessage());
                textViewEmptyCart.setVisibility(View.VISIBLE);
                bottomBar.setVisibility(View.GONE);
                recyclerViewCart.setVisibility(View.GONE);
                buttonClearCart.setVisibility(View.GONE);
                cartAdapter.submitList(Collections.emptyList());
                textViewTotalPrice.setText("0đ");
            }
        });

        cartViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            showLoading(false);
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                textViewEmptyCart.setVisibility(View.VISIBLE);
                bottomBar.setVisibility(View.GONE);
                recyclerViewCart.setVisibility(View.GONE);
                buttonClearCart.setVisibility(View.GONE);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            textViewEmptyCart.setVisibility(View.GONE);
            recyclerViewCart.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
            buttonClearCart.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onIncreaseQuantity(CartItemDto item) {
        try {
            cartViewModel.updateItemQuantity(item.getCartItemId(), 1);
            item.setQuantity(item.getQuantity() + 1);
            if (cartAdapter.isItemChecked(item.getCartItemId())) {
                updateTotalPrice();
            }
            Toast.makeText(getContext(), "Tăng số lượng cho: " + item.getProductName(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            android.util.Log.e("CartFragment", "Error increasing quantity: " + e.getMessage());
            Toast.makeText(getContext(), "Lỗi khi tăng số lượng", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDecreaseQuantity(CartItemDto item) {
        if (item.getQuantity() > 1) {
            try {
                cartViewModel.updateItemQuantity(item.getCartItemId(), -1);
                item.setQuantity(item.getQuantity() - 1);
                if (cartAdapter.isItemChecked(item.getCartItemId())) {
                    updateTotalPrice();
                }
                Toast.makeText(getContext(), "Giảm số lượng cho: " + item.getProductName(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                android.util.Log.e("CartFragment", "Error decreasing quantity: " + e.getMessage());
                Toast.makeText(getContext(), "Lỗi khi giảm số lượng", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRemoveItem(CartItemDto item) {
        cartViewModel.removeItemFromCart(item.getCartItemId());
        Toast.makeText(getContext(), "Xóa: " + item.getProductName(), Toast.LENGTH_SHORT).show();
    }

    private void updateTotalPrice() {
        try {
            double total = 0;
            if (cartAdapter != null) {
                total = cartAdapter.getCheckedItemsTotal();
            }
            DecimalFormat formatter = new DecimalFormat("###,###,###");
            String formattedPrice = formatter.format(total);
            if (textViewTotalPrice != null) {
                textViewTotalPrice.setText(String.format("%sđ", formattedPrice));
            }
        } catch (Exception e) {
            android.util.Log.e("CartFragment", "Error updating total price: " + e.getMessage());
            if (textViewTotalPrice != null) {
                textViewTotalPrice.setText("0đ");
            }
        }
    }

    @Override
    public void onItemCheckedChanged(CartItemDto item, boolean isChecked) {
        updateTotalPrice();
    }

    public CartViewModel getViewModel() {
        return cartViewModel;
    }

    @Override
    public void onQuantityChanged(CartItemDto item, int newQuantity) {
        try {
            int oldQuantity = item.getQuantity();
            item.setQuantity(newQuantity);
            int change = newQuantity - oldQuantity;
            cartViewModel.updateItemQuantity(item.getCartItemId(), change);
            if (cartAdapter.isItemChecked(item.getCartItemId())) {
                updateTotalPrice();
            }
            String message = String.format("Đã cập nhật số lượng thành %d", newQuantity);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            android.util.Log.e("CartFragment", "Error updating quantity: " + e.getMessage());
            Toast.makeText(getContext(), "Lỗi cập nhật số lượng", Toast.LENGTH_SHORT).show();
        }
    }
}
