package com.example.prm392_android_app_frontend.data.dto.order;

import com.google.gson.annotations.SerializedName;

public class OrderItemDto {

    @SerializedName("id")
    private Integer id;

    @SerializedName("productId")
    private Integer productId;

    @SerializedName("productName")
    private String productName;

    @SerializedName("unitPrice")
    private Double unitPrice;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("taxRate")
    private Double taxRate;

    @SerializedName("currencyCode")
    private String currencyCode;

    @SerializedName("imageUrl")
    private String imageUrl;

    public Integer getId() {
        return id;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Double getTaxRate() {
        return taxRate;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
