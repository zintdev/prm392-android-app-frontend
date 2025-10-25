package com.example.prm392_android_app_frontend.data.dto;
import com.google.firebase.Timestamp;
public class MessageDto {
    public String id;
    public String text;
    public String senderId;
    public Timestamp createdAt;

    public MessageDto(){}
    public MessageDto(String id, String text, String senderId, Timestamp createdAt){
        this.id = id;
        this.text = text;
        this.senderId = senderId;

    }
}
