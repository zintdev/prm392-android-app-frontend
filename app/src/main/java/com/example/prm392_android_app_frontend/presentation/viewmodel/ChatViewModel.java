package com.example.prm392_android_app_frontend.presentation.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.data.dto.MessageDto;
import com.example.prm392_android_app_frontend.data.dto.chat.ReadReceiptRequest;
import com.example.prm392_android_app_frontend.data.dto.chat.SendMessageRequest;
import com.example.prm392_android_app_frontend.data.dto.chat.TypingEventRequest;
import com.example.prm392_android_app_frontend.data.repository.ChatRepository;

import java.util.List;
import java.util.Objects;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository;

    // IDs (cần được set từ Activity)
    private Integer conversationId;
    private Integer currentUserId;
    private Integer receiverId;

    // LiveData cho View quan sát
    private LiveData<List<MessageDto>> historyMessages;
    private LiveData<MessageDto> newMessage;
    private LiveData<String> typingStatus;
    private LiveData<MessageDto> readReceipt;

    // Logic xử lý typing
    private boolean isTyping = false;
    private final Handler typingHandler = new Handler(Looper.getMainLooper());
    private Runnable typingTimeoutRunnable;

    public ChatViewModel(Application application) {
        super(application);
        this.chatRepository = new ChatRepository(application);
    }

    // Gọi phương thức này 1 lần duy nhất từ Activity
    public void init(Integer conversationId, Integer currentUserId, Integer receiverId) {
        this.conversationId = conversationId;
        this.currentUserId = currentUserId;
        this.receiverId = receiverId;

        // Load lịch sử 1 lần
        this.historyMessages = chatRepository.getMessageHistory(conversationId);

        // Bắt đầu lắng nghe các sự kiện realtime
        this.newMessage = chatRepository.getNewMessageListener(conversationId);
        this.typingStatus = chatRepository.getTypingStatusListener(conversationId, currentUserId);
        this.readReceipt = chatRepository.getReadReceiptListener(conversationId);
    }

    // --- Getters cho View ---
    public LiveData<List<MessageDto>> getHistoryMessages() { return historyMessages; }
    public LiveData<MessageDto> getNewMessage() { return newMessage; }
    public LiveData<String> getTypingStatus() { return typingStatus; }
    public LiveData<MessageDto> getReadReceipt() { return readReceipt; }

    // --- Các hành động (Action) từ View ---

    public void sendTextMessage(String content) {
        if (content == null || content.trim().isEmpty()) return;

        SendMessageRequest request = new SendMessageRequest(
                receiverId,
                MessageDto.MessageType.TEXT,
                content.trim()
        );
        chatRepository.sendTextMessage(request);
    }

    public void uploadImage(Uri imageUri) {
        if (imageUri == null) return;
        chatRepository.uploadImage(imageUri, receiverId);
    }

    public void notifyTyping(boolean isCurrentlyTyping) {
        // Nếu trạng thái không đổi, không làm gì
        if (isCurrentlyTyping == isTyping) return;

        this.isTyping = isCurrentlyTyping;

        // Gửi sự kiện ngay lập tức
        chatRepository.sendTypingEvent(new TypingEventRequest(conversationId, isTyping));

        // Hủy bỏ runnable cũ (nếu có)
        typingHandler.removeCallbacks(typingTimeoutRunnable);

        // Nếu đang gõ, set timeout để tự động gửi "stop typing"
        if (isCurrentlyTyping) {
            typingTimeoutRunnable = () -> {
                this.isTyping = false;
                chatRepository.sendTypingEvent(new TypingEventRequest(conversationId, false));
            };
            typingHandler.postDelayed(typingTimeoutRunnable, 2000); // 2 giây
        }
    }

    public void notifyMessageRead(MessageDto message) {
        if (message == null || message.getId() == null) return;

        // Chỉ gửi "đã đọc" nếu đó là tin nhắn của người khác VÀ chưa được đọc
        if (!Objects.equals(message.getSenderId(), currentUserId) && message.getReadAt() == null) {
            ReadReceiptRequest request = new ReadReceiptRequest(message.getId());
            chatRepository.sendReadReceipt(request);
        }
    }

    public void setPresence(boolean isOnline) {
        chatRepository.setPresence(currentUserId, isOnline);
    }

    // Tự động gọi khi ViewModel bị hủy (Activity/Fragment bị destroy)
    @Override
    protected void onCleared() {
        super.onCleared();
        // Rất quan trọng: Báo cho Repository dọn dẹp listener
        chatRepository.cleanUpAllListeners();
        // Set offline
        setPresence(false);
        // Dọn dẹp typing handler
        typingHandler.removeCallbacksAndMessages(null);
    }
}