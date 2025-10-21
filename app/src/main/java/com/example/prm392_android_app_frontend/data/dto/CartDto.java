package com.example.prm392_android_app_frontend.data.dto;
import com.google.gson.annotations.SerializedName;
import java.util.List;
public class CartDto {

    @SerializedName("cartId")
    private int cartId;

    @SerializedName("status")
    private String status;

    @SerializedName("subtotal")
    private double subtotal;

    @SerializedName("taxTotal")
    private double taxTotal;

    @SerializedName("shippingFee")
    private double shippingFee;

    @SerializedName("grandTotal")
    private double grandTotal;

    @SerializedName("items")
    private List<CartItemDto> items;

    // --- Getters and Setters ---
    // (Tạo getters và setters cho tất cả các trường)

    public int getCartId() { return cartId; }
    public String getStatus() { return status; }
    public double getSubtotal() { return subtotal; }
    public double getTaxTotal() { return taxTotal; }
    public double getShippingFee() { return shippingFee; }
    public double getGrandTotal() { return grandTotal; }
    public List<CartItemDto> getItems() {return items; }
}
