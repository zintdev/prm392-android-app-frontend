package com.example.prm392_android_app_frontend.data.dto;

public class CategoryDto {
    public Integer id;
    public String name;

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public Integer getId() {return id;}

    public void setId(Integer id) {
        this.id = id;
    }

    public CategoryDto() {
    }

    public CategoryDto(String name, Integer id) {
        this.name = name;
        this.id = id;
    }
}
