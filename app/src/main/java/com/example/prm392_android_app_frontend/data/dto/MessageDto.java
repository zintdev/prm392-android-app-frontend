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
    private Long createdAt;

    @SerializedName("readAt")
    private Long readAt; // Có thể null

    public MessageDto() {
    }

    public Integer getId() { return id; }
    public Integer getConversationId() { return conversationId; }
    public Integer getSenderId() { return senderId; }
    public MessageType getMessageType() { return messageType; }
    public String getContent() { return content; }

    // THAY ĐỔI: String -> Long
    public Long getCreatedAt() {
        return createdAt;
    }

    // THAY ĐỔI: String -> Long
    public Long getReadAt() {
        return readAt;
    }

    // Setters (Adapter cần)
    public void setReadAt(Long readAt) {
        this.readAt = readAt;
    }
}