package com.example.prm392_android_app_frontend.data.remote.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StoreApi {
    class StoreNearbyDto {
        public int storeId;
        public String name;
        public String address;
        public double latitude;
        public double longitude;
        public double distanceKm;
        public Integer quantity; // có thể null
    }

    @GET("/api/store-locations/nearby")
    Call<List<StoreNearbyDto>> getNearby(
            @Query("lat") double lat,
            @Query("lng") double lng,
            @Query("radiusKm") Double radiusKm,
            @Query("limit") Integer limit,
            @Query("productId") Integer productId,
            @Query("inStockOnly") Boolean inStockOnly
    );
}