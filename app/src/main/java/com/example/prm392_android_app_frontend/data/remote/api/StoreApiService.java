package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.store.StoreLocationResponse;
import com.example.prm392_android_app_frontend.data.dto.store.StoreLocationRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Interface Retrofit cho các API liên quan đến Store (Backend của bạn).
 * Base URL (ví dụ: "http://10.0.2.2:8080/") sẽ được cung cấp khi bạn tạo Retrofit client.
 */
public interface StoreApiService {

    /**
     * Gọi API để tạo một cửa hàng mới.
     * Tương ứng với: @PostMapping("/api/store-locations") trong Controller
     *
     * @param req Đối tượng chứa (storeName, address, latitude, longitude)
     * @return Một Call object chứa StoreLocationResponse
     */
    @POST("store-locations")
    Call<StoreLocationResponse> create(@Body StoreLocationRequest req);

    // Bạn có thể thêm các API khác từ Controller của mình vào đây, ví dụ:

    // @GET("api/store-locations")
    // Call<List<StoreLocationResponse>> list(); // (Lưu ý: Backend của bạn trả về Page<>, sẽ phức tạp hơn)

    // @GET("api/store-locations/{id}")
    // Call<StoreLocationResponse> get(@Path("id") Integer id);
}
