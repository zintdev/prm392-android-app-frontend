package com.example.prm392_android_app_frontend.features.blog.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.prm392_android_app_frontend.features.blog.ui.BlogDetailFragment;
import com.example.prm392_android_app_frontend.features.blog.data.dto.BlogDto;
import com.example.prm392_android_app_frontend.R;

import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {

    private List<BlogDto> blogDtos;

    public BlogAdapter(List<BlogDto> blogDtos) {
        this.blogDtos = blogDtos;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.blog_item, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        BlogDto item = blogDtos.get(position);
        holder.titleText.setText(item.getTitle());
        holder.dateText.setText(item.getDate());
        holder.authorText.setText(item.getAuthor());

        // Load blog image using Glide
        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.blog_placeholder)
                            .error(R.drawable.blog_placeholder)
                            .centerCrop())
                    .into(holder.blogImage);
        } else {
            // Set default placeholder image
            holder.blogImage.setImageResource(R.drawable.blog_placeholder);
        }

        // Set click listener to navigate to blog detail
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, BlogDetailFragment.class);
            intent.putExtra(BlogDetailFragment.EXTRA_BLOG_ITEM, item);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return blogDtos.size();
    }

    public void updateData(List<BlogDto> newBlogDtos) {
        this.blogDtos = newBlogDtos;
        notifyDataSetChanged();
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView dateText;
        TextView authorText;
        ImageView blogImage;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.title_text);
            dateText = itemView.findViewById(R.id.date_text);
            authorText = itemView.findViewById(R.id.author_text);
            blogImage = itemView.findViewById(R.id.blog_image);
        }
    }
}