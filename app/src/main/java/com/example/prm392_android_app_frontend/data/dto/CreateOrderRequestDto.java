package com.example.prm392_android_app_frontend.data.dto;

import com.google.gson.annotations.SerializedName;

public class CreateOrderRequestDto {
    private int userId;
    private String shipmentMethod;
    @SerializedName("paymentMethod")
    private String paymentMethod;
    private String shippingFullName;
    private String shippingPhone;
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCityState;
    private Integer storeLocationId;

    public CreateOrderRequestDto(int userId, String shipmentMethod, String paymentMethod,
                                 String shippingFullName, String shippingPhone,
                                 String shippingAddressLine1, String shippingAddressLine2,
                                 String shippingCityState, Integer storeLocationId) {
        this.userId = userId;
        this.shipmentMethod = shipmentMethod;
        this.paymentMethod = paymentMethod;
        this.shippingFullName = shippingFullName;
        this.shippingPhone = shippingPhone;
        this.shippingAddressLine1 = shippingAddressLine1;
        this.shippingAddressLine2 = shippingAddressLine2;
        this.shippingCityState = shippingCityState;
        this.storeLocationId = storeLocationId;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getShipmentMethod() { return shipmentMethod; }
    public void setShipmentMethod(String shipmentMethod) { this.shipmentMethod = shipmentMethod; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

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
    public Integer getStoreLocationId() { return storeLocationId; }
    public void setStoreLocationId(Integer storeLocationId) { this.storeLocationId = storeLocationId; }
}