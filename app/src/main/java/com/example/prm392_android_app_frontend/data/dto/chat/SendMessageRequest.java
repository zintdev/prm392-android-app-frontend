package com.example.prm392_android_app_frontend.data.dto.chat;

public class SendMessageRequest {

    private Integer receiverId;
    private MessageDto.MessageType messageType;
    private String content;

    public SendMessageRequest(Integer receiverId, MessageDto.MessageType messageType, String content) {
        this.receiverId = receiverId;
        this.messageType = messageType;
        this.content = content;
    }

    // Getters (Gson cần)
    public Integer getReceiverId() {
        return receiverId;
    }

    public MessageDto.MessageType getMessageType() {
        return messageType;
    }

    public String getContent() {
        return content;
    }
}