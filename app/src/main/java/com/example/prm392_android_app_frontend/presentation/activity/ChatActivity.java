package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
// Xóa SharedPreferences, chúng ta sẽ dùng TokenStore
// import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;

import com.example.prm392_android_app_frontend.presentation.viewmodel.ChatViewModel;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.presentation.adapter.ChatAdapter;
import com.example.prm392_android_app_frontend.storage.TokenStore; // <-- THÊM IMPORT TOKENSTORE

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private EditText etMessage;
    private ImageButton btnSend, btnAttach;
    private Toolbar toolbar;

    private ChatViewModel viewModel;

    // Thông tin chat (sẽ được lấy từ Intent/TokenStore)
    private Integer currentUserId;
    private Integer receiverId;
    private Integer conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // --- BẮT ĐẦU SỬA: Lấy dữ liệu thật ---

        // 1. Lấy currentUserId từ TokenStore (Cho nhất quán với AccountFragment)
        currentUserId = TokenStore.getUserId(this);

        // 2. Lấy receiverId và conversationId từ Intent
        Intent intent = getIntent();
        receiverId = intent.getIntExtra("RECEIVER_ID", -1);
        conversationId = intent.getIntExtra("CONVERSATION_ID", -1);

        // 3. Kiểm tra dữ liệu
        // (TokenStore.getUserId trả về 0 nếu không tìm thấy, nên ta kiểm tra <= 0)
        if (currentUserId <= 0 || receiverId == -1 || conversationId == -1) {
            Toast.makeText(this, "Lỗi: Không đủ thông tin để bắt đầu chat.", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity nếu thiếu thông tin
            return; // Dừng hàm onCreate
        }
        // --- KẾT THÚC SỬA ---


        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // 2. Thiết lập UI (Hàm này giờ sẽ dùng ID thật)
        setupUI();

        // 3. Khởi tạo ViewModel (Giờ đã dùng ID thật)
        viewModel.init(conversationId, currentUserId, receiverId);

        // 4. Lắng nghe (Observe) LiveData từ ViewModel
        setupObservers();

        // 5. Thiết lập listeners cho UI
        setupClickListeners();
    }

    private void setupUI() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Sửa: Dùng receiverId thật
        toolbar.setTitle("Hỗ trợ (Admin)"); // Hiển thị "Hỗ trợ" thay vì ID

        // Thêm nút back để quay về profile
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        recyclerView = findViewById(R.id.recycler_view_messages);
        etMessage = findViewById(R.id.edit_text_message);
        btnSend = findViewById(R.id.button_send);
        btnAttach = findViewById(R.id.button_attach_image);

        // Sửa: Dùng currentUserId thật
        adapter = new ChatAdapter(this, new ArrayList<>(), currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // Đổi màu nút back thành trắng (sau khi toolbar đã được setup)
        toolbar.post(() -> {
            Drawable upArrow = toolbar.getNavigationIcon();
            if (upArrow != null) {
                upArrow = DrawableCompat.wrap(upArrow.mutate());
                DrawableCompat.setTint(upArrow, ContextCompat.getColor(this, android.R.color.white));
                toolbar.setNavigationIcon(upArrow);
            }
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupObservers() {
        // 4.1. Quan sát LỊCH SỬ chat
        viewModel.getHistoryMessages().observe(this, historyList -> {
            if (historyList != null) {
                adapter.setMessageList(historyList);
                if (!historyList.isEmpty()) {
                    recyclerView.scrollToPosition(historyList.size() - 1);
                    // Đánh dấu TẤT CẢ tin nhắn chưa đọc (của người khác) là đã đọc khi vào chat
                    // Lặp từ cuối lên để đánh dấu tin nhắn mới nhất trước
                    for (int i = historyList.size() - 1; i >= 0; i--) {
                        viewModel.notifyMessageRead(historyList.get(i));
                    }
                }
            }
        });

        // 4.2. Quan sát TIN NHẮN MỚI
        viewModel.getNewMessage().observe(this, message -> {
            if (message != null) {
                adapter.addMessage(message);
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                viewModel.notifyMessageRead(message);
            }
        });

        // 4.3. Quan sát TRẠNG THÁI TYPING
        viewModel.getTypingStatus().observe(this, status -> {
            if (status != null && !status.isEmpty()) {
                toolbar.setSubtitle(status);
            } else {
                toolbar.setSubtitle("online");
            }
        });

        // 4.4. Quan sát SỰ KIỆN ĐÃ ĐỌC
        viewModel.getReadReceipt().observe(this, readMessage -> {
            if (readMessage != null) {
                adapter.updateMessageReadStatus(readMessage);
            }
        });
        
        // 4.5. Quan sát TRẠNG THÁI UPLOAD ẢNH
        viewModel.getIsUploading().observe(this, isUploading -> {
            if (isUploading != null) {
                btnAttach.setEnabled(!isUploading);
                btnAttach.setAlpha(isUploading ? 0.5f : 1.0f);
            }
        });
        
        // 4.6. Quan sát KẾT QUẢ UPLOAD
        viewModel.getUploadSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Đã gửi ảnh", Toast.LENGTH_SHORT).show();
            }
        });
        
        viewModel.getUploadError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupClickListeners() {
        // 5.1. Nút Gửi
        btnSend.setOnClickListener(v -> {
            String messageContent = etMessage.getText().toString().trim();
            if (!messageContent.isEmpty()) {
                viewModel.sendTextMessage(messageContent);
                etMessage.setText("");
            }
        });

        // 5.2. Nút Đính kèm
        btnAttach.setOnClickListener(v -> pickImage());

        // 5.3. Gửi sự kiện Typing
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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
        viewModel.setPresence(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
                viewModel.uploadImage(imageUri);
            }
        }
    }
}