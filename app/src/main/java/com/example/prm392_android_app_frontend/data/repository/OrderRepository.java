package com.example.prm392_android_app_frontend.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.core.util.Resource;
import com.example.prm392_android_app_frontend.data.dto.CreateOrderRequestDto;
import com.example.prm392_android_app_frontend.data.dto.OrderDTO;
import com.example.prm392_android_app_frontend.data.dto.order.OrderDto;
import com.example.prm392_android_app_frontend.data.dto.order.UpdateOrderStatusRequest;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.OrderApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository để xử lý các hoạt động liên quan đến đơn hàng.
 * Đóng gói việc gọi API Order từ ViewModel và cung cấp LiveData cho UI.
 */
public class OrderRepository {

    private static final String TAG = "OrderRepository";

    private final OrderApi orderApi;

    public OrderRepository(OrderApi orderApi) {
        this.orderApi = orderApi;
    }

    public OrderRepository(Context context) {
        this(ApiClient.getAuthClient(context).create(OrderApi.class));
    }

    /**
     * Tạo đơn hàng mới.
     */
    public void placeOrder(CreateOrderRequestDto request, Callback<OrderDTO> callback) {
        orderApi.placeOrder(request).enqueue(callback);
    }

    /**
     * Lấy danh sách đơn hàng theo userId và trạng thái (callback kiểu cũ).
     */
    public void getOrdersByUserId(int userId, String status, Callback<List<OrderDTO>> callback) {
        orderApi.getOrdersByUserId(userId, status).enqueue(callback);
    }

    public LiveData<Resource<List<OrderDto>>> getAllOrders(String orderStatus) {
        MutableLiveData<Resource<List<OrderDto>>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading(null));
        orderApi.getAllOrders(orderStatus).enqueue(new Callback<List<OrderDto>>() {
            @Override
            public void onResponse(Call<List<OrderDto>> call, Response<List<OrderDto>> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(Resource.success(response.body()));
                } else {
                    logHttpError("getAllOrders", response.code(), response.message());
                    liveData.setValue(Resource.error("Failed to get orders: " + response.message(), null));
                }
            }

            @Override
            public void onFailure(Call<List<OrderDto>> call, Throwable t) {
                logFailure("getAllOrders", t);
                liveData.setValue(Resource.error(safeMessage(t), null));
            }
        });
        return liveData;
    }

    public LiveData<Resource<OrderDto>> getOrderById(int orderId) {
        MutableLiveData<Resource<OrderDto>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading(null));
        orderApi.getOrderById(orderId).enqueue(new Callback<OrderDto>() {
            @Override
            public void onResponse(Call<OrderDto> call, Response<OrderDto> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(Resource.success(response.body()));
                } else {
                    logHttpError("getOrderById", response.code(), response.message());
                    liveData.setValue(Resource.error("Failed to get order details: " + response.message(), null));
                }
            }

            @Override
            public void onFailure(Call<OrderDto> call, Throwable t) {
                logFailure("getOrderById", t);
                liveData.setValue(Resource.error(safeMessage(t), null));
            }
        });
        return liveData;
    }

    public LiveData<Resource<List<OrderDto>>> getOrdersByUserId(int userId) {
        MutableLiveData<Resource<List<OrderDto>>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading(null));
        orderApi.getOrdersByUserId(userId).enqueue(new Callback<List<OrderDto>>() {
            @Override
            public void onResponse(Call<List<OrderDto>> call, Response<List<OrderDto>> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(Resource.success(response.body()));
                } else {
                    logHttpError("getOrdersByUserId", response.code(), response.message());
                    liveData.setValue(Resource.error("Failed to get user orders: " + response.message(), null));
                }
            }

            @Override
            public void onFailure(Call<List<OrderDto>> call, Throwable t) {
                logFailure("getOrdersByUserId", t);
                liveData.setValue(Resource.error(safeMessage(t), null));
            }
        });
        return liveData;
    }

    public LiveData<Resource<OrderDto>> updateOrderStatus(int orderId, String newStatus) {
        return updateOrderStatus(orderId, newStatus, null);
    }

    public LiveData<Resource<OrderDto>> updateOrderStatus(int orderId, String newStatus, Integer actorUserId) {
        MutableLiveData<Resource<OrderDto>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading(null));
        UpdateOrderStatusRequest request = actorUserId != null
                ? new UpdateOrderStatusRequest(newStatus, actorUserId)
                : new UpdateOrderStatusRequest(newStatus);
        orderApi.updateOrderStatus(orderId, request).enqueue(new Callback<OrderDto>() {
            @Override
            public void onResponse(Call<OrderDto> call, Response<OrderDto> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(Resource.success(response.body()));
                } else {
                    logHttpError("updateOrderStatus", response.code(), response.message());
                    liveData.setValue(Resource.error("Failed to update order: " + response.message(), null));
                }
            }

            @Override
            public void onFailure(Call<OrderDto> call, Throwable t) {
                logFailure("updateOrderStatus", t);
                liveData.setValue(Resource.error(safeMessage(t), null));
            }
        });
        return liveData;
    }

    private static void logHttpError(String action, int code, String message) {
        Log.e(TAG, action + ": API call failed with code " + code + ", message=" + message);
    }

    private static void logFailure(String action, Throwable t) {
        Log.e(TAG, action + ": API call failure", t);
    }

    private static String safeMessage(Throwable t) {
        return t != null && t.getMessage() != null ? t.getMessage() : "Unexpected error";
    }

    /**
     * Lấy tất cả đơn hàng (dành cho admin)
     */
    public void getAllOrders(Callback<List<OrderDTO>> callback) {
        Call<List<OrderDTO>> call = orderApi.getAllOrders();
        call.enqueue(callback);
    }
}
