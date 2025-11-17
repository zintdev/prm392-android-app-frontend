package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.UpdateUserRequest.UpdateUserRequest;
import com.example.prm392_android_app_frontend.data.dto.UserDto;
import com.example.prm392_android_app_frontend.data.dto.changePassword.ChangePasswordRequest;
import com.example.prm392_android_app_frontend.data.dto.user.CreateStaffRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.POST;

public interface UserApi {
    @GET("users/{id}")
    Call<UserDto> getUserById(@Path("id") int id);

    @PUT("users/{id}")
    Call<UserDto> updateUser(@Path("id") int id, @Body UpdateUserRequest body);

    @PUT("users/{id}")
    Call<Void> changePassword(@Path("id") int userId,
                              @Body ChangePasswordRequest body);

    @GET("users")
    Call<List<UserDto>> getAllUsers();

    @POST("users")
    Call<UserDto> createUser(@Body CreateStaffRequest request);
}
