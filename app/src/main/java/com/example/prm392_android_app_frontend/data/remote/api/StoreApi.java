package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.chat.SpringPage;
import com.example.prm392_android_app_frontend.data.dto.store.StoreNearbyDto;
import com.example.prm392_android_app_frontend.data.dto.store.StoreLocationRequest;
import com.example.prm392_android_app_frontend.data.dto.store.StoreLocationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.POST;
import retrofit2.http.Body;

public interface StoreApi {

    @GET("/api/store-locations/nearby")
    Call<List<StoreNearbyDto>> getNearby(
            @Query("lat") double lat,
            @Query("lng") double lng,
            @Query("radiusKm") Double radiusKm,
            @Query("limit") Integer limit,
            @Query("productId") Integer productId,
            @Query("inStockOnly") Boolean inStockOnly
    );

    /**
     * Gọi API để tạo một cửa hàng mới.
     * Tương ứng với: @PostMapping("/api/store-locations") trong Controller
     *
     * @param req Đối tượng chứa (storeName, address, latitude, longitude)
     * @return Một Call object chứa StoreLocationResponse
     */
    @POST("store-locations")
    Call<StoreLocationResponse> create(@Body StoreLocationRequest req);

    @GET("store-locations")
    Call<SpringPage<StoreLocationResponse>> list(@Query("page") int page,
                                                 @Query("size") int size);
}