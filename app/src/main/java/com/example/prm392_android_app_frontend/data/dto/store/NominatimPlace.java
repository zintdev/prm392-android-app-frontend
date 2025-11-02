package com.example.prm392_android_app_frontend.data.dto.store;

import com.google.gson.annotations.SerializedName;

public class NominatimPlace {
    @SerializedName("lat")
    private String lat; // API trả về vĩ độ dạng String

    @SerializedName("lon")
    private String lon; // API trả về kinh độ dạng String

    @SerializedName("display_name")
    private String displayName; // Đây là địa chỉ đầy đủ

    // --- Getters ---
    public String getLat() { return lat; }
    public String getLon() { return lon; }
    public String getDisplayName() { return displayName; }

    // --- Setters (để tạo object từ kết quả LocationPicker) ---
    public void setLat(String lat) { this.lat = lat; }
    public void setLon(String lon) { this.lon = lon; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}