package com.example.prm392_android_app_frontend.presentation.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.domain.model.chat.ConversationUiData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ConversationListAdapter extends ListAdapter<ConversationUiData, ConversationListAdapter.ConversationViewHolder> {

    private final OnConversationClickListener clickListener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private Context context; // Thêm context để dùng Glide

    public interface OnConversationClickListener {
        void onConversationClick(ConversationUiData conversation);
    }

    public ConversationListAdapter(@NonNull DiffUtil.ItemCallback<ConversationUiData> diffCallback, OnConversationClickListener listener) {
        super(diffCallback);
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Lưu lại context khi ViewHolder được tạo
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        ConversationUiData conversation = getItem(position);
        holder.bind(conversation, clickListener);
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivAvatar;
        private final TextView tvName;
        private final TextView tvLastMessage;
        private final TextView tvTime;
        private final TextView tvUnreadCount;
        private final View unreadBadgeContainer; // Tham chiếu đến FrameLayout

        ConversationViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_customer_avatar);
            tvName = itemView.findViewById(R.id.tv_customer_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime = itemView.findViewById(R.id.tv_last_message_time);
            tvUnreadCount = itemView.findViewById(R.id.tv_unread_count);
            unreadBadgeContainer = itemView.findViewById(R.id.unread_badge_container); // Lấy FrameLayout
        }

        void bind(ConversationUiData conversation, OnConversationClickListener listener) {
            tvName.setText(conversation.getParticipantName());

            // Xử lý tin nhắn cuối
            String lastMessagePrefix = conversation.isLastMessageFromAdmin() ? "Bạn: " : "";
            tvLastMessage.setText(lastMessagePrefix + conversation.getLastMessage());

            // Xử lý thời gian
            tvTime.setText(timeFormat.format(new Date(conversation.getLastMessageTimestamp())));

            // Xử lý trạng thái chưa đọc
            if (conversation.getUnreadCount() > 0) {
                unreadBadgeContainer.setVisibility(View.VISIBLE);
                tvUnreadCount.setText(String.valueOf(conversation.getUnreadCount()));

                // In đậm nếu chưa đọc
                tvName.setTypeface(null, Typeface.BOLD);
                tvLastMessage.setTypeface(null, Typeface.BOLD);
                tvLastMessage.setTextColor(context.getResources().getColor(android.R.color.black)); // Màu đậm
            } else {
                unreadBadgeContainer.setVisibility(View.GONE);

                // Trở lại bình thường nếu đã đọc
                tvName.setTypeface(null, Typeface.NORMAL);
                tvLastMessage.setTypeface(null, Typeface.NORMAL);
                tvLastMessage.setTextColor(context.getResources().getColor(R.color.gray)); // Màu xám (bạn cần định nghĩa màu này)
            }

            // Tải Avatar
            Glide.with(context)
                    .load(conversation.getParticipantAvatarUrl())
                    .placeholder(R.drawable.ic_music_cd) // Ảnh placeholder
                    .error(R.drawable.ic_music_cd)     // Ảnh khi lỗi
                    .into(ivAvatar);

            // Bắt sự kiện click
            itemView.setOnClickListener(v -> listener.onConversationClick(conversation));
        }
    }

    // Lớp DiffUtil để RecyclerView cập nhật hiệu quả
    public static class ConversationDiff extends DiffUtil.ItemCallback<ConversationUiData> {
        @Override
        public boolean areItemsTheSame(@NonNull ConversationUiData oldItem, @NonNull ConversationUiData newItem) {
            return oldItem.getConversationId().equals(newItem.getConversationId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ConversationUiData oldItem, @NonNull ConversationUiData newItem) {
            // So sánh các trường quan trọng để biết item có thay đổi nội dung không
            return oldItem.getLastMessage().equals(newItem.getLastMessage()) &&
                    oldItem.getUnreadCount() == newItem.getUnreadCount() &&
                    oldItem.getLastMessageTimestamp() == newItem.getLastMessageTimestamp();
        }
    }
}