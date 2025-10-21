package com.example.prm392_android_app_frontend.data.dto;

public class AddToCartRequestDto {
    private int productId;
    private int quantity;

    public AddToCartRequestDto(int productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters and Setters
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
    