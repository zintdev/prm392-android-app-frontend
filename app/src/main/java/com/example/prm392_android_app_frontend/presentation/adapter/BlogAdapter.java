package com.example.prm392_android_app_frontend.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.presentation.fragment.user.BlogDetailFragment;

import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {

    public interface OnItemClick { void onClick(BlogDto item); }

    private List<BlogDto> blogDtos;
    private final OnItemClick onItemClick;

    public BlogAdapter(List<BlogDto> blogDtos, OnItemClick onItemClick) {
        this.blogDtos = blogDtos;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.blog_item, parent, false);
        return new BlogViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        BlogDto item = blogDtos.get(position);
        holder.title.setText(item.getTitle());
        holder.author.setText(item.getAuthor());
        holder.date.setText(item.getDate());

        // Load ảnh bằng Glide
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.blog_placeholder)
                            .error(R.drawable.blog_placeholder)
                            .centerCrop())
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.blog_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return blogDtos != null ? blogDtos.size() : 0;
    }

    public void updateData(List<BlogDto> newBlogDtos) {
        this.blogDtos = newBlogDtos;
        notifyDataSetChanged();
    }

    static class BlogViewHolder extends RecyclerView.ViewHolder {
        TextView title, author, date;
        ImageView image;

        BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title_text);
            author = itemView.findViewById(R.id.author_text);
            date = itemView.findViewById(R.id.date_text);
            image = itemView.findViewById(R.id.blog_image);
        }
    }
}
