package com.example.prm392_android_app_frontend.data.dto.address;

import java.util.List;

public class DistrictDto {
    public int code;
    public String name;
    public List<WardDto> wards;

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public List<WardDto> getWards() {
        return wards;
    }
}
