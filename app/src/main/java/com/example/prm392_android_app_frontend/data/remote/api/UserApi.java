package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.UpdateUserRequest.UpdateUserRequest;
import com.example.prm392_android_app_frontend.data.dto.UserDto;
import com.example.prm392_android_app_frontend.data.dto.changePassword.ChangePasswordRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApi {
    @GET("/api/users/{id}")
    Call<UserDto> getUserById(@Path("id") int id);

    @PUT("/api/users/{id}")
    Call<UserDto> updateUser(@Path("id") int id, @Body UpdateUserRequest body);

    @PUT("users/{id}")
    Call<Void> changePassword(@Path("id") int userId,
                              @Body ChangePasswordRequest body);
}
