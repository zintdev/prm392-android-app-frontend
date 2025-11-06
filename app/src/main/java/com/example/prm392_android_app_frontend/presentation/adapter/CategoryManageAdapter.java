package com.example.prm392_android_app_frontend.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.CategoryDto;

import java.util.ArrayList;
import java.util.List;

public class CategoryManageAdapter extends RecyclerView.Adapter<CategoryManageAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(CategoryDto category);
        void onDelete(CategoryDto category);
    }

    private final List<CategoryDto> items = new ArrayList<>();
    private final Listener listener;

    public CategoryManageAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<CategoryDto> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CategoryDto it = items.get(position);
        TextView name = h.itemView.findViewById(R.id.category_name); // id in layout
        Button btnEdit = h.itemView.findViewById(R.id.button_edit_category);
        Button btnDelete = h.itemView.findViewById(R.id.button_delete_category);

        name.setText(it.getName());

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


