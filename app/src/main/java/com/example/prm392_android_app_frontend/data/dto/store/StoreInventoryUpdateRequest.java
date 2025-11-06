package com.example.prm392_android_app_frontend.data.dto.store;

import com.google.gson.annotations.SerializedName;

public class StoreInventoryUpdateRequest {

    @SerializedName("productId")
    private final Integer productId;

    @SerializedName("quantity")
    private final Integer quantity;

    @SerializedName("actorUserId")
    private final Integer actorUserId;

    public StoreInventoryUpdateRequest(Integer productId, Integer quantity, Integer actorUserId) {
        this.productId = productId;
        this.quantity = quantity;
        this.actorUserId = actorUserId;
    }

    public Integer getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getActorUserId() {
        return actorUserId;
    }
}
