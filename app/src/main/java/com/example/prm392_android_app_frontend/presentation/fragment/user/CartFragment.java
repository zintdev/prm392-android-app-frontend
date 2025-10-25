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
import com.example.prm392_android_app_frontend.presentation.activity.LoginActivity;
import com.example.prm392_android_app_frontend.presentation.adapter.CartAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.CartViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DecimalFormat;
import java.util.Collections;

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
        // Kiểm tra đăng nhập ngay từ đầu
        if (!TokenStore.isLoggedIn(requireContext())) {
            // Hiển thị thông báo và chuyển đến trang đăng nhập
            Toast.makeText(requireContext(), "Bạn cần đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            // Trả về view trống để không hiển thị gì
            return new View(requireContext());
        }
        
        // Gắn layout "fragment_cart.xml" vào Fragment này.
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Kiểm tra lại lần nữa để đảm bảo
        if (!TokenStore.isLoggedIn(requireContext())) {
            return;
        }

        // Khởi tạo các view từ layout
        initViews(view);

        // Khởi tạo ViewModel
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

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
                // Hiển thị loading
                showLoading(true);
                
                // Gọi API để select/deselect all items trên server
                cartViewModel.selectAllItems(isChecked);
                
                // Cập nhật UI local
                cartAdapter.setSelectAll(isChecked);
                
                // Hiển thị thông báo
                String message = isChecked ? "Đã chọn tất cả sản phẩm" : "Đã bỏ chọn tất cả sản phẩm";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý sự kiện khi người dùng nhấn nút "Thanh toán"
        buttonCheckout.setOnClickListener(v -> {
            // Lấy giỏ hàng hiện tại từ LiveData để kiểm tra
            CartDto currentCart = cartViewModel.getCartLiveData().getValue();


            if (currentCart != null && currentCart.getItems() != null && !currentCart.getItems().isEmpty()) {
                Toast.makeText(getContext(), "Chuyển đến màn hình thanh toán...", Toast.LENGTH_SHORT).show();
                android.content.Intent intent = new android.content.Intent(getActivity(), com.example.prm392_android_app_frontend.presentation.activity.CheckoutActivity.class);

                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Giỏ hàng của bạn đang trống!", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý sự kiện khi người dùng nhấn nút "Hủy tất cả"
        buttonClearCart.setOnClickListener(v -> {
            // Lấy giỏ hàng hiện tại để kiểm tra
            CartDto currentCart = cartViewModel.getCartLiveData().getValue();
            
            if (currentCart != null && currentCart.getItems() != null && !currentCart.getItems().isEmpty()) {
                // Hiển thị dialog xác nhận
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa tất cả sản phẩm trong giỏ hàng?")
                        .setPositiveButton("Xóa tất cả", (dialog, which) -> {
                            // Gọi ViewModel để xóa toàn bộ giỏ hàng
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
        // Khởi tạo Adapter và truyền "this" vào vì Fragment này đã implement OnCartItemActionListener
        cartAdapter = new CartAdapter(this);
        recyclerViewCart.setAdapter(cartAdapter);
    }

    private void observeViewModel() {
        showLoading(true);

        // Lắng nghe dữ liệu giỏ hàng trả về thành công
        cartViewModel.getCartLiveData().observe(getViewLifecycleOwner(), cartDto -> {
            showLoading(false);
            
            try {
                if (cartDto != null && cartDto.getItems() != null && !cartDto.getItems().isEmpty()) {
                    // Có sản phẩm trong giỏ hàng, hiển thị các view cần thiết
                    textViewEmptyCart.setVisibility(View.GONE);
                    bottomBar.setVisibility(View.VISIBLE);
                    recyclerViewCart.setVisibility(View.VISIBLE);
                    buttonClearCart.setVisibility(View.VISIBLE); // Hiển thị nút "Hủy tất cả"

                    // Cập nhật danh sách sản phẩm cho Adapter
                    cartAdapter.submitList(cartDto.getItems());

                    // Cập nhật tổng tiền dựa trên các item được chọn thay vì đặt về 0
                    updateTotalPrice();

                } else {
                    // Giỏ hàng trống hoặc có lỗi, hiển thị thông báo
                    textViewEmptyCart.setVisibility(View.VISIBLE);
                    bottomBar.setVisibility(View.GONE);
                    recyclerViewCart.setVisibility(View.GONE);
                    buttonClearCart.setVisibility(View.GONE); // Ẩn nút "Hủy tất cả"
                    cartAdapter.submitList(Collections.emptyList()); // Xóa danh sách hiện tại trong adapter
                    textViewTotalPrice.setText("0đ");
                }
            } catch (Exception e) {
                // Xử lý lỗi để tránh crash app
                android.util.Log.e("CartFragment", "Error updating cart UI: " + e.getMessage());
                textViewEmptyCart.setVisibility(View.VISIBLE);
                bottomBar.setVisibility(View.GONE);
                recyclerViewCart.setVisibility(View.GONE);
                buttonClearCart.setVisibility(View.GONE);
                cartAdapter.submitList(Collections.emptyList());
                textViewTotalPrice.setText("0đ");
            }
        });

        // Lắng nghe các thông báo lỗi từ ViewModel
        cartViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            showLoading(false);
            // Chỉ hiển thị Toast nếu có tin nhắn lỗi thực sự
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
                // Đồng thời cập nhật UI để hiển thị trạng thái trống khi có lỗi
                textViewEmptyCart.setVisibility(View.VISIBLE);
                bottomBar.setVisibility(View.GONE);
                recyclerViewCart.setVisibility(View.GONE);
                buttonClearCart.setVisibility(View.GONE); // Ẩn nút "Hủy tất cả" khi có lỗi
            }
        });
    }



    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            textViewEmptyCart.setVisibility(View.GONE);
            recyclerViewCart.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
            buttonClearCart.setVisibility(View.GONE); // Ẩn nút "Hủy tất cả" khi đang tải
        } else {
            progressBar.setVisibility(View.GONE);
            // Các view khác sẽ được quản lý trong observeViewModel
        }
    }

    // --- Implement các phương thức từ interface của Adapter ---

    @Override
    public void onIncreaseQuantity(CartItemDto item) {
        try {
            // Gửi request lên server để cập nhật số lượng
            cartViewModel.updateItemQuantity(item.getCartItemId(), 1);
            
            // Cập nhật UI local
            item.setQuantity(item.getQuantity() + 1);
            
            // Cập nhật tổng tiền nếu item được chọn
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
                // Gửi request lên server để cập nhật số lượng
                cartViewModel.updateItemQuantity(item.getCartItemId(), -1);
                
                // Cập nhật UI local
                item.setQuantity(item.getQuantity() - 1);
                
                // Cập nhật tổng tiền nếu item được chọn
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
        // TODO: Gọi ViewModel để xóa sản phẩm khỏi giỏ hàng trên server
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
            // Lưu số lượng cũ
            int oldQuantity = item.getQuantity();
            
            // Cập nhật số lượng mới
            item.setQuantity(newQuantity);
            
            // Tính toán sự thay đổi số lượng
            int change = newQuantity - oldQuantity;
            
            // Gửi request lên server
            cartViewModel.updateItemQuantity(item.getCartItemId(), change);
            
            // Cập nhật total price nếu item đang được chọn
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
