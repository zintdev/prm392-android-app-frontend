package com.example.prm392_android_app_frontend.data.remote.api;

// Import DTOs từ package DTO chính xác của bạn
import com.example.prm392_android_app_frontend.data.dto.MessageDto;
import com.example.prm392_android_app_frontend.data.dto.chat.ReadReceiptRequest;
import com.example.prm392_android_app_frontend.data.dto.chat.SendMessageRequest;
import com.example.prm392_android_app_frontend.data.dto.chat.TypingEventRequest;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Interface mới, định nghĩa các API endpoint cho tính năng Chat.
 * (Tương tự như AuthApi, ShopApi của bạn)
 */
public interface ChatApi {

    /**
     * Gửi một tin nhắn dạng TEXT
     * Tương ứng: @PostMapping("/api/chat/send")
     */
    @POST("chat/send")
    Call<MessageDto> sendMessage(@Body SendMessageRequest request);

    /**
     * Upload một file ảnh và gửi tin nhắn dạng IMAGE
     * Tương ứng: @PostMapping("/api/chat/upload_image")
     */
    @Multipart
    @POST("chat/upload_image")
    Call<MessageDto> uploadImage(
            @Part("receiverId") RequestBody receiverId,
            @Part MultipartBody.Part imageFile
    );

    /**
     * Lấy lịch sử tin nhắn của một cuộc hội thoại
     * Tương ứng: @GetMapping("/api/chat/history/{conversationId}")
     */
    @GET("chat/history/{conversationId}")
    Call<List<MessageDto>> getMessageHistory(@Path("conversationId") Integer conversationId);

    /**
     * Gửi sự kiện đang gõ phím
     * Tương ứng: @PostMapping("/api/chat/typing")
     */
    @POST("chat/typing")
    Call<Void> sendTypingEvent(@Body TypingEventRequest request);

    /**
     * Gửi sự kiện đã đọc tin nhắn
     * Tương ứng: @PostMapping("/api/chat/read")
     */
    @POST("chat/read")
    Call<Void> sendReadReceipt(@Body ReadReceiptRequest request);
}