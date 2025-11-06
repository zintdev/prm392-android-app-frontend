package com.example.prm392_android_app_frontend.data.dto.store;

import com.google.gson.annotations.SerializedName;

public class StoreNearbyDto {
    @SerializedName("storeId")
    public int storeId;

    @SerializedName(value = "name", alternate = {"storeName"})
    public String name;

    @SerializedName(value = "address", alternate = {"storeAddress"})
    public String address;

    @SerializedName("latitude")
    public double latitude;

    @SerializedName("longitude")
    public double longitude;

    @SerializedName(value = "distanceKm", alternate = {"distance"})
    public double distanceKm;

    @SerializedName(value = "quantity", alternate = {"availableQuantity"})
    public Integer quantity;
}