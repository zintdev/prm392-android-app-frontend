package com.example.prm392_android_app_frontend.presentation.fragment;

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

import java.text.DecimalFormat;
import java.util.Collections;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemActionListener {

    private CartViewModel cartViewModel;
    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;
    private ProgressBar progressBar;
    private TextView textViewEmptyCart;
    private TextView textViewTotalPrice;
    private View bottomBar; // Thanh chứa tổng tiền và nút thanh toán
    private MaterialButton buttonCheckout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout "fragment_cart.xml" vào Fragment này.
        // Tuyệt đối không viết thêm logic nào sau lệnh return này.
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
            if (cartDto != null && cartDto.getItems() != null && !cartDto.getItems().isEmpty()) {
                // Có sản phẩm trong giỏ hàng, hiển thị các view cần thiết
                textViewEmptyCart.setVisibility(View.GONE);
                bottomBar.setVisibility(View.VISIBLE);
                recyclerViewCart.setVisibility(View.VISIBLE);

                // Cập nhật danh sách sản phẩm cho Adapter
                cartAdapter.submitList(cartDto.getItems());


                DecimalFormat formatter = new DecimalFormat("###,###,###");
                String formattedPrice = formatter.format(cartDto.getGrandTotal());
                // Sử dụng String.format để tạo chuỗi cuối cùng một cách an toàn, tránh cảnh báo màu vàng
                textViewTotalPrice.setText(String.format("%sđ", formattedPrice));

            } else {
                // Giỏ hàng trống hoặc có lỗi, hiển thị thông báo
                textViewEmptyCart.setVisibility(View.VISIBLE);
                bottomBar.setVisibility(View.GONE);
                recyclerViewCart.setVisibility(View.GONE);
                cartAdapter.submitList(Collections.emptyList()); // Xóa danh sách hiện tại trong adapter
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
            }
        });
    }



    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            textViewEmptyCart.setVisibility(View.GONE);
            recyclerViewCart.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            // Các view khác sẽ được quản lý trong observeViewModel
        }
    }

    // --- Implement các phương thức từ interface của Adapter ---

    @Override
    public void onIncreaseQuantity(CartItemDto item) {
        // TODO: Gọi ViewModel để tăng số lượng trên server
        // Ví dụ: cartViewModel.updateItemQuantity(item.getProductId(), item.getQuantity() + 1);
        Toast.makeText(getContext(), "Tăng số lượng cho: " + item.getProductName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDecreaseQuantity(CartItemDto item) {
        // TODO: Gọi ViewModel để giảm số lượng trên server
        // Ví dụ: cartViewModel.updateItemQuantity(item.getProductId(),  item.getQuantity() - 1);
        Toast.makeText(getContext(), "Giảm số lượng cho: " + item.getProductName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRemoveItem(CartItemDto item) {
        // TODO: Gọi ViewModel để xóa sản phẩm khỏi giỏ hàng trên server
        // Ví dụ: cartViewModel.removeItemFromCart(item.getProductId());
        Toast.makeText(getContext(), "Xóa: " + item.getProductName(), Toast.LENGTH_SHORT).show();
    }
}
