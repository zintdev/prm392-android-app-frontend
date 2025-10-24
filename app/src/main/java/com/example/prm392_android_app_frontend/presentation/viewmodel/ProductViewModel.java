package com.example.prm392_android_app_frontend.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.core.util.Resource;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.data.dto.ProductFilter;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.ProductApi;
// import com.example.prm392_android_app_frontend.data.remote.api.ShopApi; // ❌ bỏ nếu không dùng
import com.example.prm392_android_app_frontend.data.repository.ProductRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductViewModel extends AndroidViewModel {

    private final ProductRepository productRepository;

    private final MutableLiveData<Resource<List<ProductDto>>> productsState = new MutableLiveData<>();
    public LiveData<Resource<List<ProductDto>>> getProductsState() { return productsState; }

    private final MutableLiveData<List<ProductDto>> productList = new MutableLiveData<>();
    private final MutableLiveData<ProductDto> selectedProduct = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProductViewModel(@NonNull Application application) {
        super(application);
        ProductApi productApi = ApiClient.get().create(ProductApi.class);
        this.productRepository = new ProductRepository(productApi);
    }

    // --- Getters để UI observe ---
    public LiveData<List<ProductDto>> getProductList() { return productList; }
    public LiveData<ProductDto> getSelectedProduct() { return selectedProduct; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // --- Danh sách tất cả sản phẩm ---
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

    // --- Lấy chi tiết theo id ---
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

    // --- 🔎 SEARCH: sửa duy nhất phần này để gọi đúng repository ---
    public void search(String name, ProductFilter filter) {
        productsState.postValue(Resource.loading());

        // ✅ gọi đúng repository (thay vì repo.*)
        productRepository.search(name, filter, new Callback<List<ProductDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductDto>> call, @NonNull Response<List<ProductDto>> res) {
                if (res.isSuccessful()) {
                    List<ProductDto> products = res.body();
                    

                    if (products == null || products.isEmpty()) {
                        productsState.postValue(Resource.error("Không có sản phẩm"));
                    } else {
                        productsState.postValue(Resource.success(products));
                    }
                } else {
                    String errorMessage = getErrorMessageForStatusCode(res.code());
                    productsState.postValue(Resource.error(errorMessage));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductDto>> call, @NonNull Throwable t) {
                String errorMessage = (t.getMessage() != null) ? t.getMessage() : "Lỗi kết nối mạng";
                productsState.postValue(Resource.error(errorMessage));
            }
        });
    }
    private String getErrorMessageForStatusCode(int statusCode) {
        switch (statusCode) {
            case 404:
                return "Không có sản phẩm";
            case 400:
                return "Yêu cầu không hợp lệ";
            case 401:
                return "Không có quyền truy cập";
            case 403:
                return "Truy cập bị từ chối";
            case 500:
                return "Lỗi máy chủ";
            case 503:
                return "Dịch vụ tạm thời không khả dụng";
            default:
                return "Lỗi tải dữ liệu (HTTP " + statusCode + ")";
        }
    }
    public void searchByName(String name) {
        search(name, null);
    }
}
