package com.example.prm392_android_app_frontend.data.remote.api;
import com.example.prm392_android_app_frontend.data.dto.AddToCartRequestDto;
import com.example.prm392_android_app_frontend.data.dto.CartDto;
import com.example.prm392_android_app_frontend.data.dto.UpdateCartItemRequest;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
//import com.example.prm392_android_app_frontend.data.dto.LoginRequest;
//import com.example.prm392_android_app_frontend.data.dto.LoginResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
<<<<<<<< HEAD:app/src/main/java/com/example/prm392_android_app_frontend/data/remote/api/ShopService.java
import retrofit2.http.DELETE;
import retrofit2.http.PATCH;
========


public interface ShopApi {
>>>>>>>> develop:app/src/main/java/com/example/prm392_android_app_frontend/data/remote/api/ShopApi.java



public interface ShopService {


    @GET("products")
    Call<List<ProductDto>> getAllProducts();
    @GET("products/{id}")
    Call<ProductDto> getProductById(@Path("id") int id);

    @GET("cart")
    Call<CartDto> getCart();

<<<<<<<< HEAD:app/src/main/java/com/example/prm392_android_app_frontend/data/remote/api/ShopService.java
========
    // Bỏ @Header đi
>>>>>>>> develop:app/src/main/java/com/example/prm392_android_app_frontend/data/remote/api/ShopApi.java
    @POST("cart/items")
    Call<CartDto> addToCart(@Body AddToCartRequestDto requestBody);

    @DELETE("cart/items/{productId}")
    Call<CartDto> removeItemFromCart(@Path("productId") int productId);

    @PATCH("cart/items/{productId}")
    Call<CartDto> updateItemQuantity(@Path("productId") int productId, @Body UpdateCartItemRequest body);
}
