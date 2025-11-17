package com.example.prm392_android_app_frontend.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.store.StoreLocationResponse;

import java.util.ArrayList;
import java.util.List;

public class StoreAssignAdapter extends RecyclerView.Adapter<StoreAssignAdapter.StoreViewHolder> {

    public interface OnStoreClickListener {
        void onStoreClick(StoreLocationResponse store);
    }

    private final List<StoreLocationResponse> data = new ArrayList<>();
    private final OnStoreClickListener listener;

    public StoreAssignAdapter(OnStoreClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store_select, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        holder.bind(data.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void submit(List<StoreLocationResponse> stores) {
        data.clear();
        if (stores != null) {
            data.addAll(stores);
        }
        notifyDataSetChanged();
    }

    static class StoreViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtName;
        private final TextView txtAddress;

        StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtStoreName);
            txtAddress = itemView.findViewById(R.id.txtStoreAddress);
        }

        void bind(StoreLocationResponse store, OnStoreClickListener listener) {
            txtName.setText(store.getStoreName() != null ? store.getStoreName() : "");
            String address = store.getAddress();
            txtAddress.setText(address != null ? address : "");
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStoreClick(store);
                }
            });
        }
    }
}
