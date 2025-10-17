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

    // --- Getters and Setters ---
    // (Tạo getters và setters cho tất cả các trường)

    public int getCartItemId() { return cartItemId; }
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getImageUrl() { return imageUrl; }
    public double getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public boolean isSelected() { return selected; }
    public String getCurrency() { return currency; }
    public double getTaxRate() { return taxRate; }
}

