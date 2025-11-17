package com.example.prm392_android_app_frontend.data.repository;

import androidx.annotation.NonNull;

import com.example.prm392_android_app_frontend.data.dto.address.DistrictDto;
import com.example.prm392_android_app_frontend.data.dto.address.ProvinceDto;
import com.example.prm392_android_app_frontend.data.remote.api.ProvincesServices;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProvincesRepository {

    public interface SimpleCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public void getProvinces(SimpleCallback<List<ProvinceDto>> cb) {
        ProvincesServices.api().getProvinces().enqueue(new Callback<List<ProvinceDto>>() {
            @Override public void onResponse(@NonNull Call<List<ProvinceDto>> call, @NonNull Response<List<ProvinceDto>> res) {
                if (res.isSuccessful() && res.body()!=null) cb.onSuccess(res.body());
                else cb.onError("Không tải được danh sách tỉnh");
            }
            @Override public void onFailure(@NonNull Call<List<ProvinceDto>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void getDistrictsByProvince(int provinceCode, SimpleCallback<List<com.example.prm392_android_app_frontend.data.dto.address.DistrictDto>> cb) {
        ProvincesServices.api().getProvinceDetail(provinceCode).enqueue(new Callback<ProvinceDto>() {
            @Override public void onResponse(@NonNull Call<ProvinceDto> call, @NonNull Response<ProvinceDto> res) {
                if (res.isSuccessful() && res.body()!=null) cb.onSuccess(res.body().districts);
                else cb.onError("Không tải được quận/huyện");
            }
            @Override public void onFailure(@NonNull Call<ProvinceDto> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void getWardsByDistrict(int districtCode, SimpleCallback<List<com.example.prm392_android_app_frontend.data.dto.address.WardDto>> cb) {
        ProvincesServices.api().getDistrictDetail(districtCode).enqueue(new Callback<DistrictDto>() {
            @Override public void onResponse(@NonNull Call<DistrictDto> call, @NonNull Response<DistrictDto> res) {
                if (res.isSuccessful() && res.body()!=null) cb.onSuccess(res.body().wards);
                else cb.onError("Không tải được phường/xã");
            }
            @Override public void onFailure(@NonNull Call<DistrictDto> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }
}
