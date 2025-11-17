package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.ProductDto;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.QueryMap;

public interface ProductApi {
    @GET("products")
    Call<List<ProductDto>> getAllProducts();
    
    @GET("products/{id}")
    Call<ProductDto> getProductById(@Path("id") int id);

    @POST("products")
    Call<ProductDto> createProduct(@Body ProductDto product);

    @PUT("products/{id}")
    Call<ProductDto> updateProduct(@Path("id") int id, @Body ProductDto product);

    @DELETE("products/{id}")
    Call<Void> deleteProduct(@Path("id") int id);

    @GET("products")
    Call<List<ProductDto>> getProducts(@QueryMap Map<String, String> queries);

}
