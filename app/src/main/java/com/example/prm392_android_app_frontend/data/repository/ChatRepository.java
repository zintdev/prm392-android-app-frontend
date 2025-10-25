package com.example.prm392_android_app_frontend.data.repository;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.data.dto.MessageDto;
import com.example.prm392_android_app_frontend.data.dto.chat.FirebaseTypingEvent;
import com.example.prm392_android_app_frontend.data.dto.chat.ReadReceiptRequest;
import com.example.prm392_android_app_frontend.data.dto.chat.SendMessageRequest;
import com.example.prm392_android_app_frontend.data.dto.chat.TypingEventRequest;
import com.example.prm392_android_app_frontend.data.remote.api.ChatApi;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
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
        chatApi.sendMessage(request).enqueue(new Callback<MessageDto>() {
            @Override
            public void onResponse(Call<MessageDto> call, Response<MessageDto> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Message sent via REST, waiting for Firebase echo");
                } else {
                    Log.e(TAG, "Failed to send message: " + response.code());
                    // TODO: Báo lỗi về ViewModel
                }
            }
            @Override
            public void onFailure(Call<MessageDto> call, Throwable t) {
                Log.e(TAG, "Error sending message: " + t.getMessage());
                // TODO: Báo lỗi về ViewModel
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

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
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

    // --- DỌN DẸP ---
    // Phương thức này RẤT QUAN TRỌNG, được gọi bởi ViewModel khi nó bị hủy
    public void cleanUpAllListeners() {
        Log.d(TAG, "Cleaning up all Firebase listeners...");
        for (Map.Entry<DatabaseReference, ChildEventListener> entry : childListeners.entrySet()) {
            entry.getKey().removeEventListener(entry.getValue());
        }
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : valueListeners.entrySet()) {
            entry.getKey().removeEventListener(entry.getValue());
        }
        childListeners.clear();
        valueListeners.clear();
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


}