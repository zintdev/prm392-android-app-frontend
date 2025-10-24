package com.example.prm392_android_app_frontend.presentation.viewmodel;

// === SỬA Ở ĐÂY: Dùng AndroidViewModel để có thể truy cập Application Context ===
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.prm392_android_app_frontend.data.dto.UpdateCartItemRequest;
import com.example.prm392_android_app_frontend.data.dto.CartDto;
import com.example.prm392_android_app_frontend.data.repository.CartRepository;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.ShopService;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// === SỬA Ở ĐÂY: Kế thừa từ AndroidViewModel ===
public class CartViewModel extends androidx.lifecycle.AndroidViewModel {

    private final CartRepository cartRepository;
    private final MutableLiveData<CartDto> cartLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // === SỬA Ở ĐÂY: Constructor của AndroidViewModel ===
    public CartViewModel(@NonNull Application application) {
        super(application);
        ShopService shopService = ApiClient.getAuthClient(application).create(ShopService.class);
        this.cartRepository = new CartRepository(shopService);
    }

    public LiveData<CartDto> getCartLiveData() {
        return cartLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void addProductToCart(int productId, int quantity) {
        // BÂY GIỜ KHÔNG CẦN TRUYỀN TOKEN NỮA
        cartRepository.addToCart(productId, quantity, new Callback<CartDto>() {
            @Override
            public void onResponse(Call<CartDto> call, Response<CartDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cartLiveData.postValue(response.body());
                } else {
                    errorMessage.postValue("Failed to add product. Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CartDto> call, Throwable t) {
                errorMessage.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void fetchCart() {
        // BÂY GIỜ KHÔNG CẦN TRUYỀN TOKEN NỮA
        cartRepository.getCart(new Callback<CartDto>() {
            @Override
            public void onResponse(Call<CartDto> call, Response<CartDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cartLiveData.postValue(response.body());
                } else {
                    errorMessage.postValue("Failed to fetch cart. Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CartDto> call, Throwable t) {
                errorMessage.postValue("Network error: " + t.getMessage());
            }
        });
    }
}
