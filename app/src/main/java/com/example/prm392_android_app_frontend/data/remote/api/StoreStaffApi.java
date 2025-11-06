package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.UserDto;
import com.example.prm392_android_app_frontend.data.dto.store.AssignStaffRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface StoreStaffApi {

    @GET("store-locations/{storeId}/staff")
    Call<List<UserDto>> listStaff(@Path("storeId") int storeId);

    @POST("store-locations/{storeId}/staff")
    Call<UserDto> assignStaff(@Path("storeId") int storeId,
                              @Body AssignStaffRequest request);
}
