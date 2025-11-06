package com.example.prm392_android_app_frontend.data.dto.order;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderDto {

    @SerializedName("id")
    private Integer id;

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("cartId")
    private Integer cartId;

    @SerializedName("orderStatus")
    private String orderStatus;

    @SerializedName("shipmentMethod")
    private String shipmentMethod;

    @SerializedName("orderDate")
    private String orderDate;

    @SerializedName("shippingFullName")
    private String shippingFullName;

    @SerializedName("shippingPhone")
    private String shippingPhone;

    @SerializedName("shippingAddressLine1")
    private String shippingAddressLine1;

    @SerializedName("shippingAddressLine2")
    private String shippingAddressLine2;

    @SerializedName("shippingCityState")
    private String shippingCityState;

    @SerializedName("storeLocationId")
    private Integer storeLocationId;

    @SerializedName("storeName")
    private String storeName;

    @SerializedName("keepingExpiresAt")
    private String keepingExpiresAt;

    @SerializedName("subtotal")
    private Double subtotal;

    @SerializedName("taxTotal")
    private Double taxTotal;

    @SerializedName("shippingFee")
    private Double shippingFee;

    @SerializedName("grandTotal")
    private Double grandTotal;

    @SerializedName("items")
    private List<OrderItemDto> items;

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getCartId() {
        return cartId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getShipmentMethod() {
        return shipmentMethod;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getShippingFullName() {
        return shippingFullName;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }

    public String getShippingAddressLine1() {
        return shippingAddressLine1;
    }

    public String getShippingAddressLine2() {
        return shippingAddressLine2;
    }

    public String getShippingCityState() {
        return shippingCityState;
    }

    public Integer getStoreLocationId() {
        return storeLocationId;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getKeepingExpiresAt() {
        return keepingExpiresAt;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public Double getTaxTotal() {
        return taxTotal;
    }

    public Double getShippingFee() {
        return shippingFee;
    }

    public Double getGrandTotal() {
        return grandTotal;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }
}
