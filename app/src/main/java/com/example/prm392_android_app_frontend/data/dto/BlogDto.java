package com.example.prm392_android_app_frontend.data.dto;

import java.io.Serializable;

/**
 * BlogDto - Data Transfer Object cho bài viết Blog
 * Dùng để chứa dữ liệu blog khi lấy từ API hoặc truyền giữa Activity/Fragment.
 */
public class BlogDto implements Serializable {

    private String id;
    private String title;
    private String date;
    private String author;
    private String summary;
    private String content;
    private String imageUrl;

    // --- Constructors ---
    public BlogDto() {
    }

    public BlogDto(String id, String title, String date, String author, String summary) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.author = author;
        this.summary = summary;
        this.content = summary; // fallback
        this.imageUrl = null;
    }

    public BlogDto(String id, String title, String date, String author, String summary, String content) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.author = author;
        this.summary = summary;
        this.content = content;
        this.imageUrl = null;
    }

    public BlogDto(String id, String title, String date, String author,
                   String summary, String content, String imageUrl) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.author = author;
        this.summary = summary;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }

    public String getSummary() {
        return summary;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // --- Setters ---
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // --- Helper ---
    @Override
    public String toString() {
        return "BlogDto{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", date='" + date + '\'' +
                ", author='" + author + '\'' +
                ", summary='" + summary + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
