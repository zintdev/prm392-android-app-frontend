package com.example.prm392_android_app_frontend.presentation.util;

/**
 * Được sử dụng như một trình bao bọc cho dữ liệu được hiển thị qua LiveData đại diện cho một sự kiện.
 */
public class Event<T> {

    private T content;

    private boolean hasBeenHandled = false;

    public Event(T content) {
        this.content = content;
    }

    /**
     * Trả về nội dung và ngăn việc sử dụng lại nó.
     */
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    /**
     * Trả về nội dung, ngay cả khi nó đã được xử lý.
     */
    public T peekContent() {
        return content;
    }
}
