// data/remote/api/CategoryApi.java
package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.CategoryDto;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface CategoryApi {

    @GET("categories")
    Call<List<CategoryDto>> getCategories();
}
