package com.example.prm392_android_app_frontend.data.dto.order;

public class UpdateOrderStatusRequest {

    private final String orderStatus;

    public UpdateOrderStatusRequest(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }
}
