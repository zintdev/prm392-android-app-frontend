package com.example.prm392_android_app_frontend.presentation.fragment.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.presentation.adapter.BlogAdapter;
import com.example.prm392_android_app_frontend.presentation.component.NavbarManager;
import com.example.prm392_android_app_frontend.presentation.activity.MainActivity;
import com.example.prm392_android_app_frontend.data.dto.BlogDto;
import com.example.prm392_android_app_frontend.data.repository.BlogRepository;
import com.example.prm392_android_app_frontend.R;

import java.util.List;

public class BlogListFragment extends AppCompatActivity implements BlogRepository.BlogDataListener, NavbarManager.OnNavItemClickListener {

    private static final String TAG = "BlogListActivity";
    private RecyclerView recyclerView;
    private BlogAdapter adapter;
    private NavbarManager navbarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloglist);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Add smooth scrolling and performance optimizations
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        // Initialize navbar - pass the root view so NavbarManager can find navbar items
        navbarManager = new NavbarManager(findViewById(android.R.id.content), this);
        // Set blogs as selected since we're in BlogListActivity
        navbarManager.setSelectedItem(R.id.nav_blogs);

        // Fetch blog data
        BlogRepository blogRepository = new BlogRepository();
        blogRepository.fetchData(this);
    }

    @Override
    public void onBlogDataFetched(List<BlogDto> blogDtos) {
        Log.d(TAG, "Blog data fetched successfully, size: " + blogDtos.size());
        if (adapter == null) {
            adapter = new BlogAdapter(blogDtos);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(blogDtos);
        }
    }

    @Override
    public void onError(String errorMessage) {
        Log.e(TAG, "Error fetching blog data: " + errorMessage);
        // You might want to show an error message to the user
    }

    @Override
    public void onNavItemClick(int itemId) {
        String itemName = getNavItemName(itemId);
        
        if (itemId == R.id.nav_home) {
            // Navigate to home
            Intent intent = new Intent(BlogListFragment.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close current activity
        } else if (itemId == R.id.nav_blogs) {
            // Already on blogs, maybe refresh or scroll to top
            Toast.makeText(this, "Already on Blogs", Toast.LENGTH_SHORT).show();
            recyclerView.smoothScrollToPosition(0);
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