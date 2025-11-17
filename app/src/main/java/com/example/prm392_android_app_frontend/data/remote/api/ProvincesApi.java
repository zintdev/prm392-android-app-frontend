package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.address.DistrictDto;
import com.example.prm392_android_app_frontend.data.dto.address.ProvinceDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ProvincesApi {

    @GET("api/?depth=1")
    Call<List<ProvinceDto>> getProvinces();

    @GET("api/p/{code}?depth=2")
    Call<ProvinceDto> getProvinceDetail(@Path("code") int provinceCode);

    @GET("api/d/{code}?depth=2")
    Call<DistrictDto> getDistrictDetail(@Path("code") int districtCode);
}
