package com.example.prm392_android_app_frontend.data.repository;

import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.data.remote.api.ShopService;
import java.util.List;
import retrofit2.Callback;

public class ProductRepository {
    private final ShopService shopService;

    // Constructor để nhận dependency là ApiService
    public ProductRepository(ShopService shopService) {
        this.shopService = shopService;
    }

    // Phương thức nhận callback để xử lý bất đồng bộ
    public void getAllProducts(Callback<List<ProductDto>> callback) {
        shopService.getAllProducts().enqueue(callback);
    }

    // Phương thức nhận callback để xử lý bất đồng bộ
    public void getProductById(int productId, Callback<ProductDto> callback) {
        shopService.getProductById(productId).enqueue(callback);
    }
}
