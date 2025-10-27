package com.example.prm392_android_app_frontend.data.dto.chat;

public class FirebaseTypingEvent {

    private Integer userId;
    private Boolean isTyping;
    private Long timestamp;

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

    public Long getTimestamp() {
        return timestamp;
    }

    // Setters (for Firebase parsing)
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setIsTyping(Boolean isTyping) {
        this.isTyping = isTyping;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}