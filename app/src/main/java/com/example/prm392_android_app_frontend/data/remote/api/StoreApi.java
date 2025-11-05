package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.store.StoreNearbyDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

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
}