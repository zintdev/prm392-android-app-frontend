package com.example.prm392_android_app_frontend.presentation.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.ApiService;
import com.example.prm392_android_app_frontend.data.repository.ProductRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductViewModel extends AndroidViewModel {

    private final ProductRepository productRepository;

    // LiveData cho danh sách sản phẩm (dùng cho màn hình Home/Danh sách)
    private final MutableLiveData<List<ProductDto>> productList = new MutableLiveData<>();

    // LiveData cho sản phẩm chi tiết đang được chọn
    private final MutableLiveData<ProductDto> selectedProduct = new MutableLiveData<>();

    // LiveData để thông báo lỗi cho UI
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProductViewModel(@NonNull Application application) {
        super(application);
        // Các API để lấy thông tin sản phẩm thường không yêu cầu xác thực,
        // vì vậy chúng ta dùng client thông thường: ApiClient.get()
        ApiService apiService = ApiClient.get().create(ApiService.class);

        // Khởi tạo Repository với ApiService tương ứng
        this.productRepository = new ProductRepository(apiService);
    }

    // --- Getters để UI có thể observe một cách an toàn ---

    public LiveData<List<ProductDto>> getProductList() {
        return productList;
    }

    public LiveData<ProductDto> getSelectedProduct() {
        return selectedProduct;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }


    // --- Các phương thức để kích hoạt hành động từ UI ---

    /**
     * Tải toàn bộ danh sách sản phẩm từ API.
     * Kết quả sẽ được cập nhật vào `productList` LiveData.
     */
    public void fetchAllProducts() {
        productRepository.getAllProducts(new Callback<List<ProductDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductDto>> call, @NonNull Response<List<ProductDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productList.postValue(response.body());
                } else {
                    errorMessage.postValue("Lỗi tải danh sách sản phẩm. Mã lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductDto>> call, @NonNull Throwable t) {
                errorMessage.postValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    /**
     * Tải chi tiết một sản phẩm theo ID.
     * Kết quả sẽ được cập nhật vào `selectedProduct` LiveData.
     */
    public void fetchProductById(int productId) {
        productRepository.getProductById(productId, new Callback<ProductDto>() {
            @Override
            public void onResponse(@NonNull Call<ProductDto> call, @NonNull Response<ProductDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    selectedProduct.postValue(response.body());
                } else {
                    errorMessage.postValue("Lỗi tải chi tiết sản phẩm. Mã lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductDto> call, @NonNull Throwable t) {
                errorMessage.postValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }
}
