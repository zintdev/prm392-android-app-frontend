package com.example.prm392_android_app_frontend.data.dto;

import com.google.gson.annotations.SerializedName;

public class CartItemDto {

    @SerializedName("cartItemId")
    private int cartItemId;

    @SerializedName("productId")
    private int productId;

    @SerializedName("productName")
    private String productName;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("unitPrice")
    private double unitPrice;

    @SerializedName("quantity")
    private int quantity;


    @SerializedName("selected")
    private boolean selected;

    @SerializedName("currency")
    private String currency;

    @SerializedName("taxRate")
    private double taxRate;

    // --- Getters ---

    public int getCartItemId() {
        return cartItemId;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getCurrency() {
        return currency;
    }

    public double getTaxRate() {
        return taxRate;
    }


    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }
}
