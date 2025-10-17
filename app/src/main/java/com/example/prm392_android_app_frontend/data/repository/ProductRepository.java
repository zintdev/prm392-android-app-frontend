package com.example.prm392_android_app_frontend.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {
    private ApiService apiService;

    public ProductRepository() {
        // Lấy ApiService từ ApiClient, không cần xác thực
        this.apiService = ApiClient.get().create(ApiService.class);
    }

    public LiveData<List<ProductDto>> getAllProducts() {
        MutableLiveData<List<ProductDto>> data = new MutableLiveData<>();
        apiService.getAllProducts().enqueue(new Callback<List<ProductDto>>() {
            @Override
            public void onResponse(Call<List<ProductDto>> call, Response<List<ProductDto>> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<ProductDto>> call, Throwable t) {
                // Có thể xử lý lỗi ở đây, ví dụ: data.setValue(null);
            }
        });
        return data;
    }
    public LiveData<ProductDto> getProductById(int id) {
        MutableLiveData<ProductDto> data = new MutableLiveData<>();
        apiService.getProductById(id).enqueue(new Callback<ProductDto>() {
            @Override
            public void onResponse(Call<ProductDto> call, Response<ProductDto> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                }
            }
            @Override
            public void onFailure(Call<ProductDto> call, Throwable t) {
                // Có thể xử lý lỗi ở đây, ví dụ: data.setValue(null);
            }
        });
        return data;
    }
}
    