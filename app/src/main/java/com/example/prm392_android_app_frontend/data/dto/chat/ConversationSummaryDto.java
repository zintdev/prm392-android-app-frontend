package com.example.prm392_android_app_frontend.data.dto.chat;

import com.google.gson.annotations.SerializedName;
// (Giả sử bạn cũng có MessageDto.java trong cùng package)
// import com.example.prm392_android_app_frontend.data.dto.chat.MessageDto;

import java.util.List;

public class ConversationSummaryDto {

    @SerializedName("conversationId")
    private Integer conversationId;

    @SerializedName("lastMessage")
    private MessageDto lastMessage;

    @SerializedName("lastMessageAt")
    private Long lastMessageAt;

    @SerializedName("customerId")
    private Integer customerId;

    @SerializedName("customerName")
    private String customerName;

    // --- THÊM TRƯỜNG NÀY ---
    @SerializedName("customerAvatarUrl")
    private String customerAvatarUrl; // Backend cần JOIN bảng users để lấy trường này

    @SerializedName("unreadCount")
    private Integer unreadCount;

    // Getters và Setters
    public Integer getConversationId() { return conversationId; }
    public MessageDto getLastMessage() { return lastMessage; }
    public Long getLastMessageAt() { return lastMessageAt; }
    public Integer getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }

    // --- THÊM GETTER NÀY ---
    public String getCustomerAvatarUrl() { return customerAvatarUrl; }

    public Integer getUnreadCount() { return unreadCount; }
}