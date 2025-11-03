package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.store.NominatimPlace;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NominatimApiService {

    // Base URL để dùng cho Retrofit client:
    // https://nominatim.openstreetmap.org/

    @GET("search")
    Call<List<NominatimPlace>> search(
            @Query("q") String query,      // Địa chỉ người dùng nhập
            @Query("format") String format, // Luôn là "json"
            @Query("limit") int limit      // Giới hạn 1 kết quả
    );

    /**
     * Autocomplete search với nhiều kết quả cho dropdown suggestions
     */
    @GET("search")
    Call<List<NominatimPlace>> autocomplete(
            @Query("q") String query,
            @Query("format") String format,
            @Query("limit") int limit      // Thường là 5-10 kết quả
    );

    /**
     * Reverse geocoding: chuyển từ lat/lon sang địa chỉ
     */
    @GET("reverse")
    Call<NominatimPlace> reverseGeocode(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("format") String format  // "json"
    );
}