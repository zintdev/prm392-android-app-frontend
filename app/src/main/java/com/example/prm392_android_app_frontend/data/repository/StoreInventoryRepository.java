package com.example.prm392_android_app_frontend.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.prm392_android_app_frontend.data.dto.chat.SpringPage;
import com.example.prm392_android_app_frontend.data.dto.store.StoreInventoryItemDto;
import com.example.prm392_android_app_frontend.data.dto.store.StoreInventoryUpdateRequest;
import com.example.prm392_android_app_frontend.data.dto.store.StoreLocationResponse;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.StoreApi;
import com.example.prm392_android_app_frontend.data.remote.api.StoreInventoryApi;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoreInventoryRepository {

    public interface Result<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public interface ResultWithCode<T> {
        void onSuccess(T data);
        void onError(String message, int code);
    }

    private final StoreInventoryApi inventoryApi;
    private final StoreApi storeApi;

    public StoreInventoryRepository(Context context) {
        Context appContext = context.getApplicationContext();
        inventoryApi = ApiClient.getAuthClient(appContext).create(StoreInventoryApi.class);
        storeApi = ApiClient.getAuthClient(appContext).create(StoreApi.class);
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

    public void listInventory(int storeId, Result<List<StoreInventoryItemDto>> callback) {
        inventoryApi.listInventory(storeId).enqueue(new Callback<List<StoreInventoryItemDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<StoreInventoryItemDto>> call,
                                   @NonNull Response<List<StoreInventoryItemDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải tồn kho");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<StoreInventoryItemDto>> call,
                                  @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateInventory(int storeId, int productId, int quantity, int actorUserId,
                                ResultWithCode<StoreInventoryItemDto> callback) {
        StoreInventoryUpdateRequest request = new StoreInventoryUpdateRequest(productId, quantity, actorUserId);
        inventoryApi.updateInventory(storeId, request).enqueue(new Callback<StoreInventoryItemDto>() {
            @Override
            public void onResponse(@NonNull Call<StoreInventoryItemDto> call,
                                   @NonNull Response<StoreInventoryItemDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể cập nhật tồn kho", response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<StoreInventoryItemDto> call,
                                  @NonNull Throwable t) {
                callback.onError(t.getMessage(), -1);
            }
        });
    }
}
