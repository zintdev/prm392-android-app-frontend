package com.example.prm392_android_app_frontend.data.dto;

public class PublisherDto {
    private Integer id;
    private String name;
    private Integer founderYear;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFounderYear() {
        return founderYear;
    }

    public void setFounderYear(Integer founderYear) {
        this.founderYear = founderYear;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
