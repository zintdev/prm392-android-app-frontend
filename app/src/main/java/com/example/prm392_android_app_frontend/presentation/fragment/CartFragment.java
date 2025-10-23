package com.example.prm392_android_app_frontend.presentation.fragment;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.CartDto;
import com.example.prm392_android_app_frontend.data.dto.CartItemDto;
import com.example.prm392_android_app_frontend.presentation.activity.CheckoutActivity;
import com.example.prm392_android_app_frontend.presentation.adapter.CartAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.CartViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CartFragment extends Fragment implements CartAdapter.OnCartItemActionListener {

    private CartViewModel cartViewModel;
    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;
    private ProgressBar progressBar;
    private TextView textViewEmptyCart;
    private TextView textViewTotalPrice;
    private View bottomBar;
    private MaterialButton buttonCheckout;


    private List<CartItemDto> currentCartItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        setupRecyclerView();
        observeViewModel();
        cartViewModel.fetchCart();
    }

    private void initViews(@NonNull View view) {
        recyclerViewCart = view.findViewById(R.id.recycler_view_cart);
        progressBar = view.findViewById(R.id.progress_bar_cart);
        textViewEmptyCart = view.findViewById(R.id.text_view_empty_cart);
        textViewTotalPrice = view.findViewById(R.id.text_view_total_price);
        bottomBar = view.findViewById(R.id.bottom_bar_cart);
        buttonCheckout = view.findViewById(R.id.button_checkout);

        buttonCheckout.setOnClickListener(v -> {
            // Lọc ra danh sách các sản phẩm đã được chọn để gửi đi thanh toán
            ArrayList<CartItemDto> selectedItems = new ArrayList<>();
            for (CartItemDto item : currentCartItems) {
                if (item.isSelected()) {
                    selectedItems.add(item);
                }
            }

            if (!selectedItems.isEmpty()) {
                // TODO: Chuyển danh sách selectedItems sang CheckoutActivity
                Toast.makeText(getContext(), "Thanh toán " + selectedItems.size() + " sản phẩm...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), CheckoutActivity.class);
                // intent.putParcelableArrayListExtra("SELECTED_ITEMS", selectedItems); // Cần làm CartItemDto thành Parcelable
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn ít nhất một sản phẩm để thanh toán!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCart.setAdapter(cartAdapter);
    }

    private void observeViewModel() {
        // Tách riêng observer cho isLoading để code gọn hơn
        cartViewModel.getIsLoading().observe(getViewLifecycleOwner(), this::showLoading);


        cartViewModel.getCartLiveData().observe(getViewLifecycleOwner(), cartDto -> {
            if (cartDto != null && cartDto.getItems() != null && !cartDto.getItems().isEmpty()) {
                textViewEmptyCart.setVisibility(View.GONE);
                bottomBar.setVisibility(View.VISIBLE);
                recyclerViewCart.setVisibility(View.VISIBLE);

                // Cập nhật danh sách local và gửi cho adapter
                currentCartItems = new ArrayList<>(cartDto.getItems());
                cartAdapter.submitList(currentCartItems);

                // Tính toán lại tổng tiền ngay sau khi nhận dữ liệu
                updateTotalPrice();
            } else {
                // Xử lý khi giỏ hàng trống
                textViewEmptyCart.setVisibility(View.VISIBLE);
                bottomBar.setVisibility(View.GONE);
                recyclerViewCart.setVisibility(View.GONE);
                currentCartItems.clear();
                cartAdapter.submitList(Collections.emptyList());
                updateTotalPrice(); // Reset tổng tiền về 0
            }
        });

        cartViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
                // Khi có lỗi cũng ẩn hết view và reset
                textViewEmptyCart.setVisibility(View.VISIBLE);
                bottomBar.setVisibility(View.GONE);
                recyclerViewCart.setVisibility(View.GONE);
                currentCartItems.clear();
                cartAdapter.submitList(Collections.emptyList());
                updateTotalPrice();
            }
        });
    }


    private void updateTotalPrice() {
        long total = 0;
        // Duyệt qua danh sách local và chỉ cộng tiền những item được chọn
        for (CartItemDto item : currentCartItems) {
            if (item.isSelected()) {
                total += (long) item.getUnitPrice() * item.getQuantity();
            }
        }

        if (total > 0) {
            DecimalFormat formatter = new DecimalFormat("###,###,###");
            String formattedPrice = formatter.format(total);
            textViewTotalPrice.setText(String.format("%sđ", formattedPrice));
            buttonCheckout.setEnabled(true); // Bật nút thanh toán
            buttonCheckout.setAlpha(1.0f);
        } else {
            textViewTotalPrice.setText("0đ");
            buttonCheckout.setEnabled(false); // Tắt nút thanh toán nếu không có gì được chọn
            buttonCheckout.setAlpha(0.5f);
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerViewCart.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
            textViewEmptyCart.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    // --- Implement các phương thức từ interface của Adapter ---

    @Override
    public void onIncreaseQuantity(CartItemDto item) {
        cartViewModel.updateItemQuantity(item.getProductId(), item.getQuantity() + 1);
        // Tổng tiền sẽ được cập nhật tự động khi LiveData trả về kết quả
    }

    @Override
    public void onDecreaseQuantity(CartItemDto item) {
        if (item.getQuantity() > 1) {
            cartViewModel.updateItemQuantity(item.getProductId(), item.getQuantity() - 1);
        }
    }

    @Override
    public void onRemoveItem(CartItemDto item) {
        cartViewModel.removeItemFromCart(item.getProductId());
    }


    @Override
    public void onItemSelectionChanged(CartItemDto item, boolean isSelected) {
        // Cập nhật trạng thái 'selected' trong danh sách local
        item.setSelected(isSelected);
        // Tính toán lại tổng tiền ngay lập tức để người dùng thấy thay đổi
        updateTotalPrice();

        // (Nâng cao) Gửi trạng thái lên server để lưu lại lựa chọn của người dùng
        // Nếu bạn có API, hãy thêm phương thức mới vào ViewModel và gọi nó ở đây
        // cartViewModel.updateItemSelection(item.getProductId(), isSelected);
    }
}
