package com.example.prm392_android_app_frontend.presentation.activity;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.presentation.fragment.AccountFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.BlogFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.CartFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.HomeFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.NotificationFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.SettingFragment;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        nav = findViewById(R.id.bottomNav);
        MaterialToolbar toolbar = (MaterialToolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_open_map) {
                startActivity(new Intent(this, MapsActivity.class));
                return true;
            }
            return false;
            });
        }

        if (savedInstanceState == null) {
            switchFragment(new HomeFragment());
            nav.setSelectedItemId(R.id.nav_home);
        }

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                switchFragment(new HomeFragment());
                return true;

            } else if (id == R.id.nav_blog) {
                switchFragment(new BlogFragment());
                return true;

            } else if (id == R.id.nav_cart) {
                switchFragment(new CartFragment());
                return true;

            } else if (id == R.id.nav_notification) {
                switchFragment(new NotificationFragment());
                return true;

            } else if (id == R.id.nav_setting) {
                switchFragment(new SettingFragment());
                return true;

            } else if (id == R.id.nav_account) {
                if (TokenStore.isLoggedIn(this)) {
                    switchFragment(new AccountFragment());
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Yêu cầu đăng nhập")
                            .setMessage("Bạn cần đăng nhập để sử dụng tính năng này.")
                            .setPositiveButton("Đăng nhập", (d, w) -> startActivity(new Intent(this, LoginActivity.class)))
                            .setNegativeButton("Hủy", null)
                            .show();
                    return false;
                }
            }

            return true;
        });
    }

    private void switchFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int nextTab = getIntent().getIntExtra("select_tab", -1);
        if (nextTab != -1) {
            nav.setSelectedItemId(nextTab);
            getIntent().removeExtra("select_tab");
        }
    }
}