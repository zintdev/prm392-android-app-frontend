package com.example.prm392_android_app_frontend.data.dto.chat;


import com.google.gson.annotations.SerializedName;
import java.util.List;

// DTO này phải khớp với ConversationDto của Backend
public class ConversationDto {

    @SerializedName("conversationId")
    private Integer conversationId;

    @SerializedName("participantIds")
    private List<Integer> participantIds;

    @SerializedName("createdAt")
    private Long createdAt;

    @SerializedName("updatedAt")
    private Long updatedAt;

    // Constructors
    public ConversationDto() {}

    // Getters
    public Integer getConversationId() {
        return conversationId;
    }

    public List<Integer> getParticipantIds() {
        return participantIds;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    // Setters (for Firebase parsing)
    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    public void setParticipantIds(List<Integer> participantIds) {
        this.participantIds = participantIds;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}