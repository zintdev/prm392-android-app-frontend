package com.example.prm392_android_app_frontend.data.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateCartItemRequest {
//kkkk//
    @SerializedName("quantity")
    private int quantity;

    public UpdateCartItemRequest(int quantity) {
        this.quantity = quantity;
    }

    // Bạn có thể thêm getter nếu cần, mặc dù không bắt buộc cho request body
    public int getQuantity() {
        return quantity;
    }
}
