package com.example.prm392_android_app_frontend.data.dto.chat;

public class ReadReceiptRequest {

    private Integer messageId;

    public ReadReceiptRequest(Integer messageId) {
        this.messageId = messageId;
    }

    // Getters
    public Integer getMessageId() {
        return messageId;
    }
}