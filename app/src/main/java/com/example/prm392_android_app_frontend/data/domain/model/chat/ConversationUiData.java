package com.example.prm392_android_app_frontend.data.domain.model.chat;

// Lớp này đại diện cho dữ liệu ĐÃ ĐƯỢC XỬ LÝ để hiển thị lên item_conversation.xml
public class ConversationUiData {
    private final Integer conversationId;
    private final Integer customerId;
    private final String participantName;
    private final String participantAvatarUrl;
    private final String lastMessage;
    private final long lastMessageTimestamp; // Dùng để sắp xếp và hiển thị thời gian
    private final int unreadCount;
    private final boolean isLastMessageFromAdmin;
    private final boolean isLastMessageRead;

    // Constructor đầy đủ
    public ConversationUiData(Integer conversationId, Integer customerId, String participantName, String participantAvatarUrl,
                              String lastMessage, long lastMessageTimestamp, int unreadCount,
                              boolean isLastMessageFromAdmin, boolean isLastMessageRead) {
        this.conversationId = conversationId;
        this.customerId = customerId;
        this.participantName = participantName;
        this.participantAvatarUrl = participantAvatarUrl;
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.unreadCount = unreadCount;
        this.isLastMessageFromAdmin = isLastMessageFromAdmin;
        this.isLastMessageRead = isLastMessageRead;
    }

    // Getters
    public Integer getConversationId() { return conversationId; }
    public Integer getCustomerId() { return customerId; }
    public String getParticipantName() { return participantName; }
    public String getParticipantAvatarUrl() { return participantAvatarUrl; }
    public String getLastMessage() { return lastMessage; }
    public long getLastMessageTimestamp() { return lastMessageTimestamp; }
    public int getUnreadCount() { return unreadCount; }
    public boolean isLastMessageFromAdmin() { return isLastMessageFromAdmin; }
    public boolean isLastMessageRead() { return isLastMessageRead; }
}