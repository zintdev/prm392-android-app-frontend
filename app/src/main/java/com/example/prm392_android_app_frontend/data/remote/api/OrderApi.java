package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.order.OrderDto;
import com.example.prm392_android_app_frontend.data.dto.order.UpdateOrderStatusRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OrderApi {

    @GET("orders")
    Call<List<OrderDto>> getAllOrders(@Query("orderStatus") String orderStatus);

    @PUT("orders/{orderId}")
    Call<OrderDto> updateOrderStatus(@Path("orderId") int orderId, @Body UpdateOrderStatusRequest request);
}
