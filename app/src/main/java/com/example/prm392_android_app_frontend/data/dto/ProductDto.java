package com.example.prm392_android_app_frontend.data.dto;

import com.google.gson.annotations.SerializedName;

public class ProductDto {
    // Sử dụng @SerializedName nếu tên biến khác với tên key trong JSON
    @SerializedName("id")
    private int id; // ID là kiểu số nguyên (int)

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private double price;

    @SerializedName("quantity")
    private int quantity; // Quantity là kiểu số nguyên (int)

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("artistName")
    private String artistName;

    @SerializedName("publisherName")
    private String publisherName;

    @SerializedName("categoryName")
    private String categoryName;

    @SerializedName("releaseDate")
    private String releaseDate;

    @SerializedName("createdAt")
    private String createdAt;

    // --- Thêm getters cho tất cả các trường ---

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // --- Thêm setters cho tất cả các trường ---

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

}
