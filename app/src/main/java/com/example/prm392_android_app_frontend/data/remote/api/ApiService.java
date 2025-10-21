package com.example.prm392_android_app_frontend.data.remote.api;
import com.example.prm392_android_app_frontend.data.dto.AddToCartRequestDto;
import com.example.prm392_android_app_frontend.data.dto.CartDto;

import com.example.prm392_android_app_frontend.data.dto.ProductDto;
//import com.example.prm392_android_app_frontend.data.dto.LoginRequest;
//import com.example.prm392_android_app_frontend.data.dto.LoginResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;


public interface ApiService {



//    @POST("auth/login") // Giả sử endpoint đăng nhập là "auth/login"
//    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);


    @GET("products")
    Call<List<ProductDto>> getAllProducts();
    @GET("products/{id}")
    Call<ProductDto> getProductById(@Path("id") int id);

    @GET("api/cart")
    Call<CartDto> getCart();

    // Bỏ @Header đi
    @POST("api/cart/items")
    Call<CartDto> addToCart(@Body AddToCartRequestDto requestBody);

}
