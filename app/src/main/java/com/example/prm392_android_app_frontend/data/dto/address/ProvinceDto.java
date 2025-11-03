package com.example.prm392_android_app_frontend.data.dto.address;

import java.util.List;

public class ProvinceDto {
    public int code;
    public String name;
    public List<DistrictDto> districts;

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public List<DistrictDto> getDistricts() {
        return districts;
    }
}
