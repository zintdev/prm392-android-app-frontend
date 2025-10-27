package com.example.prm392_android_app_frontend.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.prm392_android_app_frontend.data.dto.UpdateUserRequest.UpdateUserRequest;
import com.example.prm392_android_app_frontend.data.dto.UserDto;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.UserApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private final UserApi api;

    public interface CallbackResult<T> {
        void onSuccess(T data);
        void onError(String message, int code);
    }

    public UserRepository(Context ctx) {
        this.api = ApiClient.getAuthClient(ctx).create(UserApi.class);
    }

    public void getUserById(int userId, CallbackResult<UserDto> cb) {
        api.getUserById(userId).enqueue(new Callback<UserDto>() {
            @Override public void onResponse(@NonNull Call<UserDto> call, @NonNull Response<UserDto> res) {
                if (res.isSuccessful() && res.body()!=null) cb.onSuccess(res.body());
                else cb.onError("Không thể tải thông tin", res.code());
            }
            @Override public void onFailure(@NonNull Call<UserDto> call, @NonNull Throwable t) {
                cb.onError(t.getMessage(), -1);
            }
        });
    }

    public void updateUser(int userId, UpdateUserRequest req, CallbackResult<UserDto> cb) {
        api.updateUser(userId, req).enqueue(new Callback<UserDto>() {
            @Override public void onResponse(@NonNull Call<UserDto> call, @NonNull Response<UserDto> res) {
                if (res.isSuccessful() && res.body()!=null) cb.onSuccess(res.body());
                else cb.onError("Cập nhật thất bại", res.code());
            }
            @Override public void onFailure(@NonNull Call<UserDto> call, @NonNull Throwable t) {
                cb.onError(t.getMessage(), -1);
            }
        });
    }
}
