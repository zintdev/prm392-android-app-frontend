package com.example.prm392_android_app_frontend.data.repository;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.data.dto.ProductFilter;
import com.example.prm392_android_app_frontend.data.remote.api.ProductApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Callback;

public class ProductRepository {
    private final ProductApi ProductApi;

    // Constructor để nhận dependency là ApiService
    public ProductRepository(ProductApi ProductApi) {
        this.ProductApi = ProductApi;
    }

    // Phương thức nhận callback để xử lý bất đồng bộ
    public void getAllProducts(Callback<List<ProductDto>> callback) {
        ProductApi.getAllProducts().enqueue(callback);
    }

    // Phương thức nhận callback để xử lý bất đồng bộ
    public void getProductById(int productId, Callback<ProductDto> callback) {
        ProductApi.getProductById(productId).enqueue(callback);
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
    //-------------------------------------------------------------------------------------------//
    public void search(String name, @Nullable ProductFilter filter, Callback<List<ProductDto>> cb) {

        Map<String, String> q = new HashMap<>();
        if (name != null && !name.trim().isEmpty()) q.put("name", name.trim());

        Log.d("SearchRepo", "🔍 Filter object: " + (filter != null ? filter.toString() : "null"));
        
        if (filter != null) {
            Log.d("SearchRepo", "🔍 categoryId value: " + filter.categoryId);
            Log.d("SearchRepo", "🔍 priceSort value: " + filter.priceSort);
            
            if (filter.categoryId != null)      q.put("categoryId",String.valueOf(filter.categoryId));
            if (filter.publisherId != null)     q.put("publisherId", String.valueOf(filter.publisherId));
            if (filter.artistId != null)        q.put("artistId", String.valueOf(filter.artistId));
            if (filter.priceSort != null)       q.put("priceSort", filter.priceSort);
            if (filter.releaseYearFrom != null) q.put("releaseYearFrom", String.valueOf(filter.releaseYearFrom));
            if (filter.releaseYearTo != null)   q.put("releaseYearTo", String.valueOf(filter.releaseYearTo));
            if (filter.priceMin != null)        q.put("priceMin", String.valueOf(filter.priceMin));
            if (filter.priceMax != null)        q.put("priceMax", String.valueOf(filter.priceMax));
        }
        ProductApi.getProducts(q).enqueue(cb);
        Log.d("SearchRepo", "🔍 Sending search params: " + q.toString());

    }

}
