package com.example.prm392_android_app_frontend.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.data.repository.ProductRepository;
import java.util.List;

public class ProductViewModel extends ViewModel {
    private ProductRepository productRepository;
    private LiveData<List<ProductDto>> productList;

    public ProductViewModel() {
        productRepository = new ProductRepository();
        productList = productRepository.getAllProducts();
    }

    // Phương thức này giữ nguyên để dùng cho trang Home
    public LiveData<List<ProductDto>> getProductList() {
        return productList;
    }

    public LiveData<ProductDto> getProductDetails(int productId) {
        // Gọi phương thức getProductById từ repository mà bạn đã tạo
        return productRepository.getProductById(productId);
    }
}
