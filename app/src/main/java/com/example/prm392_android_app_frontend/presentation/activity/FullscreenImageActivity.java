package com.example.prm392_android_app_frontend.presentation.activity;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.prm392_android_app_frontend.R;

public class FullscreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        ImageView imageView = findViewById(R.id.fullscreen_image_view);
        String url = getIntent().getStringExtra("IMAGE_URL");
        if (url != null) {
            url = url.replace("http://localhost:", "http://10.0.2.2:")
                     .replace("https://localhost:", "http://10.0.2.2:")
                     .replace("http://127.0.0.1:", "http://10.0.2.2:")
                     .replace("https://127.0.0.1:", "http://10.0.2.2:");
        }
        if (url != null && !url.isEmpty()) {
            Glide.with(this).load(url).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_placeholder);
        }
        imageView.setOnClickListener(v -> finish());
    }
}


