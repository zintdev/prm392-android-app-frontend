package com.example.prm392_android_app_frontend.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.core.util.Resource;
import com.example.prm392_android_app_frontend.data.dto.order.OrderDto;
import com.example.prm392_android_app_frontend.data.dto.order.UpdateOrderStatusRequest;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.OrderApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderRepository {

    private static final String TAG = "OrderRepository_DEBUG";
    private final OrderApi orderApi;

    public OrderRepository(Context context) {
        Log.d(TAG, "Creating OrderApi with auth client.");
        this.orderApi = ApiClient.getAuthClient(context).create(OrderApi.class);
        Log.d(TAG, "OrderApi created.");
    }

    public LiveData<Resource<List<OrderDto>>> getAllOrders() {
        Log.d(TAG, "getAllOrders: Making API call.");
        MutableLiveData<Resource<List<OrderDto>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        orderApi.getAllOrders(null).enqueue(new Callback<List<OrderDto>>() {
            @Override
            public void onResponse(Call<List<OrderDto>> call, Response<List<OrderDto>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "getAllOrders: API call successful.");
                    data.setValue(Resource.success(response.body()));
                } else {
                    Log.e(TAG, "getAllOrders: API call failed with code " + response.code());
                    data.setValue(Resource.error("Failed to get orders: " + response.message(), null));
                }
            }

            @Override
            public void onFailure(Call<List<OrderDto>> call, Throwable t) {
                Log.e(TAG, "getAllOrders: API call failed on failure", t);
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    public LiveData<Resource<OrderDto>> updateOrderStatus(int orderId, String newStatus) {
        Log.d(TAG, "updateOrderStatus: Making API call for orderId " + orderId);
        MutableLiveData<Resource<OrderDto>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(newStatus);
        orderApi.updateOrderStatus(orderId, request).enqueue(new Callback<OrderDto>() {
            @Override
            public void onResponse(Call<OrderDto> call, Response<OrderDto> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "updateOrderStatus: API call successful.");
                    data.setValue(Resource.success(response.body()));
                } else {
                    Log.e(TAG, "updateOrderStatus: API call failed with code " + response.code());
                    data.setValue(Resource.error("Failed to update order: " + response.message(), null));
                }
            }

            @Override
            public void onFailure(Call<OrderDto> call, Throwable t) {
                Log.e(TAG, "updateOrderStatus: API call failed on failure", t);
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }
}
