
package com.example.prm392_android_app_frontend.data.repository;

import com.example.prm392_android_app_frontend.data.dto.CategoryDto;
import com.example.prm392_android_app_frontend.data.remote.api.CategoryApi;

import java.util.List;

import retrofit2.Callback;
import retrofit2.Call;

public class CategoryRepository {
    private final CategoryApi api;

    public CategoryRepository(CategoryApi api) {
        this.api = api;
    }

    public void getAll(Callback<List<CategoryDto>> cb) {
        api.getCategories().enqueue(cb);
    }

    public void create(CategoryDto dto, Callback<CategoryDto> cb) {
        Call<CategoryDto> call = api.createCategory(dto);
        call.enqueue(cb);
    }

    public void update(int id, CategoryDto dto, Callback<CategoryDto> cb) {
        Call<CategoryDto> call = api.updateCategory(id, dto);
        call.enqueue(cb);
    }

    public void delete(Integer id, Callback<Void> callback) {
        api.deleteCategory(id).enqueue(callback);
    }
}
