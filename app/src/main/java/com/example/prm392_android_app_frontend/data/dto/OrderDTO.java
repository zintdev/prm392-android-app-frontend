package com.example.prm392_android_app_frontend.data.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderDTO {
    private int id;
    private int userId;
    private int cartId;
    
    @SerializedName("orderStatus")
    private String status;
    
    private String shipmentMethod;
    
    @SerializedName("orderDate")
    private String createdAt;
    
    private String shippingFullName;
    private String shippingPhone;
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCityState;
    
    private double subtotal;
    private double taxTotal;
    private double shippingFee;
    
    @SerializedName("grandTotal")
    private double totalAmount;
    
    private List<OrderItemDTO> items;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getShipmentMethod() { return shipmentMethod; }
    public void setShipmentMethod(String shipmentMethod) { this.shipmentMethod = shipmentMethod; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getShippingFullName() { return shippingFullName; }
    public void setShippingFullName(String shippingFullName) { this.shippingFullName = shippingFullName; }
    
    public String getShippingPhone() { return shippingPhone; }
    public void setShippingPhone(String shippingPhone) { this.shippingPhone = shippingPhone; }
    
    public String getShippingAddressLine1() { return shippingAddressLine1; }
    public void setShippingAddressLine1(String shippingAddressLine1) { this.shippingAddressLine1 = shippingAddressLine1; }
    
    public String getShippingAddressLine2() { return shippingAddressLine2; }
    public void setShippingAddressLine2(String shippingAddressLine2) { this.shippingAddressLine2 = shippingAddressLine2; }
    
    public String getShippingCityState() { return shippingCityState; }
    public void setShippingCityState(String shippingCityState) { this.shippingCityState = shippingCityState; }
    
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    
    public double getTaxTotal() { return taxTotal; }
    public void setTaxTotal(double taxTotal) { this.taxTotal = taxTotal; }
    
    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
}