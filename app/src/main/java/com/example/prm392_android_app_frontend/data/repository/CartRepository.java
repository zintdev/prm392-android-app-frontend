package com.example.prm392_android_app_frontend.data.repository;

import com.example.prm392_android_app_frontend.data.dto.AddToCartRequestDto;
import com.example.prm392_android_app_frontend.data.dto.CartDto;
import com.example.prm392_android_app_frontend.data.remote.api.ApiService;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Lớp Repository để xử lý các hoạt động liên quan đến giỏ hàng.
 * Nó đóng gói việc gọi API từ ViewModel.
 */
public class CartRepository {

    // Phải là ApiService của dự án, không phải của Firebase.
    private final ApiService apiService;

    public CartRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public void addToCart(int productId, int quantity, Callback<CartDto> callback) {
        // Tạo đối tượng request body
        AddToCartRequestDto request = new AddToCartRequestDto(productId, quantity);

        // Gọi phương thức từ ApiService, phương thức này trả về một retrofit2.Call
        Call<CartDto> call = apiService.addToCart(request);

        // Thực hiện yêu cầu bất đồng bộ
        call.enqueue(callback);
    }

    public void getCart(Callback<CartDto> callback) {
        // Gọi phương thức từ ApiService
        Call<CartDto> call = apiService.getCart();

        // Thực hiện yêu cầu bất đồng bộ
        call.enqueue(callback);
    }
}
