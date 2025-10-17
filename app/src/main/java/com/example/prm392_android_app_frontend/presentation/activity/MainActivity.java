package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.presentation.component.NavbarManager;
import com.example.prm392_android_app_frontend.presentation.fragment.BlogListFragment;

public class MainActivity extends AppCompatActivity implements NavbarManager.OnNavItemClickListener {

    private static final String TAG = "MainActivity";
    private NavbarManager navbarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize navbar
        navbarManager = new NavbarManager(findViewById(R.id.main), this);

        // Add a button to navigate to the blog list
        Button blogListButton = findViewById(R.id.blog_list_button);
        if (blogListButton != null) {
            blogListButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, BlogListFragment.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "Blog list button not found in layout");
        }
    }

    @Override
    public void onNavItemClick(int itemId) {
        String itemName = getNavItemName(itemId);

        if (itemId == R.id.nav_home) {
            // Already on home, maybe refresh or scroll to top
            Toast.makeText(this, "Home selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_blogs) {
            // Navigate to blogs
            Intent intent = new Intent(MainActivity.this, BlogListFragment.class);
            startActivity(intent);
            Toast.makeText(this, "Blogs selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_search) {
            // Navigate to search (you can create SearchActivity later)
            Toast.makeText(this, "Search selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_stats) {
            // Navigate to stats (you can create StatsActivity later)
            Toast.makeText(this, "Stats selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_profile) {
            // Navigate to profile (you can create ProfileActivity later)
            Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "Navigation item clicked: " + itemName);
    }

    private String getNavItemName(int itemId) {
        if (itemId == R.id.nav_home) return "Home";
        else if (itemId == R.id.nav_blogs) return "Blogs";
        else if (itemId == R.id.nav_search) return "Search";
        else if (itemId == R.id.nav_stats) return "Stats";
        else if (itemId == R.id.nav_profile) return "Profile";
        else return "Unknown";
    }
}