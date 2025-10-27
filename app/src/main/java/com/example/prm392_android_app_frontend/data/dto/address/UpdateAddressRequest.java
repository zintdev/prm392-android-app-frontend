package com.example.prm392_android_app_frontend.data.dto.address;

public class UpdateAddressRequest {
    private int userId;
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCityState;

    public UpdateAddressRequest(int userId, String line1, String line2, String cityState) {
        this.userId = userId;
        this.shippingAddressLine1 = line1;
        this.shippingAddressLine2 = line2;
        this.shippingCityState = cityState;
    }

    public int getUserId() { return userId; }
    public String getShippingAddressLine1() { return shippingAddressLine1; }
    public String getShippingAddressLine2() { return shippingAddressLine2; }
    public String getShippingCityState() { return shippingCityState; }

    public void setUserId(int userId) { this.userId = userId; }
    public void setShippingAddressLine1(String v) { this.shippingAddressLine1 = v; }
    public void setShippingAddressLine2(String v) { this.shippingAddressLine2 = v; }
    public void setShippingCityState(String v) { this.shippingCityState = v; }
}
