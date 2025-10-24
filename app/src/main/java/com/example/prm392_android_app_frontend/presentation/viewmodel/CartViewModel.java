package com.example.prm392_android_app_frontend.presentation.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.data.dto.CartDto;
import com.example.prm392_android_app_frontend.data.dto.CartItemDto;
import com.example.prm392_android_app_frontend.data.dto.UpdateCartItemRequest;
import com.example.prm392_android_app_frontend.data.remote.api.CartApi;
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
        CartApi cartApi = ApiClient.getAuthClient(application).create(CartApi.class);
        this.cartRepository = new CartRepository(cartApi);
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

    public void removeItemFromCart(int CartItemId) {
        isLoading.postValue(true);
        cartRepository.removeItemFromCart(CartItemId, new Callback<CartDto>() {
            @Override
            public void onResponse(@NonNull Call<CartDto> call, @NonNull Response<CartDto> response) {
                if (response.isSuccessful()) {
                    // Sau khi xóa thành công, gọi fetchCart() để lấy dữ liệu mới nhất
                    fetchCart();
                } else {
                    errorMessage.postValue("Lỗi xóa sản phẩm. Mã: " + response.code());
                    isLoading.postValue(false);
                }
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


    public void updateItemQuantity(int CartItemId, int quantityChange) {
        isLoading.postValue(true);

        // Lấy thông tin cart hiện tại để biết selected status
        CartDto currentCart = cartLiveData.getValue();
        boolean isSelected = false;
        
        // Kiểm tra xem item có đang được chọn không
        if (currentCart != null && currentCart.getItems() != null) {
            for (CartItemDto item : currentCart.getItems()) {
                if (item.getCartItemId() == CartItemId) {
                    isSelected = item.isSelected();
                    break;
                }
            }
        }

        // Tạo request với cả selected status
        UpdateCartItemRequest request = new UpdateCartItemRequest(quantityChange, isSelected);
        
        // Gọi repository để update
        cartRepository.updateItemQuantity(CartItemId, request, new Callback<CartDto>() {
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

    public void updateItemSelected(int CartItemId, boolean selected) {
        isLoading.postValue(true);
        cartRepository.updateItemSelected(CartItemId, selected, new Callback<CartDto>() {
            @Override
            public void onResponse(@NonNull Call<CartDto> call, @NonNull Response<CartDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cartLiveData.postValue(response.body()); // Cập nhật lại giỏ hàng
                } else {
                    errorMessage.postValue("Lỗi cập nhật trạng thái chọn. Mã: " + response.code());
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

    public void deleteCart() {
        isLoading.postValue(true);
        cartRepository.deleteCart(new Callback<CartDto>() {
            @Override
            public void onResponse(@NonNull Call<CartDto> call, @NonNull Response<CartDto> response) {
                if (response.isSuccessful()) {
                    // Xóa thành công, tạo một CartDto trống để cập nhật UI
                    CartDto emptyCart = new CartDto();
                    emptyCart.setItems(new java.util.ArrayList<com.example.prm392_android_app_frontend.data.dto.CartItemDto>());
                    emptyCart.setGrandTotal(0.0);
                    emptyCart.setSubtotal(0.0);
                    emptyCart.setTaxTotal(0.0);
                    emptyCart.setShippingFee(0.0);
                    emptyCart.setStatus("empty");
                    cartLiveData.postValue(emptyCart);
                } else {
                    errorMessage.postValue("Lỗi xóa giỏ hàng. Mã: " + response.code());
                }
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<CartDto> call, @NonNull Throwable t) {
                errorMessage.postValue("Lỗi mạng khi xóa giỏ hàng: " + t.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void selectAllItems(boolean selected) {
        isLoading.postValue(true);
        cartRepository.selectAllItems(selected, new Callback<CartDto>() {
            @Override
            public void onResponse(@NonNull Call<CartDto> call, @NonNull Response<CartDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cartLiveData.postValue(response.body());
                } else {
                    errorMessage.postValue("Lỗi khi chọn tất cả sản phẩm. Mã: " + response.code());
                }
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<CartDto> call, @NonNull Throwable t) {
                errorMessage.postValue("Lỗi mạng khi chọn tất cả sản phẩm: " + t.getMessage());
                isLoading.postValue(false);
            }
        });
    }
}
