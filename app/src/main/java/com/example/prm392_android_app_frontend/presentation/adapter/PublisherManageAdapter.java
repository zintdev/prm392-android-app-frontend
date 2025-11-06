package com.example.prm392_android_app_frontend.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.PublisherDto;

import java.util.ArrayList;
import java.util.List;

public class PublisherManageAdapter extends RecyclerView.Adapter<PublisherManageAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(PublisherDto publisher);
        void onDelete(PublisherDto publisher);
    }

    private final List<PublisherDto> items = new ArrayList<>();
    private final Listener listener;

    public PublisherManageAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<PublisherDto> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_publisher, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        PublisherDto it = items.get(position);
        TextView name = h.itemView.findViewById(R.id.publisher_name);
        TextView year = h.itemView.findViewById(R.id.publisher_founded_year);
        Button btnEdit = h.itemView.findViewById(R.id.button_edit_publisher);
        Button btnDelete = h.itemView.findViewById(R.id.button_delete_publisher);

        name.setText(it.getName());
        if (it.getFoundedYear() != null) {
            year.setText("Năm thành lập: " + it.getFoundedYear());
            year.setVisibility(View.VISIBLE);
        } else {
            year.setText("");
            year.setVisibility(View.GONE);
        }
        btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(it);
        });

        btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(it); // 👈 thêm dòng này
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View itemView) { super(itemView); }
    }
}


