package com.example.prm392_android_app_frontend.data.dto.chat;

public class TypingEventRequest {

    private Integer conversationId;
    private Boolean isTyping;

    public TypingEventRequest(Integer conversationId, Boolean isTyping) {
        this.conversationId = conversationId;
        this.isTyping = isTyping;
    }

    // Getters
    public Integer getConversationId() {
        return conversationId;
    }

    public Boolean getIsTyping() {
        return isTyping;
    }
}