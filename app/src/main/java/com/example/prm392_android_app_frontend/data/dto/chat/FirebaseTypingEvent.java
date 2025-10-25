package com.example.prm392_android_app_frontend.data.dto.chat;

public class FirebaseTypingEvent {

    private Integer userId;
    private Boolean isTyping;

    // Firebase SDK cần một constructor rỗng để parse data
    public FirebaseTypingEvent() {
    }

    // Getters
    public Integer getUserId() {
        return userId;
    }

    public Boolean isTyping() {
        // Firebase có thể lưu 'null'
        return isTyping != null && isTyping;
    }
}