// data/remote/api/CategoryApi.java
package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.CategoryDto;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CategoryApi {

    @GET("categories")
    Call<List<CategoryDto>> getCategories();

    @GET("categories/{id}")
    Call<CategoryDto> getCategoryById(@Path("id") int id);

    @PUT("categories/{id}")
    Call<CategoryDto> updateCategory(@Path("id") int id, @Body CategoryDto category);

    @POST("categories")
    Call<CategoryDto> createCategory(@Body CategoryDto category);

    @DELETE("categories/{id}")
    Call<Void> deleteCategory(@Path("id") int id);
}
