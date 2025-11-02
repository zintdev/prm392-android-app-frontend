package com.example.prm392_android_app_frontend.data.repository;

import com.example.prm392_android_app_frontend.data.dto.PaymentDTO;
import com.example.prm392_android_app_frontend.data.dto.PaymentResponseDTO;
import com.example.prm392_android_app_frontend.data.dto.VNPayRequestDTO;
import com.example.prm392_android_app_frontend.data.dto.VNPayResponseDTO;
import com.example.prm392_android_app_frontend.data.remote.api.PaymentApi;
import com.example.prm392_android_app_frontend.data.remote.api.VNPayApi;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Repository để xử lý các hoạt động liên quan đến thanh toán.
 * Đóng gói việc gọi API Payment và VNPay từ ViewModel.
 */
public class PaymentRepository {

    private final PaymentApi paymentApi;
    private final VNPayApi vnPayApi;

    public PaymentRepository(PaymentApi paymentApi, VNPayApi vnPayApi) {
        this.paymentApi = paymentApi;
        this.vnPayApi = vnPayApi;
    }

    /**
     * Tạo payment record cho order
     * @param orderId ID của đơn hàng
     * @param method Phương thức thanh toán (COD, VNPAY, etc.)
     * @param amount Số tiền thanh toán
     * @param callback Callback xử lý kết quả
     */
    public void createPayment(int orderId, String method, double amount, Callback<PaymentResponseDTO> callback) {
        PaymentDTO paymentDTO = new PaymentDTO(orderId, method, amount);
        Call<PaymentResponseDTO> call = paymentApi.createPayment(paymentDTO);
        call.enqueue(callback);
    }

    /**
     * Tạo URL thanh toán VNPay
     * @param paymentId ID của payment record
     * @param amount Số tiền thanh toán
     * @param orderDescription Mô tả đơn hàng
     * @param callback Callback xử lý kết quả
     */
    public void createVNPayUrl(int paymentId, double amount, String orderDescription, Callback<VNPayResponseDTO> callback) {
        VNPayRequestDTO vnPayRequest = new VNPayRequestDTO(paymentId, amount, orderDescription);
        Call<VNPayResponseDTO> call = vnPayApi.createPaymentUrl(vnPayRequest);
        call.enqueue(callback);
    }
}
