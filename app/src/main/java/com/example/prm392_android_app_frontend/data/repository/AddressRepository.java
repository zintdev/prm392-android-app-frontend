package com.example.prm392_android_app_frontend.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.prm392_android_app_frontend.data.dto.address.AddressDto;
import com.example.prm392_android_app_frontend.data.dto.address.CreateAddressRequest;
import com.example.prm392_android_app_frontend.data.dto.address.UpdateAddressRequest;
import com.example.prm392_android_app_frontend.data.remote.api.AddressApi;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressRepository {

    public interface CallbackResult<T> {
        void onSuccess(T data);
        void onError(String message, int code);
    }

    private final AddressApi api;

    public AddressRepository(Context ctx) {
        this.api = ApiClient.getAuthClient(ctx).create(AddressApi.class);
    }

    /** Tạo địa chỉ mới */
    public void createAddress(int userId,
                              String shippingAddressLine1,
                              String shippingAddressLine2,
                              String shippingCityState,
                              CallbackResult<AddressDto> cb) {

        CreateAddressRequest req = new CreateAddressRequest();
        req.userId = userId;
        req.shippingAddressLine1 = shippingAddressLine1;
        req.shippingAddressLine2 = shippingAddressLine2;
        req.shippingCityState = shippingCityState;

        api.createAddress(req).enqueue(new Callback<AddressDto>() {
            @Override public void onResponse(@NonNull Call<AddressDto> call, @NonNull Response<AddressDto> res) {
                if (res.isSuccessful() && res.body() != null) {
                    cb.onSuccess(res.body());
                } else {
                    cb.onError("Tạo địa chỉ thất bại", res.code());
                }
            }
            @Override public void onFailure(@NonNull Call<AddressDto> call, @NonNull Throwable t) {
                cb.onError(t.getMessage(), -1);
            }
        });
    }
    public void updateAddress(int id, int userId, String line1, String line2, String cityState,
                              CallbackResult<AddressDto> cb) {
        UpdateAddressRequest req = new UpdateAddressRequest();
        req.userId = userId;
        req.shippingAddressLine1 = line1;
        req.shippingAddressLine2 = line2;
        req.shippingCityState = cityState;

        api.updateAddress(id, req).enqueue(new Callback<AddressDto>() {
            @Override public void onResponse(@NonNull Call<AddressDto> call, @NonNull Response<AddressDto> res) {
                if (res.isSuccessful() && res.body()!=null) cb.onSuccess(res.body());
                else cb.onError("Cập nhật địa chỉ thất bại", res.code());
            }
            @Override public void onFailure(@NonNull Call<AddressDto> call, @NonNull Throwable t) {
                cb.onError(t.getMessage(), -1);
            }
        });
    }

    public void deleteAddress(int id, CallbackResult<Boolean> cb) {
        api.deleteAddress(id).enqueue(new Callback<Void>() {
            @Override public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> res) {
                if (res.isSuccessful()) cb.onSuccess(true);
                else cb.onError("Xoá địa chỉ thất bại", res.code());
            }
            @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                cb.onError(t.getMessage(), -1);
            }
        });
    }
}

