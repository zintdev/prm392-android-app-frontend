package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.AddToCartRequestDto;
import com.example.prm392_android_app_frontend.data.dto.CartDto;
import com.example.prm392_android_app_frontend.data.dto.UpdateCartItemRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CartApi {
    @GET("cart")
    Call<CartDto> getCart();

    @POST("cart/items")
    Call<CartDto> addToCart(@Body AddToCartRequestDto requestBody);

    @DELETE("cart/items/{itemId}")
    Call<CartDto> removeItemFromCart(@Path("itemId") int itemId);

    @PATCH("cart/items/{itemId}")
    Call<CartDto> updateItemQuantity(@Path("itemId") int itemId, @Body UpdateCartItemRequest body);

    @DELETE("cart")
    Call<CartDto> deleteCart();
}
