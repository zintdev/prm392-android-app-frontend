package com.example.prm392_android_app_frontend.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.prm392_android_app_frontend.data.dto.UserDto;
import com.example.prm392_android_app_frontend.data.dto.chat.SpringPage;
import com.example.prm392_android_app_frontend.data.dto.store.AssignStaffRequest;
import com.example.prm392_android_app_frontend.data.dto.store.StoreLocationResponse;
import com.example.prm392_android_app_frontend.data.dto.user.CreateStaffRequest;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.StoreApi;
import com.example.prm392_android_app_frontend.data.remote.api.StoreStaffApi;
import com.example.prm392_android_app_frontend.data.remote.api.UserApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffRepository {

    public interface Result<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public interface ResultWithCode<T> {
        void onSuccess(T data);
        void onError(String message, int code);
    }

    private final UserApi userApi;
    private final StoreApi storeApi;
    private final StoreStaffApi storeStaffApi;

    public StaffRepository(Context context) {
        Context appCtx = context.getApplicationContext();
        userApi = ApiClient.getAuthClient(appCtx).create(UserApi.class);
        storeApi = ApiClient.getAuthClient(appCtx).create(StoreApi.class);
        storeStaffApi = ApiClient.getAuthClient(appCtx).create(StoreStaffApi.class);
    }

    public void getStaffList(Result<List<UserDto>> callback) {
        userApi.getAllUsers().enqueue(new Callback<List<UserDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserDto>> call,
                                   @NonNull Response<List<UserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserDto> filtered = new ArrayList<>();
                    for (UserDto dto : response.body()) {
                        if (dto != null && dto.role != null && dto.role.toUpperCase().contains("STAFF")) {
                            filtered.add(dto);
                        }
                    }
                    callback.onSuccess(filtered);
                } else {
                    callback.onError("Không thể tải danh sách nhân viên");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserDto>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createStaff(CreateStaffRequest request, ResultWithCode<UserDto> callback) {
        userApi.createUser(request).enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(@NonNull Call<UserDto> call,
                                   @NonNull Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Tạo nhân viên thất bại", response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserDto> call, @NonNull Throwable t) {
                callback.onError(t.getMessage(), -1);
            }
        });
    }

    public void listStores(Result<List<StoreLocationResponse>> callback) {
        storeApi.list(0, 200).enqueue(new Callback<SpringPage<StoreLocationResponse>>() {
            @Override
            public void onResponse(@NonNull Call<SpringPage<StoreLocationResponse>> call,
                                   @NonNull Response<SpringPage<StoreLocationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<StoreLocationResponse> content = response.body().getContent();
                    callback.onSuccess(content != null ? content : Collections.emptyList());
                } else {
                    callback.onError("Không thể tải danh sách cửa hàng");
                }
            }

            @Override
            public void onFailure(@NonNull Call<SpringPage<StoreLocationResponse>> call,
                                  @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void assignStaffToStore(int storeId, int staffId, int actorId,
                                   ResultWithCode<UserDto> callback) {
        AssignStaffRequest request = new AssignStaffRequest(staffId, actorId);
        storeStaffApi.assignStaff(storeId, request).enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(@NonNull Call<UserDto> call,
                                   @NonNull Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể gán nhân viên", response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserDto> call, @NonNull Throwable t) {
                callback.onError(t.getMessage(), -1);
            }
        });
    }

    public void listStaffOfStore(int storeId, Result<List<UserDto>> callback) {
        storeStaffApi.listStaff(storeId).enqueue(new Callback<List<UserDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserDto>> call,
                                   @NonNull Response<List<UserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải nhân viên của cửa hàng");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserDto>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
