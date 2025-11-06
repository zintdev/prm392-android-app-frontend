package com.example.prm392_android_app_frontend.presentation.viewmodel;

import android.app.Application;
import android.util.Log; // <-- Thêm Log để debug
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.data.dto.chat.ConversationSummaryDto;
import com.example.prm392_android_app_frontend.data.dto.chat.MessageDto;
import com.example.prm392_android_app_frontend.data.dto.chat.SpringPage;
import com.example.prm392_android_app_frontend.data.domain.model.chat.ConversationUiData;
import com.example.prm392_android_app_frontend.data.repository.ChatRepository;
import com.example.prm392_android_app_frontend.storage.TokenStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminChatViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository;
    private final Integer currentAdminId;

    private final MutableLiveData<List<ConversationUiData>> _conversationList = new MutableLiveData<>();
    public LiveData<List<ConversationUiData>> conversationList = _conversationList;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    // Listener cho real-time update
    private LiveData<ConversationSummaryDto> updateListenerLiveData;

    public AdminChatViewModel(@NonNull Application application) {
        super(application);
        this.chatRepository = new ChatRepository(application);
        this.currentAdminId = TokenStore.getUserId(application);
    }

    /**
     * Hàm tải dữ liệu ban đầu
     */
    public void fetchConversations() {
        _isLoading.setValue(true);
        chatRepository.getAdminConversations(0, 20).enqueue(new Callback<SpringPage<ConversationSummaryDto>>() {
            @Override
            public void onResponse(Call<SpringPage<ConversationSummaryDto>> call, Response<SpringPage<ConversationSummaryDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ConversationSummaryDto> dtos = response.body().getContent();
                    if (dtos != null) {
                        // GỌI HÀM SỐ NHIỀU
                        List<ConversationUiData> uiData = mapDtosToUiData(dtos);
                        Log.d("AdminChatViewModel", "Fetch success, UI data size: " + uiData.size());
                        _conversationList.postValue(uiData);
                    } else {
                        Log.e("AdminChatViewModel", "Fetch failed: DTO content is null");
                        _conversationList.postValue(new ArrayList<>()); // Trả về list rỗng
                    }
                } else {
                    Log.e("AdminChatViewModel", "Fetch failed: " + response.message());
                    _errorMessage.postValue("Lỗi khi tải dữ liệu: " + response.message());
                }
                _isLoading.postValue(false);
            }

            @Override
            public void onFailure(Call<SpringPage<ConversationSummaryDto>> call, Throwable t) {
                Log.e("AdminChatViewModel", "Fetch failure: " + t.getMessage());
                _errorMessage.postValue("Lỗi mạng: " + t.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    /**
     * Hàm tìm kiếm conversations theo tên khách hàng
     */
    public void searchConversations(String customerName) {
        if (customerName == null || customerName.trim().isEmpty()) {
            // Nếu tìm kiếm rỗng, tải lại danh sách ban đầu
            fetchConversations();
            return;
        }

        _isLoading.setValue(true);
        chatRepository.searchAdminConversations(customerName.trim(), 0, 20).enqueue(new Callback<SpringPage<ConversationSummaryDto>>() {
            @Override
            public void onResponse(Call<SpringPage<ConversationSummaryDto>> call, Response<SpringPage<ConversationSummaryDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ConversationSummaryDto> dtos = response.body().getContent();
                    if (dtos != null) {
                        // Log để debug
                        Log.d("AdminChatViewModel", "Search returned " + dtos.size() + " results");
                        for (ConversationSummaryDto dto : dtos) {
                            Log.d("AdminChatViewModel", "Search result - conversationId: " + dto.getConversationId() 
                                    + ", customerName: " + dto.getCustomerName() 
                                    + ", customerId: " + dto.getCustomerId()
                                    + ", unreadCount: " + dto.getUnreadCount());
                        }
                        
                        // Map trước
                        List<ConversationUiData> mapped = mapDtosToUiData(dtos);

                        // Merge với danh sách hiện có để tránh rơi vào fallback khi API search thiếu trường
                        List<ConversationUiData> current = _conversationList.getValue();
                        if (current != null && !current.isEmpty()) {
                            List<ConversationUiData> merged = new ArrayList<>();
                            for (ConversationUiData ui : mapped) {
                                ConversationUiData existing = null;
                                for (ConversationUiData it : current) {
                                    if (it.getConversationId().equals(ui.getConversationId())) {
                                        existing = it;
                                        break;
                                    }
                                }

                                if (existing == null) {
                                    merged.add(ui);
                                } else {
                                    String name = ui.getParticipantName();
                                    if (name == null || name.trim().isEmpty() || "Khách hàng".equals(name)) {
                                        name = existing.getParticipantName();
                                    }

                                    String avatar = ui.getParticipantAvatarUrl();
                                    if (avatar == null || avatar.trim().isEmpty()) {
                                        avatar = existing.getParticipantAvatarUrl();
                                    }

                                    String lastMessage = ui.getLastMessage();
                                    if (lastMessage == null || "[Chưa có tin nhắn]".equals(lastMessage)) {
                                        lastMessage = existing.getLastMessage();
                                    }

                                    long lastTs = ui.getLastMessageTimestamp() == 0 ? existing.getLastMessageTimestamp() : ui.getLastMessageTimestamp();

                                    int unread = ui.getUnreadCount() == 0 ? existing.getUnreadCount() : ui.getUnreadCount();

                                    ConversationUiData mergedItem = new ConversationUiData(
                                            ui.getConversationId(),
                                            ui.getCustomerId(),
                                            name,
                                            avatar,
                                            lastMessage,
                                            lastTs,
                                            unread,
                                            ui.isLastMessageFromAdmin(),
                                            ui.isLastMessageRead()
                                    );
                                    merged.add(mergedItem);
                                }
                            }
                            Log.d("AdminChatViewModel", "Search success, merged size: " + merged.size());
                            _conversationList.postValue(merged);
                        } else {
                            Log.d("AdminChatViewModel", "Search success, UI data size: " + mapped.size());
                            _conversationList.postValue(mapped);
                        }
                    } else {
                        Log.d("AdminChatViewModel", "Search returned empty results");
                        _conversationList.postValue(new ArrayList<>());
                    }
                } else {
                    Log.e("AdminChatViewModel", "Search failed: " + response.message() + ", code: " + response.code());
                    _errorMessage.postValue("Lỗi khi tìm kiếm: " + response.message());
                }
                _isLoading.postValue(false);
            }

            @Override
            public void onFailure(Call<SpringPage<ConversationSummaryDto>> call, Throwable t) {
                Log.e("AdminChatViewModel", "Search failure: " + t.getMessage(), t);
                _errorMessage.postValue("Lỗi mạng: " + t.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    /**
     * Lắng nghe các cập nhật real-time
     */
    public LiveData<ConversationSummaryDto> getUpdateListener() {
        if (updateListenerLiveData == null) {
            updateListenerLiveData = chatRepository.getAdminUpdatesListener(currentAdminId);
        }
        return updateListenerLiveData;
    }

    /**
     * Xử lý một cập nhật real-time từ Firebase
     */
    public void processSingleUpdate(ConversationSummaryDto updatedDto) {
        Log.d("AdminChatViewModel", "Processing real-time update for conversationId: " + updatedDto.getConversationId());
        
        List<ConversationUiData> currentList = _conversationList.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        } else {
            currentList = new ArrayList<>(currentList); // Tạo list mới
        }

        // GỌI HÀM SỐ ÍT
        ConversationUiData updatedUiData = mapDtoToUiData(updatedDto);
        Log.d("AdminChatViewModel", "Mapped UI data - conversationId: " + updatedUiData.getConversationId() 
                + ", customerName: " + updatedUiData.getParticipantName() 
                + ", unreadCount: " + updatedUiData.getUnreadCount()
                + ", lastMessage: " + updatedUiData.getLastMessage());

        // Tìm và xóa item cũ
        int foundIndex = -1;
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).getConversationId().equals(updatedUiData.getConversationId())) {
                foundIndex = i;
                break;
            }
        }
        if (foundIndex != -1) {
            currentList.remove(foundIndex);
            Log.d("AdminChatViewModel", "Removed old item at index: " + foundIndex);
        } else {
            Log.d("AdminChatViewModel", "Item not found in current list, adding new");
        }

        // Thêm item mới lên đầu
        currentList.add(0, updatedUiData);
        _conversationList.postValue(currentList);
        Log.d("AdminChatViewModel", "Updated list size: " + currentList.size());
    }

    // --- CÁC HÀM MAP (RẤT QUAN TRỌNG) ---

    /**
     * HÀM SỬA LỖI (SỐ NHIỀU)
     * Biến đổi một DANH SÁCH DTO (từ API) thành DANH SÁCH UI.
     * Hàm này chỉ gọi hàm (số ít) bên dưới.
     */
    private List<ConversationUiData> mapDtosToUiData(List<ConversationSummaryDto> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }
        List<ConversationUiData> uiList = new ArrayList<>();
        for (ConversationSummaryDto dto : dtos) {
            // Đảm bảo gọi hàm map (số ít)
            uiList.add(mapDtoToUiData(dto));
        }
        return uiList;
    }

    /**
     * HÀM SỐ ÍT (Helper)
     * Biến đổi MỘT DTO thành MỘT UI Data.
     */
    private ConversationUiData mapDtoToUiData(ConversationSummaryDto dto) {
        MessageDto lastMessage = dto.getLastMessage();
        String lastMessageContent = "[Chưa có tin nhắn]";
        long lastMessageTimestamp = dto.getLastMessageAt() != null ? dto.getLastMessageAt() : 0;
        boolean isLastMessageFromAdmin = false;
        boolean isLastMessageRead = true;

        if (lastMessage != null) {
            lastMessageContent = (lastMessage.getMessageType() == MessageDto.MessageType.IMAGE)
                    ? "[Hình ảnh]"
                    : lastMessage.getContent();
            isLastMessageFromAdmin = Objects.equals(lastMessage.getSenderId(), currentAdminId);
            isLastMessageRead = (lastMessage.getReadAt() != null);
        }

        // Xử lý customerName - đảm bảo không null
        String customerName = dto.getCustomerName();
        if (customerName == null || customerName.trim().isEmpty()) {
            customerName = "Khách hàng"; // Fallback nếu không có tên
            Log.w("AdminChatViewModel", "Warning: customerName is null or empty for conversationId: " + dto.getConversationId());
        } else {
            Log.d("AdminChatViewModel", "Customer name for conversationId " + dto.getConversationId() + ": " + customerName);
        }

        // DTO của bạn có "customerAvatarUrl" không? (Dựa trên log JSON thì không)
        // Nếu có, hãy thay "null" bằng dto.getCustomerAvatarUrl()
        return new ConversationUiData(
                dto.getConversationId(),
                dto.getCustomerId(),
                customerName, // Đảm bảo không null
                dto.getCustomerAvatarUrl(), // Có thể là null
                lastMessageContent,
                lastMessageTimestamp,
                dto.getUnreadCount() != null ? dto.getUnreadCount() : 0,
                isLastMessageFromAdmin,
                isLastMessageRead
        );
    }
}