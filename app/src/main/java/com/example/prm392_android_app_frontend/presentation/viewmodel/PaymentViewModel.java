package com.example.prm392_android_app_frontend.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.prm392_android_app_frontend.data.dto.PaymentDTO;
import com.example.prm392_android_app_frontend.data.dto.PaymentResponseDTO;
import com.example.prm392_android_app_frontend.data.dto.VNPayRequestDTO;
import com.example.prm392_android_app_frontend.data.dto.VNPayResponseDTO;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.PaymentApi;
import com.example.prm392_android_app_frontend.data.remote.api.VNPayApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentViewModel extends ViewModel {

    private final MutableLiveData<PaymentResponseDTO> paymentLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> vnpayUrlLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private final PaymentApi paymentApi;
    private final VNPayApi vnPayApi;

    public PaymentViewModel() {
        paymentApi = ApiClient.get().create(PaymentApi.class);
        vnPayApi = ApiClient.get().create(VNPayApi.class);
    }

    public LiveData<PaymentResponseDTO> getPaymentLiveData() {
        return paymentLiveData;
    }

    public LiveData<String> getVnpayUrlLiveData() {
        return vnpayUrlLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void createPayment(int orderId, String method, double amount) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        PaymentDTO paymentDTO = new PaymentDTO(orderId, method, amount);
        Call<PaymentResponseDTO> call = paymentApi.createPayment(paymentDTO);
        
        call.enqueue(new Callback<PaymentResponseDTO>() {
            @Override
            public void onResponse(Call<PaymentResponseDTO> call, Response<PaymentResponseDTO> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    paymentLiveData.setValue(response.body());
                } else {
                    errorMessage.setValue("Không thể tạo thanh toán. Vui lòng thử lại.");
                }
            }

            @Override
            public void onFailure(Call<PaymentResponseDTO> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void createVNPayUrl(int paymentId, double amount, String orderDescription) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        VNPayRequestDTO request = new VNPayRequestDTO(paymentId, amount, orderDescription);
        Call<VNPayResponseDTO> call = vnPayApi.createPaymentUrl(request);
        
        call.enqueue(new Callback<VNPayResponseDTO>() {
            @Override
            public void onResponse(Call<VNPayResponseDTO> call, Response<VNPayResponseDTO> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    vnpayUrlLiveData.setValue(response.body().getPaymentUrl());
                } else {
                    errorMessage.setValue("Không thể tạo URL thanh toán VNPay");
                }
            }

            @Override
            public void onFailure(Call<VNPayResponseDTO> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối VNPay: " + t.getMessage());
            }
        });
    }
}
