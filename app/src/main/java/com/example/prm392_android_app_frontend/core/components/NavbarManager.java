package com.example.prm392_android_app_frontend.core.components;

import android.view.View;
import android.widget.LinearLayout;

import com.example.prm392_android_app_frontend.R;

public class NavbarManager {
    
    public interface OnNavItemClickListener {
        void onNavItemClick(int itemId);
    }
    
    private LinearLayout navHome;
    private LinearLayout navBlogs;
    private LinearLayout navSearch;
    private LinearLayout navStats;
    private LinearLayout navProfile;
    
    private int currentSelectedItem = R.id.nav_home; // Default selection
    private OnNavItemClickListener listener;
    
    public NavbarManager(View rootView, OnNavItemClickListener listener) {
        this.listener = listener;
        initViews(rootView);
        setupClickListeners();
        setSelectedItem(currentSelectedItem); // Set initial selection
    }
    
    private void initViews(View rootView) {
        navHome = rootView.findViewById(R.id.nav_home);
        navBlogs = rootView.findViewById(R.id.nav_blogs);
        navSearch = rootView.findViewById(R.id.nav_search);
        navStats = rootView.findViewById(R.id.nav_stats);
        navProfile = rootView.findViewById(R.id.nav_profile);
    }
    
    private void setupClickListeners() {
        navHome.setOnClickListener(v -> {
            setSelectedItem(R.id.nav_home);
            if (listener != null) listener.onNavItemClick(R.id.nav_home);
        });
        
        navBlogs.setOnClickListener(v -> {
            setSelectedItem(R.id.nav_blogs);
            if (listener != null) listener.onNavItemClick(R.id.nav_blogs);
        });
        
        navSearch.setOnClickListener(v -> {
            setSelectedItem(R.id.nav_search);
            if (listener != null) listener.onNavItemClick(R.id.nav_search);
        });
        
        navStats.setOnClickListener(v -> {
            setSelectedItem(R.id.nav_stats);
            if (listener != null) listener.onNavItemClick(R.id.nav_stats);
        });
        
        navProfile.setOnClickListener(v -> {
            setSelectedItem(R.id.nav_profile);
            if (listener != null) listener.onNavItemClick(R.id.nav_profile);
        });
    }
    
    public void setSelectedItem(int itemId) {
        // Reset all items
        resetAllItems();
        
        // Set selected state for the clicked item
        currentSelectedItem = itemId;
        
        if (itemId == R.id.nav_home) {
            navHome.setSelected(true);
        } else if (itemId == R.id.nav_blogs) {
            navBlogs.setSelected(true);
        } else if (itemId == R.id.nav_search) {
            navSearch.setSelected(true);
        } else if (itemId == R.id.nav_stats) {
            navStats.setSelected(true);
        } else if (itemId == R.id.nav_profile) {
            navProfile.setSelected(true);
        }
    }
    
    private void resetAllItems() {
        navHome.setSelected(false);
        navBlogs.setSelected(false);
        navSearch.setSelected(false);
        navStats.setSelected(false);
        navProfile.setSelected(false);
    }
    
    public int getCurrentSelectedItem() {
        return currentSelectedItem;
    }
}