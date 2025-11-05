package com.example.prm392_android_app_frontend.presentation.fragment.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.model.Notification;
import com.example.prm392_android_app_frontend.data.repository.NotificationStore;
import com.example.prm392_android_app_frontend.presentation.adapter.NotificationAdapter;

import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private NotificationStore notificationStore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_notifications);
        adapter = new NotificationAdapter();
        notificationStore = new NotificationStore(requireContext());

        recyclerView.setAdapter(adapter);
        // Thêm dòng kẻ phân cách giữa các item
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications();
        // Khi người dùng vào xem, đánh dấu tất cả là đã đọc
        notificationStore.markAllAsRead();
        // TODO: Cập nhật badge trên navbar về 0
    }

    private void loadNotifications() {
        List<Notification> notifications = notificationStore.getNotifications();
        adapter.setNotifications(notifications);
    }
}
