package com.example.prm392_android_app_frontend.data.dto.store;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class StoreLocationRequest {

    @SerializedName("storeName")
    private String storeName; // Tên cửa hàng (mới)

    @SerializedName("latitude")
    private BigDecimal latitude;

    @SerializedName("longitude")
    private BigDecimal longitude;

    @SerializedName("address")
    private String address; // Địa chỉ đầy đủ (đã có)

    // Constructor mới
    public StoreLocationRequest(BigDecimal latitude, BigDecimal longitude, String storeName, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.storeName = storeName;
        this.address = address;
    }

    // (Getters và Setters nếu bạn cần)
}
