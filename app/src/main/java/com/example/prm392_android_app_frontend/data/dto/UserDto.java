package com.example.prm392_android_app_frontend.data.dto;

import com.google.gson.annotations.SerializedName;

public class UserDto {
    public int id;
    public String username;
    public String email;
    public String phoneNumber;
    public String role;
    @SerializedName("address")
    public String address;
    @SerializedName("storeAddress")
    public String storeAddress;
    public Integer storeLocationId;
    public String storeName;
}
