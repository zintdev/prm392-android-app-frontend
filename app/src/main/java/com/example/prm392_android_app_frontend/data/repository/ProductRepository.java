package com.example.prm392_android_app_frontend.data.repository;

import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.data.remote.api.ProductApi;
import com.example.prm392_android_app_frontend.data.remote.api.ShopApi;
import java.util.List;
import retrofit2.Callback;

public class ProductRepository {
    private final ShopApi shopApi;
    private final ProductApi productApi;

    // Constructor để nhận dependency là ApiService
    public ProductRepository(ShopApi shopApi, ProductApi productApi) {
        this.shopApi = shopApi;
        this.productApi = productApi;
    }

    // Phương thức nhận callback để xử lý bất đồng bộ
    public void getAllProducts(Callback<List<ProductDto>> callback) {
        shopApi.getAllProducts().enqueue(callback);
    }

    // Phương thức nhận callback để xử lý bất đồng bộ
    public void getProductById(int productId, Callback<ProductDto> callback) {
        shopApi.getProductById(productId).enqueue(callback);
    }

    // Thêm sản phẩm mới
    public void createProduct(ProductDto product, Callback<ProductDto> callback) {
        productApi.createProduct(product).enqueue(callback);
    }

    // Cập nhật sản phẩm
    public void updateProduct(int productId, ProductDto product, Callback<ProductDto> callback) {
        productApi.updateProduct(productId, product).enqueue(callback);
    }

    // Xóa sản phẩm
    public void deleteProduct(int productId, Callback<Void> callback) {
        productApi.deleteProduct(productId).enqueue(callback);
    }
}
