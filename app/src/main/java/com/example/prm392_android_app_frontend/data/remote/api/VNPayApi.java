package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.VNPayRequestDTO;
import com.example.prm392_android_app_frontend.data.dto.VNPayResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface VNPayApi {
    @POST("/api/vnpay/create-payment")
    Call<VNPayResponseDTO> createPaymentUrl(@Body VNPayRequestDTO request);
}
