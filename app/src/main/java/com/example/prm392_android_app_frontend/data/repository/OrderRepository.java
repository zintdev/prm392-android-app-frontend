package com.example.prm392_android_app_frontend.data.repository;

import com.example.prm392_android_app_frontend.data.dto.CreateOrderRequestDto;
import com.example.prm392_android_app_frontend.data.dto.OrderDTO;
import com.example.prm392_android_app_frontend.data.remote.api.OrderApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Repository để xử lý các hoạt động liên quan đến đơn hàng.
 * Đóng gói việc gọi API Order từ ViewModel.
 */
public class OrderRepository {

    private final OrderApi orderApi;

    public OrderRepository(OrderApi orderApi) {
        this.orderApi = orderApi;
    }

    /**
     * Tạo đơn hàng mới
     *
     * @param request  Thông tin đơn hàng (userId, shipmentMethod, địa chỉ giao hàng, etc.)
     * @param callback Callback xử lý kết quả
     */
    public void placeOrder(CreateOrderRequestDto request, Callback<OrderDTO> callback) {
        Call<OrderDTO> call = orderApi.placeOrder(request);
        call.enqueue(callback);
    }

    /**
     * Lấy danh sách đơn hàng của user
     *
     * @param userId   ID của user
     * @param status   Trạng thái đơn hàng (null để lấy tất cả)
     * @param callback Callback xử lý kết quả
     */
    public void getOrdersByUserId(int userId, String status, Callback<List<OrderDTO>> callback) {
        Call<List<OrderDTO>> call = orderApi.getOrdersByUserId(userId, status);
        call.enqueue(callback);
    }
}
