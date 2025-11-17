package com.example.prm392_android_app_frontend.data.dto;

public class ProductFilter {
    public Integer categoryId;
    public Integer publisherId;
    public Integer artistId;
    public String  priceSort;
    public Integer releaseYearFrom;
    public Integer releaseYearTo;
    public Double  priceMin;
    public Double  priceMax;
    public ProductFilter(){

    }

    @Override
    public String toString() {
        return "ProductFilter{" +
                "categoryId=" + categoryId +
                ", publisherId=" + publisherId +
                ", artistId=" + artistId +
                ", priceSort='" + priceSort + '\'' +
                ", releaseYearFrom=" + releaseYearFrom +
                ", releaseYearTo=" + releaseYearTo +
                ", priceMin=" + priceMin +
                ", priceMax=" + priceMax +
                '}';
    }

}
