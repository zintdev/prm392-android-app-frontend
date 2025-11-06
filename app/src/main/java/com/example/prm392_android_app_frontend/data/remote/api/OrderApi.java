package com.example.prm392_android_app_frontend.data.remote.api;


import com.example.prm392_android_app_frontend.data.dto.order.OrderDto;
import com.example.prm392_android_app_frontend.data.dto.order.UpdateOrderStatusRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OrderApi {

    @POST("orders")
    Call<OrderDTO> placeOrder(@Body CreateOrderRequestDto body);
    
    @GET("orders/user/{userId}")
    Call<List<OrderDTO>> getOrdersByUserId(
            @Path("userId") int userId,
            @Query("status") String status
    );
    @GET("orders")
    Call<List<OrderDto>> getAllOrders(@Query("orderStatus") String orderStatus);

    @GET("orders/{orderId}")
    Call<OrderDto> getOrderById(@Path("orderId") int orderId);

    @PUT("orders/{orderId}")
    Call<OrderDto> updateOrderStatus(@Path("orderId") int orderId, @Body UpdateOrderStatusRequest request);

    @GET("orders/user/{userId}")
    Call<List<OrderDto>> getOrdersByUserId(@Path("userId") int userId);
}
