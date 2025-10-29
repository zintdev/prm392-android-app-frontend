package com.example.prm392_android_app_frontend.data.repository;

import com.example.prm392_android_app_frontend.data.dto.CreateOrderRequestDto;
import com.example.prm392_android_app_frontend.data.dto.OrderDTO;
import com.example.prm392_android_app_frontend.data.remote.api.OrderApi;

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
     * @param request Thông tin đơn hàng (userId, shipmentMethod, địa chỉ giao hàng, etc.)
     * @param callback Callback xử lý kết quả
     */
    public void placeOrder(CreateOrderRequestDto request, Callback<OrderDTO> callback) {
        Call<OrderDTO> call = orderApi.placeOrder(request);
        call.enqueue(callback);
    }

    /**
     * Lấy thông tin đơn hàng theo ID
     * Chưa implement vì chưa có API endpoint
     */
    // public void getOrderById(int orderId, Callback<OrderDTO> callback) {
    //     Call<OrderDTO> call = orderApi.getOrderById(orderId);
    //     call.enqueue(callback);
    // }

    /**
     * Lấy danh sách đơn hàng của user
     * Chưa implement vì chưa có API endpoint
     */
    // public void getUserOrders(int userId, Callback<List<OrderDTO>> callback) {
    //     Call<List<OrderDTO>> call = orderApi.getUserOrders(userId);
    //     call.enqueue(callback);
    // }

    /**
     * Cập nhật trạng thái đơn hàng
     * Chưa implement vì chưa có API endpoint
     */
    // public void updateOrderStatus(int orderId, String status, Callback<OrderDTO> callback) {
    //     Call<OrderDTO> call = orderApi.updateOrderStatus(orderId, status);
    //     call.enqueue(callback);
    // }

    /**
     * Hủy đơn hàng
     * Chưa implement vì chưa có API endpoint
     */
    // public void cancelOrder(int orderId, Callback<OrderDTO> callback) {
    //     Call<OrderDTO> call = orderApi.cancelOrder(orderId);
    //     call.enqueue(callback);
    // }
}
