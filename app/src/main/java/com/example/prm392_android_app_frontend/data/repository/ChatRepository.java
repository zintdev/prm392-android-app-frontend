package com.example.prm392_android_app_frontend.data.repository;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.data.dto.chat.ConversationDto;
import com.example.prm392_android_app_frontend.data.dto.chat.ConversationSummaryDto;
import com.example.prm392_android_app_frontend.data.dto.chat.MessageDto;
import com.example.prm392_android_app_frontend.data.dto.chat.FirebaseTypingEvent;
import com.example.prm392_android_app_frontend.data.dto.chat.ReadReceiptRequest;
import com.example.prm392_android_app_frontend.data.dto.chat.SendMessageRequest;
import com.example.prm392_android_app_frontend.data.dto.chat.SpringPage;
import com.example.prm392_android_app_frontend.data.dto.chat.TypingEventRequest;
import com.example.prm392_android_app_frontend.data.remote.api.ChatApi;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.storage.TokenStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {

    private static final String TAG = "ChatRepository";
    private final ChatApi chatApi;
    private final FirebaseDatabase firebaseDatabase;
    private final Application application; // Dùng để lấy ContentResolver cho upload ảnh

    // Dùng để lưu các listener và gỡ bỏ khi ViewModel bị hủy
    private final Map<DatabaseReference, ChildEventListener> childListeners = new HashMap<>();
    private final Map<DatabaseReference, ValueEventListener> valueListeners = new HashMap<>();
    private final MutableLiveData<MessageDto> newMessageData = new MutableLiveData<>();
    private final MutableLiveData<MessageDto> readReceiptData = new MutableLiveData<>();

    private final MutableLiveData<ConversationSummaryDto> updatedSummaryData = new MutableLiveData<>();
    private final Map<DatabaseReference, ChildEventListener> adminInboxListeners = new HashMap<>();

    public ChatRepository(Application application) {
        this.chatApi = ApiClient.getAuthClient(application).create(ChatApi.class);
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.application = application;
    }

    // --- PHẦN GỌI API (RETROFIT) ---

    // 1. Tải lịch sử chat (ĐÃ SỬA)
    public LiveData<List<MessageDto>> getMessageHistory(Integer conversationId) {
        MutableLiveData<List<MessageDto>> historyData = new MutableLiveData<>();
        chatApi.getMessageHistory(conversationId).enqueue(new Callback<List<MessageDto>>() {
            @Override
            public void onResponse(Call<List<MessageDto>> call, Response<List<MessageDto>> response) {
                if (response.isSuccessful()) {
                    historyData.setValue(response.body());

                    // Logic lấy timestamp của tin nhắn cuối cùng
                    long lastMessageTimestamp = 0;
                    if (response.body() != null && !response.body().isEmpty()) {
                        MessageDto lastMessage = response.body().get(response.body().size() - 1);
                        if(lastMessage.getCreatedAt() != null) {
                            lastMessageTimestamp = lastMessage.getCreatedAt();
                        }
                    }

                    // GỌI HÀM MỚI: Bắt đầu listener sau khi tải xong history
                    startRealtimeListeners(conversationId, lastMessageTimestamp);

                } else {
                    Log.e(TAG, "Failed to load history: " + response.code());
                    historyData.setValue(new ArrayList<>());
                }
            }
            @Override
            public void onFailure(Call<List<MessageDto>> call, Throwable t) {
                Log.e(TAG, "Error loading history: " + t.getMessage());
                historyData.setValue(new ArrayList<>());
            }
        });
        return historyData;
    }

    // 2. Gửi tin nhắn text
    public void sendTextMessage(SendMessageRequest request) {
        Log.d(TAG, "Sending message request: receiverId=" + request.getReceiverId() + ", type=" + request.getMessageType() + ", content=" + request.getContent());
        
        chatApi.sendMessage(request).enqueue(new Callback<MessageDto>() {
            @Override
            public void onResponse(Call<MessageDto> call, Response<MessageDto> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Message sent via REST, waiting for Firebase echo");
                } else {
                    Log.e(TAG, "Failed to send message: " + response.code());
                    if (response.code() == 403) {
                        Log.e(TAG, "403 Forbidden - Customer can only send messages to admin. Check receiverId: " + request.getReceiverId());
                    }
                    // Có thể thêm callback để báo lỗi về UI
                }
            }
            @Override
            public void onFailure(Call<MessageDto> call, Throwable t) {
                Log.e(TAG, "Error sending message: " + t.getMessage());
                // Có thể thêm callback để báo lỗi về UI
            }
        });
    }

    // 3. Upload ảnh
    public void uploadImage(Uri imageUri, Integer receiverId) {
        File tempFile = null;
        try {
            // 1. Get InputStream from Uri
            InputStream inputStream = application.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                throw new Exception("Cannot open input stream from Uri");
            }

            // 2. Create a temporary file in the app's cache directory
            // Using a more unique name is better, but this is an example
            tempFile = new File(application.getCacheDir(), "upload_temp_image.jpg");

            // 3. Copy the InputStream to the temporary file
            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
            } finally {
                inputStream.close();
            }

            // 4. Create RequestBody from the temporary file
            String contentType = application.getContentResolver().getType(imageUri);
            if (contentType == null) {
                contentType = "image/jpeg"; // Fallback
            }
            RequestBody requestFile = RequestBody.create(MediaType.parse(contentType), tempFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", tempFile.getName(), requestFile);
            RequestBody receiverIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(receiverId));

            // 5. Make the API call
            chatApi.uploadImage(receiverIdBody, body).enqueue(new Callback<MessageDto>() {
                @Override
                public void onResponse(Call<MessageDto> call, Response<MessageDto> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Image sent via REST, waiting for Firebase echo");
                    } else {
                        Log.e(TAG, "Failed to send image: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<MessageDto> call, Throwable t) {
                    Log.e(TAG, "Error sending image: " + t.getMessage());
                }

            });

        } catch (Exception e) {
            Log.e(TAG, "Error preparing image upload", e);
            if (tempFile != null) {
                tempFile.delete(); // Clean up on error
            }
        }
        // 6. Delete the temporary file
        if (tempFile != null) {
            tempFile.delete();
        }
    }

    // 4. Gửi sự kiện Typing
    public void sendTypingEvent(TypingEventRequest request) {
        chatApi.sendTypingEvent(request).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    // 5. Gửi sự kiện Đã đọc
    public void sendReadReceipt(ReadReceiptRequest request) {
        chatApi.sendReadReceipt(request).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    // --- PHẦN LẮNG NGHE (FIREBASE) ---

    // 1. Lắng nghe tin nhắn mới
    public LiveData<MessageDto> getNewMessageListener(Integer conversationId) {
        MutableLiveData<MessageDto> newMessageData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("messages").child(String.valueOf(conversationId));

        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    MessageDto message = snapshot.getValue(MessageDto.class);
                    newMessageData.postValue(message);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse new message", e);
                }
            }
            // (onChildChanged, onChildRemoved, onChildMoved... có thể implement sau)
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "messagesListener:onCancelled", error.toException());
            }
        };

        // Chỉ lắng nghe tin nhắn mới (sau khi đã load history)
        ref.orderByChild("createdAt").startAt(System.currentTimeMillis()).addChildEventListener(listener);
        childListeners.put(ref, listener); // Lưu lại để dọn dẹp
        return newMessageData;
    }

    // 2. Lắng nghe trạng thái Typing
    public LiveData<String> getTypingStatusListener(Integer conversationId, Integer currentUserId) {
        MutableLiveData<String> typingStatusData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("events").child(String.valueOf(conversationId)).child("typing");

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String typingStatus = "";
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    FirebaseTypingEvent event = userSnapshot.getValue(FirebaseTypingEvent.class);
                    if (event != null && !Objects.equals(event.getUserId(), currentUserId) && event.isTyping()) {
                        typingStatus = "đang gõ..."; // TODO: Lấy tên user
                        break;
                    }
                }
                typingStatusData.postValue(typingStatus);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };

        ref.addValueEventListener(listener);
        valueListeners.put(ref, listener); // Lưu lại để dọn dẹp
        return typingStatusData;
    }

    // 3. Lắng nghe sự kiện Đã đọc
    public LiveData<MessageDto> getReadReceiptListener(Integer conversationId) {
        MutableLiveData<MessageDto> readReceiptData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("events").child(String.valueOf(conversationId)).child("read");

        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                try {
                    MessageDto readMessage = snapshot.getValue(MessageDto.class);
                    if (readMessage != null) {
                        readReceiptData.postValue(readMessage);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse read receipt", e);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    MessageDto readMessage = snapshot.getValue(MessageDto.class);
                    if (readMessage != null) {
                        readReceiptData.postValue(readMessage);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse read receipt update", e);
                }
            }

            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "readReceiptListener:onCancelled", error.toException());
            }
        };

        ref.orderByChild("readAt").startAt(System.currentTimeMillis()).addChildEventListener(listener);
        childListeners.put(ref, listener); // Lưu lại để dọn dẹp
        return readReceiptData;
    }

    // 4. Cập nhật Presence (Online/Offline)
    public void setPresence(Integer currentUserId, boolean isOnline) {
        if (currentUserId == null || currentUserId == 0) return;
        DatabaseReference presenceRef = firebaseDatabase.getReference("status").child(String.valueOf(currentUserId));
        if (isOnline) {
            presenceRef.setValue("online");
            presenceRef.onDisconnect().setValue("offline");
        } else {
            presenceRef.setValue("offline");
        }
    }

    private void startRealtimeListeners(Integer conversationId, long lastMessageTimestamp) {
        // --- 1. New Message Listener ---
        DatabaseReference msgRef = firebaseDatabase.getReference("messages").child(String.valueOf(conversationId));
        ChildEventListener msgListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    // Dùng DTO đã sửa (với Long createdAt)
                    MessageDto message = snapshot.getValue(MessageDto.class);
                    newMessageData.postValue(message);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse new message", e); // Lỗi sẽ hiện ở đây nếu DTO vẫn sai
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "messagesListener:onCancelled", error.toException());
            }
        };

        // Chỉ lắng nghe các tin nhắn có timestamp LỚN HƠN tin cuối cùng
        msgRef.orderByChild("createdAt").startAt(lastMessageTimestamp + 1).addChildEventListener(msgListener);
        childListeners.put(msgRef, msgListener); // Lưu lại để dọn dẹp

        // --- 2. Read Receipt Listener ---
        DatabaseReference readRef = firebaseDatabase.getReference("events").child(String.valueOf(conversationId)).child("read");
        ChildEventListener readListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    MessageDto readMessage = snapshot.getValue(MessageDto.class);
                    readReceiptData.postValue(readMessage);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse read receipt", e);
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };

        // Chỉ lắng nghe các sự kiện "đã đọc" xảy ra từ bây giờ
        readRef.orderByChild("readAt").startAt(System.currentTimeMillis()).addChildEventListener(readListener);
        childListeners.put(readRef, readListener); // Lưu lại để dọn dẹp
    }
    // ===================================

    // --- THÊM HÀM MỚI ĐỂ GỌI API TÌM CONVERSATION ---
    public LiveData<ConversationDto> findOrCreateConversation(Integer receiverId) {
        MutableLiveData<ConversationDto> conversationData = new MutableLiveData<>();

        // Sử dụng endpoint mới: /api/chat/conversation
        chatApi.getCustomerConversation().enqueue(new Callback<ConversationDto>() {
            @Override
            public void onResponse(Call<ConversationDto> call, Response<ConversationDto> response) {
                if (response.isSuccessful()) {
                    conversationData.postValue(response.body());
                } else {
                    Log.e(TAG, "Failed to get customer conversation: " + response.code());
                    // Fallback: Tạo conversation ID local khi backend có lỗi
                    ConversationDto fallbackConversation = createFallbackConversation();
                    conversationData.postValue(fallbackConversation);
                }
            }

            @Override
            public void onFailure(Call<ConversationDto> call, Throwable t) {
                Log.e(TAG, "Error getting customer conversation: " + t.getMessage());
                // Fallback: Tạo conversation ID local khi có lỗi network
                ConversationDto fallbackConversation = createFallbackConversation();
                conversationData.postValue(fallbackConversation);
            }
        });

        return conversationData;
    }

    /**
     * Tạo ConversationDto fallback khi backend có lỗi (500, network, etc.)
     */
    private ConversationDto createFallbackConversation() {
        ConversationDto conversation = new ConversationDto();
        
        // Lấy user ID thực tế từ TokenStore
        int currentUserId = TokenStore.getUserId(application);
        int adminId = 1; // Admin ID cố định (chỉ có 1 admin duy nhất)
        
        // Tạo conversation ID dựa trên user IDs
        int minId = Math.min(currentUserId, adminId);
        int maxId = Math.max(currentUserId, adminId);
        int conversationId = minId * 10000 + maxId;
        
        conversation.setConversationId(conversationId);
        conversation.setCreatedAt(System.currentTimeMillis());
        conversation.setUpdatedAt(System.currentTimeMillis());
        
        List<Integer> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(adminId);
        conversation.setParticipantIds(participants);
        
        Log.d(TAG, "Created fallback conversation with ID: " + conversationId + 
              ", currentUserId: " + currentUserId + ", adminId: " + adminId);
        return conversation;
    }

    /**
     * Lấy số tin nhắn chưa đọc cho customer
     */
    public LiveData<Integer> getUnreadCount(Integer conversationId) {
        MutableLiveData<Integer> unreadCountData = new MutableLiveData<>();

        chatApi.getUnreadCount(conversationId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful()) {
                    unreadCountData.postValue(response.body());
                } else {
                    Log.e(TAG, "Failed to get unread count: " + response.code());
                    unreadCountData.postValue(0); // Fallback to 0
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(TAG, "Error getting unread count: " + t.getMessage());
                unreadCountData.postValue(0); // Fallback to 0
            }
        });

        return unreadCountData;
    }

    public Call<SpringPage<ConversationSummaryDto>> getAdminConversations(int page, int size) {
        // Sắp xếp theo "lastMessageAt" mới nhất
        String sort = "lastMessageAt,desc";
        return chatApi.getConversations(page, size, sort);
    }


    /**
     * THÊM MỚI: Hàm lắng nghe cập nhật cho Admin Inbox
     */
    public LiveData<ConversationSummaryDto> getAdminUpdatesListener(Integer adminId) {
        DatabaseReference ref = firebaseDatabase.getReference("admin_inbox_updates")
                .child(String.valueOf(adminId));

        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Được gọi khi tải ban đầu VÀ khi có conversation mới
                try {
                    ConversationSummaryDto summary = snapshot.getValue(ConversationSummaryDto.class);
                    updatedSummaryData.postValue(summary);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse admin update (added)", e);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Được gọi khi một conversation có tin nhắn mới (quan trọng nhất)
                try {
                    ConversationSummaryDto summary = snapshot.getValue(ConversationSummaryDto.class);
                    updatedSummaryData.postValue(summary);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse admin update (changed)", e);
                }
            }

            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "adminInboxListener:onCancelled", error.toException());
            }
        };

        // Sắp xếp theo "lastMessageAt" để lấy các tin mới nhất trước
        ref.orderByChild("lastMessageAt").addChildEventListener(listener);
        adminInboxListeners.put(ref, listener); // Lưu lại để dọn dẹp

        return updatedSummaryData;
    }


    // --- SỬA ĐỔI: Thêm vào hàm dọn dẹp ---
    public void cleanUpAllListeners() {
        Log.d(TAG, "Cleaning up all Firebase listeners...");
        for (Map.Entry<DatabaseReference, ChildEventListener> entry : childListeners.entrySet()) {
            entry.getKey().removeEventListener(entry.getValue());
        }
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : valueListeners.entrySet()) {
            entry.getKey().removeEventListener(entry.getValue());
        }

        // --- THÊM MỚI ---
        for (Map.Entry<DatabaseReference, ChildEventListener> entry : adminInboxListeners.entrySet()) {
            entry.getKey().removeEventListener(entry.getValue());
        }
        adminInboxListeners.clear();
        // --- KẾT THÚC THÊM MỚI ---

        childListeners.clear();
        valueListeners.clear();
    }

}