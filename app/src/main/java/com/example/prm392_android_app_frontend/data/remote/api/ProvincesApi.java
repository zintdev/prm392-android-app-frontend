package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.address.DistrictDto;
import com.example.prm392_android_app_frontend.data.dto.address.ProvinceDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ProvincesApi {

    // Danh sách Tỉnh/TP (nông)
    @GET("api/?depth=1")
    Call<List<ProvinceDto>> getProvinces();

    // Chi tiết Tỉnh + mảng Districts
    @GET("api/p/{code}?depth=2")
    Call<ProvinceDto> getProvinceDetail(@Path("code") int provinceCode);

    // Chi tiết District + mảng Wards
    @GET("api/d/{code}?depth=2")
    Call<DistrictDto> getDistrictDetail(@Path("code") int districtCode);
}
