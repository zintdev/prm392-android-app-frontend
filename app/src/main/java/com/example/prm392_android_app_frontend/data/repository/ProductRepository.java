package com.example.prm392_android_app_frontend.data.repository;

import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.data.remote.api.ApiService;
import java.util.List;
import retrofit2.Callback;

public class ProductRepository {
    private final ApiService apiService;

    // Constructor để nhận dependency là ApiService
    public ProductRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    // Phương thức nhận callback để xử lý bất đồng bộ
    public void getAllProducts(Callback<List<ProductDto>> callback) {
        apiService.getAllProducts().enqueue(callback);
    }

    // Phương thức nhận callback để xử lý bất đồng bộ
    public void getProductById(int productId, Callback<ProductDto> callback) {
        apiService.getProductById(productId).enqueue(callback);
    }
}
