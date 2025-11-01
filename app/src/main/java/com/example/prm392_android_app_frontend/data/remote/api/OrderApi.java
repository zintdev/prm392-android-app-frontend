package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.CreateOrderRequestDto;
import com.example.prm392_android_app_frontend.data.dto.OrderDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
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
}
