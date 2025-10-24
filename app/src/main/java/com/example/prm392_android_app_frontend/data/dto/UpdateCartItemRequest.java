package com.example.prm392_android_app_frontend.data.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateCartItemRequest {

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("selected")
    private boolean selected;

    public UpdateCartItemRequest(int quantity, boolean selected) {
        this.quantity = quantity;
        this.selected = selected;
    }

    public UpdateCartItemRequest(int quantity) {
        this(quantity, false);
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isSelected() {
        return selected;
    }
}
