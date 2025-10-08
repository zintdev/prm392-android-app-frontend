package com.example.prm392_android_app_frontend.net;

import com.example.prm392_android_app_frontend.features.auth.data.dto.LoginRequest;
import com.example.prm392_android_app_frontend.features.auth.data.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthApi {

    @Headers("Content-Type: application/json")
    @POST("api/auth/login") // chỉnh theo backend
    Call<LoginResponse> login(@Body LoginRequest body);
}
