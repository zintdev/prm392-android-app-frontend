package com.example.prm392_android_app_frontend.presentation.fragment.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.repository.ChatRepository;
import com.example.prm392_android_app_frontend.presentation.activity.MainActivity;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.example.prm392_android_app_frontend.presentation.activity.LoginActivity;
import com.example.prm392_android_app_frontend.presentation.activity.ProfileActivity;
// THÊM IMPORT CHO CHAT ACTIVITY
import com.example.prm392_android_app_frontend.presentation.activity.ChatActivity;
import com.google.android.material.snackbar.Snackbar;

public class AccountFragment extends Fragment {

    private TextView txtName, txtFPoint, txtFreeship;
    private View rowProfile, rowLogout;
    private View rowChat; // Biến này bây giờ là Button
    private ChatRepository chatRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        txtName = v.findViewById(R.id.txtName);
        txtFPoint = v.findViewById(R.id.txtFPoint);
        txtFreeship = v.findViewById(R.id.txtFreeship);
        rowProfile = v.findViewById(R.id.rowProfile);
        rowLogout = v.findViewById(R.id.rowLogout);

        chatRepository = new ChatRepository(requireActivity().getApplication());

        // SỬA Ở ĐÂY: ID của Button là "btn_chat_with_admin"
        rowChat = v.findViewById(R.id.btn_chat_with_admin);

        String username = TokenStore.getUsername(requireContext());
        txtName.setText(username);

        // Setup row Profile
        ((TextView) rowProfile.findViewById(R.id.title)).setText("Hồ sơ cá nhân");
        ((ImageView) rowProfile.findViewById(R.id.icon)).setImageResource(R.drawable.ic_person);

        // --- XÓA 2 DÒNG GÂY CRASH ---
        // rowChat (là Button) không có title hay icon bên trong nó
        // ((TextView) rowChat.findViewById(R.id.title)).setText("Hỗ trợ (Chat)");
        // ((ImageView) rowChat.findViewById(R.id.icon)).setImageResource(R.drawable.ic_chat);
        // ----------------------------------

        // Setup row Logout
        ((TextView) rowLogout.findViewById(R.id.title)).setText("Đăng xuất");
        ((ImageView) rowLogout.findViewById(R.id.icon)).setImageResource(R.drawable.ic_logout);


        // Click: mở trang cập nhật hồ sơ
        rowProfile.setOnClickListener(v12 -> {
            if (!TokenStore.isLoggedIn(requireContext())) {
                Snackbar.make(v12, "Bạn cần đăng nhập", Snackbar.LENGTH_SHORT).show();
                startActivity(new Intent(requireContext(), LoginActivity.class));
                return;
            }
            Intent i = new Intent(requireContext(), ProfileActivity.class);
            i.putExtra("userId", TokenStore.getUserId(requireContext()));
            startActivity(i);
        });

        // --- LOGIC CLICK CHO ROW CHAT (Đã đúng) ---
        rowChat.setOnClickListener(v_chat -> {
            // 1. Kiểm tra đăng nhập (Đã đúng)
            if (!TokenStore.isLoggedIn(requireContext())) {
                Snackbar.make(v_chat, "Bạn cần đăng nhập để chat", Snackbar.LENGTH_SHORT).show();
                startActivity(new Intent(requireContext(), LoginActivity.class));
                return;
            }

            // 2. Lấy ID từ TokenStore
            int currentUserId = TokenStore.getUserId(requireContext());
            int adminReceiverId = 1; // Admin ID cố định (chỉ có 1 admin duy nhất)
            
            // Log để debug
            android.util.Log.d("AccountFragment", "Current User ID: " + currentUserId + ", Admin ID: " + adminReceiverId);

            // 3. Kiểm tra admin tự chat (Đã đúng)
            if (currentUserId == adminReceiverId) {
                Snackbar.make(v_chat, "Admin không thể tự chat với chính mình", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // 4. GỌI API ĐỂ LẤY HOẶC TẠO CONVERSATION ID (LOGIC MỚI)
            Snackbar.make(v_chat, "Đang mở cuộc hội thoại...", Snackbar.LENGTH_SHORT).show();
            // (Hiện loading...)

            // Gọi hàm mới trong repository (không cần adminReceiverId vì backend tự động tìm conversation với admin)
            chatRepository.findOrCreateConversation(null).observe(getViewLifecycleOwner(), conversationDto -> {
                // (Ẩn loading...)

                // 5. Kiểm tra kết quả
                if (conversationDto != null && conversationDto.getConversationId() != null) {
                    // 6. Lấy được ID -> Mở ChatActivity
                    Intent chatIntent = new Intent(requireContext(), ChatActivity.class);

                    // Tìm admin ID từ participantIds (không phải currentUserId)
                    int actualAdminId = adminReceiverId; // Fallback
                    if (conversationDto.getParticipantIds() != null) {
                        for (Integer participantId : conversationDto.getParticipantIds()) {
                            if (participantId != currentUserId) {
                                actualAdminId = participantId;
                                break;
                            }
                        }
                    }

                    // GỬI DỮ LIỆU ĐÚNG (KHÔNG CÒN HARDCODE)
                    chatIntent.putExtra("RECEIVER_ID", actualAdminId); // Người nhận là Admin (từ API)
                    chatIntent.putExtra("CONVERSATION_ID", conversationDto.getConversationId()); // ID động từ API

                    // 7. Mở ChatActivity
                    startActivity(chatIntent);
                    
                    // Hiển thị thông báo thành công
                    Snackbar.make(v_chat, "Đã mở cuộc hội thoại hỗ trợ", Snackbar.LENGTH_SHORT).show();
                } else {
                    // 8. Xử lý lỗi (trường hợp này không nên xảy ra vì có fallback)
                    Snackbar.make(v_chat, "Không thể mở cuộc hội thoại. Vui lòng thử lại.", Snackbar.LENGTH_LONG).show();
                }
            });
        });
        // --------------------------------------

        // Click: đăng xuất
        rowLogout.setOnClickListener(v1 -> {
            TokenStore.clear(requireContext());
            Snackbar.make(v1, "Đã đăng xuất", Snackbar.LENGTH_SHORT).show();
            // Khởi động lại MainActivity để làm mới trạng thái (đã đăng xuất)
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish(); // Đóng Activity cũ
        });

        // (Code order cũ của bạn)
//        v.findViewById(R.id.btnOrderWaitingPay).setOnClickListener(x ->
//                Snackbar.make(x, "Chờ thanh toán", Snackbar.LENGTH_SHORT).show());
//        v.findViewById(R.id.btnOrderProcessing).setOnClickListener(x ->
//                Snackbar.make(x, "Đang xử lý", Snackbar.LENGTH_SHORT).show());
////        v.findViewById(R.id.btnOrderShipping).setOnClickListener(x ->
////                Snackbar.make(x, "Đang giao hàng", Snackbar.LENGTH_SHORT).show());
//        v.findViewById(R.id.btnOrderCompleted).setOnClickListener(x ->
//                Snackbar.make(x, "Hoàn tất", Snackbar.LENGTH_SHORT).show());
    }
}