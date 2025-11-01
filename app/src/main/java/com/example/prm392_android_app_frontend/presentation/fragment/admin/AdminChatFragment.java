package com.example.prm392_android_app_frontend.presentation.fragment.admin; // (Package của bạn)

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

// (Import các lớp cần thiết)
import com.example.prm392_android_app_frontend.data.domain.model.chat.ConversationUiData;
import com.example.prm392_android_app_frontend.databinding.FragmentAdminChatBinding;
import com.example.prm392_android_app_frontend.presentation.adapter.ConversationListAdapter;
import com.example.prm392_android_app_frontend.presentation.activity.ChatActivity;
import com.example.prm392_android_app_frontend.presentation.viewmodel.AdminChatViewModel; // <-- Import ViewModel

public class AdminChatFragment extends Fragment implements ConversationListAdapter.OnConversationClickListener {

    private FragmentAdminChatBinding binding;
    private ConversationListAdapter conversationAdapter;
    private AdminChatViewModel viewModel; // <-- Thêm ViewModel
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY_MS = 500; // Debounce delay 500ms

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupViewModel(); // <-- Thay thế loadDummyData()
        setupSearchListener();

        // Kích hoạt ViewModel tải dữ liệu
        viewModel.fetchConversations();
    }

    private void setupRecyclerView() {
        conversationAdapter = new ConversationListAdapter(new ConversationListAdapter.ConversationDiff(), this);
        binding.rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvConversations.setAdapter(conversationAdapter);
    }

    private void setupViewModel() {
        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(AdminChatViewModel.class);

        // Lắng nghe danh sách cuộc trò chuyện
        viewModel.conversationList.observe(getViewLifecycleOwner(), conversations -> {

            // --- (2) THÊM LOG DEBUG VÀO ĐÂY ---
            if (conversations != null) {
                Log.d("AdminChatFragment", "Observer FIRED. Submitting list size: " + conversations.size());
                conversationAdapter.submitList(conversations);
            } else {
                Log.e("AdminChatFragment", "Observer FIRED with NULL list!");
            }
            // --- KẾT THÚC THÊM LOG ---

        });

        // Lắng nghe trạng thái loading (Cần thêm ProgressBar vào XML)
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            // binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            // binding.rvConversations.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        });


        // Lắng nghe thông báo lỗi
        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
        // Lắng nghe các cập nhật real-time
        viewModel.getUpdateListener().observe(getViewLifecycleOwner(), updatedDto -> {
            if (updatedDto != null) {
                Log.d("AdminChatFragment", "Real-time update received!");
                viewModel.processSingleUpdate(updatedDto);
            }
        });
    }

    private void setupSearchListener() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Hủy search request trước đó nếu có
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Tạo search request mới với debounce delay
                String query = s.toString();
                searchRunnable = () -> {
                    if (viewModel != null) {
                        viewModel.searchConversations(query);
                    }
                };
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý
            }
        });
    }

    @Override
    public void onConversationClick(ConversationUiData conversation) {
        Toast.makeText(getContext(), "Mở chat ID: " + conversation.getConversationId(), Toast.LENGTH_SHORT).show();

        // Mở ChatActivity giống phía Customer nhưng vai trò Admin
        Intent chatIntent = new Intent(requireContext(), ChatActivity.class);
        chatIntent.putExtra("RECEIVER_ID", conversation.getCustomerId()); // người nhận là customer
        chatIntent.putExtra("CONVERSATION_ID", conversation.getConversationId());
        startActivity(chatIntent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hủy search handler để tránh memory leak
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        binding = null;
    }
}