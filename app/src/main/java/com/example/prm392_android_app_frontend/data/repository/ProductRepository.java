package com.example.prm392_android_app_frontend.data.repository;

import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.data.remote.api.ShopApi;
import java.util.List;
import retrofit2.Callback;

public class ProductRepository {
    private final ShopApi shopApi;

    // Constructor để nhận dependency là ApiService
    public ProductRepository(ShopApi shopApi) {
        this.shopApi = shopApi;
    }

    // Phương thức nhận callback để xử lý bất đồng bộ
    public void getAllProducts(Callback<List<ProductDto>> callback) {
        shopApi.getAllProducts().enqueue(callback);
    }

    // Phương thức nhận callback để xử lý bất đồng bộ
    public void getProductById(int productId, Callback<ProductDto> callback) {
        shopApi.getProductById(productId).enqueue(callback);
    }
}
