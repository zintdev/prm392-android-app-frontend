package com.example.prm392_android_app_frontend.presentation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.MessageDto; // (Import model DTO của bạn)

import java.util.List;
import java.util.Objects;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<MessageDto> messageList;
    private Context context;
    private Integer currentUserId; // ID của user hiện tại

    public ChatAdapter(Context context, List<MessageDto> messageList, Integer currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        MessageDto message = messageList.get(position);
        // Dùng Objects.equals để so sánh Integer (hoặc Long) an toàn
        if (Objects.equals(message.getSenderId(), currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageDto message = messageList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageHolder) holder).bind(message);
        } else {
            ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // --- Các phương thức public cho ViewModel ---

    /**
     * Dùng để load lịch sử chat (từ ViewModel)
     */
    public void setMessageList(List<MessageDto> list) {
        this.messageList.clear();
        this.messageList.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * Dùng để thêm tin nhắn mới (từ Firebase listener)
     */
    public void addMessage(MessageDto message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    /**
     * Dùng để cập nhật trạng thái "đã đọc" (từ Firebase listener)
     */
    public void updateMessageReadStatus(MessageDto readMessage) {
        for (int i = messageList.size() - 1; i >= 0; i--) { // Duyệt ngược để tìm tin mới nhất
            MessageDto item = messageList.get(i);
            // Dùng Objects.equals để so sánh ID
            if (Objects.equals(item.getId(), readMessage.getId())) {
                if(item.getReadAt() == null) { // Chỉ cập nhật nếu trạng thái thay đổi
                    item.setReadAt(readMessage.getReadAt());
                    notifyItemChanged(i);
                }
                return; // Giả sử ID là duy nhất, tìm thấy thì thoát
            }
        }
    }


    // --- ViewHolders ---

    /**
     * ViewHolder cho tin nhắn GỬI (của mình)
     */
    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, readStatusText;
        ImageView messageImage;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_view_message_content);
            messageImage = itemView.findViewById(R.id.image_view_message);
            readStatusText = itemView.findViewById(R.id.text_view_read_status);
        }

        void bind(MessageDto message) {
            if (message.getMessageType() == MessageDto.MessageType.TEXT) {
                messageText.setText(message.getContent());
                messageText.setVisibility(View.VISIBLE);
                messageImage.setVisibility(View.GONE);
            } else { // IMAGE
                messageText.setVisibility(View.GONE);
                messageImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(message.getContent()) // Tải URL ảnh
                        .into(messageImage);
            }

            // Cập nhật trạng thái "Đã gửi" / "Đã xem"
            if (message.getReadAt() != null) {
                readStatusText.setText("Đã xem");
            } else {
                readStatusText.setText("Đã gửi");
            }
            readStatusText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ViewHolder cho tin nhắn NHẬN (của người khác)
     */
    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, senderNameText;
        ImageView messageImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_view_message_content);
            messageImage = itemView.findViewById(R.id.image_view_message);
            senderNameText = itemView.findViewById(R.id.text_view_sender_name);
        }

        void bind(MessageDto message) {
            // TODO: Lấy tên sender từ senderId (nếu là chat nhóm)
            // senderNameText.setText("Sender " + message.getSenderId());
            // senderNameText.setVisibility(View.VISIBLE); // Chỉ hiện khi là chat nhóm

            if (message.getMessageType() == MessageDto.MessageType.TEXT) {
                messageText.setText(message.getContent());
                messageText.setVisibility(View.VISIBLE);
                messageImage.setVisibility(View.GONE);
            } else { // IMAGE
                messageText.setVisibility(View.GONE);
                messageImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(message.getContent()) // Tải URL ảnh
                        .into(messageImage);
            }
        }
    }
}