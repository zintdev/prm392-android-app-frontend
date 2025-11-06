package com.example.prm392_android_app_frontend.data.dto;

public class PublisherDto {
    private Integer id;
    private String name;
    private Integer foundedYear;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFoundedYear() {
        return foundedYear;
    }

    public void setFoundedYear(Integer foundedYear) {
        this.foundedYear = foundedYear;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PublisherDto() {
    }

    public PublisherDto(Integer id, String name, Integer foundedYear) {
        this.id = id;
        this.name = name;
        this.foundedYear = foundedYear;
    }
}
