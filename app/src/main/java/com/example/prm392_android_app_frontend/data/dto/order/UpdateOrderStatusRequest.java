package com.example.prm392_android_app_frontend.data.dto.order;

import com.google.gson.annotations.SerializedName;

public class UpdateOrderStatusRequest {

    @SerializedName("orderStatus")
    private final String orderStatus;

    @SerializedName("actorUserId")
    private final Integer actorUserId;

    public UpdateOrderStatusRequest(String orderStatus) {
        this(orderStatus, null);
    }

    public UpdateOrderStatusRequest(String orderStatus, Integer actorUserId) {
        this.orderStatus = orderStatus;
        this.actorUserId = actorUserId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public Integer getActorUserId() {
        return actorUserId;
    }
}
