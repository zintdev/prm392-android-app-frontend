package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.order.OrderDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OrderApi {

    @GET("orders")
    Call<List<OrderDto>> getAllOrders(@Query("orderStatus") String orderStatus);
}
