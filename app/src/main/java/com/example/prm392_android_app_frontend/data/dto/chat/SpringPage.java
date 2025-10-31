package com.example.prm392_android_app_frontend.data.dto.chat;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * POJO này ánh xạ (maps) đối tượng 'Page' tiêu chuẩn
 * mà Spring Data trả về dưới dạng JSON.
 */
public class SpringPage<T> {

    @SerializedName("content")
    private List<T> content;

    @SerializedName("totalPages")
    private Integer totalPages;

    @SerializedName("totalElements")
    private Long totalElements;

    @SerializedName("size")
    private Integer size;

    @SerializedName("number")
    private Integer number; // Số trang hiện tại (bắt đầu từ 0)

    @SerializedName("first")
    private Boolean first;

    @SerializedName("last")
    private Boolean last;

    @SerializedName("numberOfElements")
    private Integer numberOfElements; // Số phần tử trong trang hiện tại

    // Getters (Bạn tự tạo)
    public List<T> getContent() {
        return content;
    }

    public Integer getTotalPages() {
        return totalPages;
    }
    // ... (Thêm các getter khác nếu bạn cần)
}