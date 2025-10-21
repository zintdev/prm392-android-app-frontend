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

}
