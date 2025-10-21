package com.example.prm392_android_app_frontend.presentation.fragment.user;

import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.bumptech.glide.Glide;
import com.example.prm392_android_app_frontend.data.dto.BlogDto;
import com.example.prm392_android_app_frontend.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BlogDetailFragment extends AppCompatActivity {

    public static final String EXTRA_BLOG_ITEM = "extra_blog_item";
    private boolean isDarkMode = false;
    private CoordinatorLayout coordinatorLayout;
    private CardView headerCard, imageCard, contentCard;
    private TextView titleText, dateText, authorText, contentText;
    private FloatingActionButton fabDarkMode;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blog_detail);

        // Initialize views
        initViews();

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setup dark mode toggle
        setupDarkModeToggle();

        // Get the blog item from the intent
        BlogDto blogDtos = (BlogDto) getIntent().getSerializableExtra(EXTRA_BLOG_ITEM);

        if (blogDtos != null) {
            loadBlogData(blogDtos);
        }
    }

    private void initViews() {
        coordinatorLayout = findViewById(R.id.coordinator_layout);
        headerCard = findViewById(R.id.header_card);
        imageCard = findViewById(R.id.image_card);
        contentCard = findViewById(R.id.content_card);
        titleText = findViewById(R.id.blog_title);
        dateText = findViewById(R.id.blog_date);
        authorText = findViewById(R.id.blog_author);
        contentText = findViewById(R.id.blog_content);
        fabDarkMode = findViewById(R.id.fab_dark_mode);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupDarkModeToggle() {
        fabDarkMode.setOnClickListener(v -> {
            isDarkMode = !isDarkMode;
            updateTheme();
        });
    }

    private void loadBlogData(BlogDto blogDtos) {
        ImageView blogImage = findViewById(R.id.blog_image);

        // Set data to views
        titleText.setText(blogDtos.getTitle());
        dateText.setText(blogDtos.getDate());
        authorText.setText(getString(R.string.author_prefix) + blogDtos.getAuthor());

        // Use the content field if available, otherwise use the summary
        String contentToDisplay = blogDtos.getContent() != null && !blogDtos.getContent().isEmpty()
                ? blogDtos.getContent()
                : blogDtos.getSummary();

        // Parse HTML content properly using Html.fromHtml() for older Android versions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            contentText.setText(Html.fromHtml(contentToDisplay, Html.FROM_HTML_MODE_COMPACT));
        } else {
            //noinspection deprecation
            contentText.setText(Html.fromHtml(contentToDisplay));
        }

        // Load and display image if available
        String imageUrl = blogDtos.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            imageCard.setVisibility(android.view.View.VISIBLE);
            blogImage.setVisibility(android.view.View.VISIBLE);
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(blogImage);
        } else {
            imageCard.setVisibility(android.view.View.GONE);
        }
    }

    private void updateTheme() {
        if (isDarkMode) {
            // Apply dark theme
            coordinatorLayout.setBackgroundColor(getResources().getColor(R.color.background_dark));
            headerCard.setCardBackgroundColor(getResources().getColor(R.color.surface_dark));
            imageCard.setCardBackgroundColor(getResources().getColor(R.color.surface_dark));
            contentCard.setCardBackgroundColor(getResources().getColor(R.color.surface_dark));
            
            titleText.setTextColor(getResources().getColor(R.color.text_primary_dark));
            dateText.setTextColor(getResources().getColor(R.color.text_secondary_dark));
            authorText.setTextColor(getResources().getColor(R.color.text_secondary_dark));
            contentText.setTextColor(getResources().getColor(R.color.text_primary_dark));
            
            fabDarkMode.setImageResource(R.drawable.ic_light_mode);
        } else {
            // Apply light theme
            coordinatorLayout.setBackgroundColor(getResources().getColor(R.color.background_light));
            headerCard.setCardBackgroundColor(getResources().getColor(R.color.surface_light));
            imageCard.setCardBackgroundColor(getResources().getColor(R.color.surface_light));
            contentCard.setCardBackgroundColor(getResources().getColor(R.color.surface_light));
            
            titleText.setTextColor(getResources().getColor(R.color.text_primary_light));
            dateText.setTextColor(getResources().getColor(R.color.text_secondary_light));
            authorText.setTextColor(getResources().getColor(R.color.text_secondary_light));
            contentText.setTextColor(getResources().getColor(R.color.text_primary_light));
            
            fabDarkMode.setImageResource(R.drawable.ic_dark_mode);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}