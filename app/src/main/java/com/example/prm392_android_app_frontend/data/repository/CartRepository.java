package com.example.prm392_android_app_frontend.data.repository;

import com.example.prm392_android_app_frontend.data.dto.AddToCartRequestDto;
import com.example.prm392_android_app_frontend.data.dto.CartDto;
import com.example.prm392_android_app_frontend.data.dto.UpdateCartItemRequest;
import com.example.prm392_android_app_frontend.data.remote.api.ShopService;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Lớp Repository để xử lý các hoạt động liên quan đến giỏ hàng.
 * Nó đóng gói việc gọi API từ ViewModel.
 */
public class CartRepository {

    // Phải là ApiService của dự án, không phải của Firebase.
    private final ShopService shopService;

    public CartRepository(ShopService shopService) {
        this.shopService = shopService;
    }

    public void addToCart(int productId, int quantity, Callback<CartDto> callback) {
        // Tạo đối tượng request body
        AddToCartRequestDto request = new AddToCartRequestDto(productId, quantity);

        // Gọi phương thức từ ApiService, phương thức này trả về một retrofit2.Call
        Call<CartDto> call = shopService.addToCart(request);

        // Thực hiện yêu cầu bất đồng bộ
        call.enqueue(callback);
    }
    public void removeItemFromCart(int productId, Callback<CartDto> callback) {
        shopService.removeItemFromCart(productId).enqueue(callback);

    }
    public void addProductToCart(int productId, int quantity, Callback<CartDto> callback) {
        AddToCartRequestDto requestBody = new AddToCartRequestDto(productId, quantity);
        shopService.addToCart(requestBody).enqueue(callback);
    }



    public void getCart(Callback<CartDto> callback) {
        // Gọi phương thức từ ApiService
        Call<CartDto> call = shopService.getCart();

        // Thực hiện yêu cầu bất đồng bộ
        call.enqueue(callback);
    }
    public void updateItemQuantity(int productId, int newQuantity, Callback<CartDto> callback) {
        // Tạo một đối tượng request body để gửi đi
        UpdateCartItemRequest requestBody = new UpdateCartItemRequest(newQuantity);
        // Gọi phương thức từ ShopService (tương đương ApiService)
        shopService.updateItemQuantity(productId, requestBody).enqueue(callback);
    }
}
