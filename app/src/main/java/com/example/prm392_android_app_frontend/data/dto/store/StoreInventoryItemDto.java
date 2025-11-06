package com.example.prm392_android_app_frontend.data.dto.store;

import com.google.gson.annotations.SerializedName;

public class StoreInventoryItemDto {

    @SerializedName("storeLocationId")
    private Integer storeLocationId;

    @SerializedName("storeName")
    private String storeName;

    @SerializedName("productId")
    private Integer productId;

    @SerializedName("productName")
    private String productName;

    @SerializedName("productImageUrl")
    private String productImageUrl;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("productQuantity")
    private Integer productQuantity;

    @SerializedName("totalAllocatedQuantity")
    private Integer totalAllocatedQuantity;

    @SerializedName("remainingQuantity")
    private Integer remainingQuantity;

    @SerializedName("availableForStore")
    private Integer availableForStore;

    public Integer getStoreLocationId() {
        return storeLocationId;
    }

    public String getStoreName() {
        return storeName;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getProductQuantity() {
        return productQuantity;
    }

    public Integer getTotalAllocatedQuantity() {
        return totalAllocatedQuantity;
    }

    public Integer getRemainingQuantity() {
        return remainingQuantity;
    }

    public Integer getAvailableForStore() {
        return availableForStore;
    }
}
