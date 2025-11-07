
package com.example.prm392_android_app_frontend.data.repository;

import com.example.prm392_android_app_frontend.data.dto.CategoryDto;
import com.example.prm392_android_app_frontend.data.remote.api.CategoryApi;

import java.util.List;

import retrofit2.Callback;

public class CategoryRepository {
    private final CategoryApi api;

    public CategoryRepository(CategoryApi api) {
        this.api = api;
    }

    public void getAll(Callback<List<CategoryDto>> cb) {
        api.getCategories().enqueue(cb);
    }
}
