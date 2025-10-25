package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.presentation.viewmodel.ChatViewModel;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.presentation.adapter.ChatAdapter;
import com.example.prm392_android_app_frontend.data.dto.MessageDto;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private EditText etMessage;
    private ImageButton btnSend, btnAttach;
    private Toolbar toolbar;

    private ChatViewModel viewModel;

    // Thông tin chat
    private Integer currentUserId = 1; // TODO: Lấy từ SharedPreferences
    private Integer receiverId = 2; // TODO: Lấy từ Intent
    private Integer conversationId = 1; // TODO: Lấy từ Intent hoặc API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // TODO: Lấy currentUserId, receiverId, conversationId từ SharedPreferences/Intent

        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // 2. Thiết lập UI
        setupUI();

        // 3. Khởi tạo ViewModel (chỉ 1 lần)
        viewModel.init(conversationId, currentUserId, receiverId);

        // 4. Lắng nghe (Observe) LiveData từ ViewModel
        setupObservers();

        // 5. Thiết lập listeners cho UI
        setupClickListeners();
    }

    private void setupUI() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Chat với User " + receiverId);

        recyclerView = findViewById(R.id.recycler_view_messages);
        etMessage = findViewById(R.id.edit_text_message);
        btnSend = findViewById(R.id.button_send);
        btnAttach = findViewById(R.id.button_attach_image);

        adapter = new ChatAdapter(this, new ArrayList<>(), currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        // 4.1. Quan sát LỊCH SỬ chat
        viewModel.getHistoryMessages().observe(this, historyList -> {
            if (historyList != null && !historyList.isEmpty()) {
                adapter.setMessageList(historyList); // Giả sử adapter có hàm setMessageList
                recyclerView.scrollToPosition(historyList.size() - 1);

                // Gửi "đã đọc" cho tin nhắn cuối cùng (nếu cần)
                viewModel.notifyMessageRead(historyList.get(historyList.size() - 1));
            }
        });

        // 4.2. Quan sát TIN NHẮN MỚI
        viewModel.getNewMessage().observe(this, message -> {
            if (message != null) {
                adapter.addMessage(message);
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);

                // Gửi "đã đọc"
                viewModel.notifyMessageRead(message);
            }
        });

        // 4.3. Quan sát TRẠNG THÁI TYPING
        viewModel.getTypingStatus().observe(this, status -> {
            if (status != null && !status.isEmpty()) {
                toolbar.setSubtitle(status);
            } else {
                toolbar.setSubtitle("online"); // (Hoặc rỗng)
            }
        });

        // 4.4. Quan sát SỰ KIỆN ĐÃ ĐỌC
        viewModel.getReadReceipt().observe(this, readMessage -> {
            if (readMessage != null) {
                adapter.updateMessageReadStatus(readMessage);
            }
        });
    }

    private void setupClickListeners() {
        // 5.1. Nút Gửi
        btnSend.setOnClickListener(v -> {
            String messageContent = etMessage.getText().toString();
            viewModel.sendTextMessage(messageContent);
            etMessage.setText(""); // Xóa input
        });

        // 5.2. Nút Đính kèm
        btnAttach.setOnClickListener(v -> pickImage());

        // 5.3. Gửi sự kiện Typing
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Báo cho ViewModel biết user đang gõ
                viewModel.notifyTyping(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // --- Xử lý Lifecycle (Presence) ---

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.setPresence(true); // Báo là tôi Online
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Không set offline ở đây, vì có thể chỉ là chuyển app tạm thời
        // ViewModel sẽ xử lý offline khi Activity bị destroy (trong onCleared)
        // Hoặc Firebase onDisconnect sẽ tự xử lý
    }



    // --- Xử lý Chọn ảnh ---

    private void pickImage() {
        ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                // Gửi Uri cho ViewModel xử lý
                viewModel.uploadImage(imageUri);
            }
        }
    }
}