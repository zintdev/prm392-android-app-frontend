package com.example.prm392_android_app_frontend.data.dto.store;

/**
 * Represents a pending inventory change for a specific product within a store.
 */
public class StoreInventoryChange {
    private final int productId;
    private final int quantity;
    private final String productName;

    public StoreInventoryChange(int productId, int quantity, String productName) {
        this.productId = productId;
        this.quantity = Math.max(0, quantity);
        this.productName = productName;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getProductName() {
        return productName;
    }
}
