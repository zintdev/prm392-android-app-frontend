package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.login.LoginRequest;
import com.example.prm392_android_app_frontend.data.dto.login.LoginResponse;
import com.example.prm392_android_app_frontend.data.dto.register.RegisterRequest;
import com.example.prm392_android_app_frontend.data.dto.register.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthApi {

    @Headers({
            "Content-Type: application/json"
    })
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @Headers({
            "Content-Type: application/json"
    })
    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest body);
}
