package com.example.prm392_android_app_frontend.data.dto.store;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

/**
 * DTO này dùng để hứng (nhận) phản hồi từ backend sau khi tạo cửa hàng thành công.
 * Nó phải khớp với tệp 'StoreLocationResponse.java' của backend.
 */
public class StoreLocationResponse {

    @SerializedName("id")
    private Integer id;

    @SerializedName("storeName")
    private String storeName;

    @SerializedName("latitude")
    private BigDecimal latitude;

    @SerializedName("longitude")
    private BigDecimal longitude;

    @SerializedName("address")
    private String address;

    // (Bạn có thể thêm Getters và Setters nếu cần truy cập các trường này)

    public Integer getId() {
        return id;
    }

    public String getStoreName() {
        return storeName;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }
}
