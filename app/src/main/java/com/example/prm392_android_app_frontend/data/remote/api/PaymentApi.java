package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.PaymentDTO;
import com.example.prm392_android_app_frontend.data.dto.PaymentResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PaymentApi {
    @POST("/api/payments")
    Call<PaymentResponseDTO> createPayment(@Body PaymentDTO paymentDTO);
}
