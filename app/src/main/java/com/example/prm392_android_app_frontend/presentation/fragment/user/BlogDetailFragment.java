package com.example.prm392_android_app_frontend.presentation.fragment.user;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.BlogDto;

public class BlogDetailFragment extends Fragment {

    private static final String ARG_BLOG = "arg_blog";

    public static BlogDetailFragment newInstance(BlogDto blog) {
        BlogDetailFragment f = new BlogDetailFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_BLOG, blog);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_blog_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        BlogDto blog = (BlogDto) getArguments().getSerializable(ARG_BLOG);
        if (blog == null) return;

        TextView title = v.findViewById(R.id.tvTitle);
        TextView meta = v.findViewById(R.id.tvMeta);
        TextView body = v.findViewById(R.id.tvBody);
        ImageView img = v.findViewById(R.id.imgCover);

        title.setText(blog.getTitle());
        meta.setText(blog.getAuthor() + " • " + blog.getDate());

        String content = blog.getContent() != null && !blog.getContent().isEmpty()
                ? blog.getContent()
                : blog.getSummary();

        body.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));

        if (blog.getImageUrl() != null && !blog.getImageUrl().isEmpty()) {
            Glide.with(this).load(blog.getImageUrl()).into(img);
        } else {
            img.setImageResource(R.drawable.blog_placeholder);
        }

        v.findViewById(R.id.btnBack).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack());
    }
}
