package com.example.prm392_android_app_frontend.presentation.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.presentation.fragment.user.AccountFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.user.BlogFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.user.CartFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.user.HomeFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.user.NotificationFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.user.SettingFragment;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavbarManager navMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        navMgr = new NavbarManager(
                this,
                getSupportFragmentManager(),
                R.id.fragment_container,   // ID container fragment trong activity_main_page.xml
                nav,
                toolbar
        );
        navMgr.init(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        navMgr.handleSelectTabExtra(getIntent());
    }
}
