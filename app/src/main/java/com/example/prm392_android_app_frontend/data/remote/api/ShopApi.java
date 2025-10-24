package com.example.prm392_android_app_frontend.data.remote.api;
import com.example.prm392_android_app_frontend.data.dto.AddToCartRequestDto;
import com.example.prm392_android_app_frontend.data.dto.CartDto;
import com.example.prm392_android_app_frontend.data.dto.UpdateCartItemRequest;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.DELETE;
import retrofit2.http.PATCH;



public interface ShopApi {

    @GET("products")
    Call<List<ProductDto>> getAllProducts();
    @GET("products/{id}")
    Call<ProductDto> getProductById(@Path("id") int id);

    @GET("cart")
    Call<CartDto> getCart();

    @POST("cart/items")
    Call<CartDto> addToCart(@Body AddToCartRequestDto requestBody);

    @DELETE("cart/items/{productId}")
    Call<CartDto> removeItemFromCart(@Path("productId") int productId);

    @PATCH("cart/items/{productId}")
    Call<CartDto> updateItemQuantity(@Path("productId") int productId, @Body UpdateCartItemRequest body);
}
