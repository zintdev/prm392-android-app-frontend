package com.example.prm392_android_app_frontend.presentation.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.data.dto.CartDto;
import com.example.prm392_android_app_frontend.data.remote.api.ShopApi;
import com.example.prm392_android_app_frontend.data.repository.CartRepository;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartViewModel extends AndroidViewModel {

    private final CartRepository cartRepository;

    private final MutableLiveData<CartDto> cartLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public CartViewModel(@NonNull Application application) {
        super(application);
        ShopApi shopApi = ApiClient.getAuthClient(application).create(ShopApi.class);
        this.cartRepository = new CartRepository(shopApi);
    }

    // --- Getters cho LiveData ---
    public LiveData<CartDto> getCartLiveData() {
        return cartLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // --- Các phương thức hành động ---

    public void fetchCart() {
        isLoading.postValue(true);
        cartRepository.getCart(new Callback<CartDto>() {
            @Override
            public void onResponse(@NonNull Call<CartDto> call, @NonNull Response<CartDto> response) {
                if (response.isSuccessful()) {
                    cartLiveData.postValue(response.body());
                } else {
                    errorMessage.postValue("Lỗi tải giỏ hàng: " + response.code());
                }
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<CartDto> call, @NonNull Throwable t) {
                errorMessage.postValue("Lỗi mạng: " + t.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void removeItemFromCart(int productId) {
        isLoading.postValue(true);
        cartRepository.removeItemFromCart(productId, new Callback<CartDto>() {
            @Override
            public void onResponse(@NonNull Call<CartDto> call, @NonNull Response<CartDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cartLiveData.postValue(response.body());
                } else {
                    errorMessage.postValue("Lỗi xóa sản phẩm. Mã: " + response.code());
                }
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<CartDto> call, @NonNull Throwable t) {
                errorMessage.postValue("Lỗi mạng khi xóa sản phẩm: " + t.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    public void addProductToCart(int productId, int quantity) {
        isLoading.postValue(true);
        cartRepository.addProductToCart(productId, quantity, new Callback<CartDto>() {
            @Override
            public void onResponse(@NonNull Call<CartDto> call, @NonNull Response<CartDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Cập nhật giỏ hàng với dữ liệu mới nhận về
                    // Hoặc có thể tạo một LiveData riêng để báo thành công
                    cartLiveData.postValue(response.body());
                } else {
                    errorMessage.postValue("Lỗi thêm vào giỏ hàng. Mã: " + response.code());
                }
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<CartDto> call, @NonNull Throwable t) {
                errorMessage.postValue("Lỗi mạng: " + t.getMessage());
                isLoading.postValue(false);
            }
        });
    }


    public void updateItemQuantity(int productId, int newQuantity) {
        isLoading.postValue(true);
        // Gọi đúng phương thức updateItemQuantity từ repository
        cartRepository.updateItemQuantity(productId, newQuantity, new Callback<CartDto>() {
            @Override
            public void onResponse(@NonNull Call<CartDto> call, @NonNull Response<CartDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cartLiveData.postValue(response.body()); // Cập nhật lại giỏ hàng
                } else {
                    errorMessage.postValue("Lỗi cập nhật số lượng. Mã: " + response.code());
                }
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<CartDto> call, @NonNull Throwable t) {
                errorMessage.postValue("Lỗi mạng: " + t.getMessage());
                isLoading.postValue(false);
            }
        });
    }
}
