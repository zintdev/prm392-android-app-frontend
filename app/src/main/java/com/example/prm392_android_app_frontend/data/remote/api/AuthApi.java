package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.login.LoginRequest;
import com.example.prm392_android_app_frontend.data.dto.login.LoginResponse;

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
}
