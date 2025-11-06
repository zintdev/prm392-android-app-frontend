package com.example.prm392_android_app_frontend.presentation.fragment.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.presentation.activity.ChatActivity;

/**
 * Simple wrapper that lets staff jump into the existing ChatActivity.
 */
public class StaffChatFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button btnOpenChat = view.findViewById(R.id.btnOpenChat);
        if (btnOpenChat != null) {
            btnOpenChat.setOnClickListener(v -> openChat());
        }
    }

    private void openChat() {
        if (getContext() == null) {
            return;
        }
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        startActivity(intent);
    }
}
