package com.example.prm392_android_app_frontend.data.dto;
import com.google.firebase.Timestamp;
import com.google.gson.annotations.SerializedName;

// Lớp này là POJO (Plain Old Java Object) để Gson
// tự động parse JSON từ API thành object Java.
public class MessageDto {

    // Enum này PHẢI khớp 100% với enum MessageType trên Backend
    public enum MessageType {
        TEXT, IMAGE
    }

    @SerializedName("id")
    private Integer id;

    @SerializedName("conversationId")
    private Integer conversationId;

    @SerializedName("senderId")
    private Integer senderId;

    @SerializedName("messageType")
    private MessageType messageType;

    @SerializedName("content")
    private String content;

    // Backend (Java) dùng Instant, khi serialize thành JSON,
    // nó thường là một String ISO 8601 (ví dụ: "2025-10-24T10:30:00Z").
    // Nhận nó dưới dạng String là an toàn nhất trên Android.
    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("readAt")
    private String readAt; // Có thể null

    // Getters
    public Integer getId() {
        return id;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getReadAt() {
        return readAt;
    }

    // Setters (Adapter cần)
    public void setReadAt(String readAt) {
        this.readAt = readAt;
    }
}