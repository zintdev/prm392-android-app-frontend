package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.presentation.fragment.admin.AdminDashboardFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.admin.AdminOrdersFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.admin.AdminChatFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.admin.AdminSettingFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.admin.ProductManagementFragment;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminMainActivity extends AppCompatActivity implements AdminDashboardFragment.AdminDashboardListener {

    private BottomNavigationView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String role = TokenStore.getRole(this);
        if (role == null || !isAdmin(role)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_admin_main_page);
        nav = findViewById(R.id.bottomNavAdmin);
        MaterialToolbar toolbar = findViewById(R.id.toolbarAdmin);

        if (toolbar != null) {
            toolbar.setTitle("Admin");
            toolbar.setOnMenuItemClickListener(item -> {
                // Nếu cần thêm action trên toolbar sau này
                return false;
            });
        }

        if (savedInstanceState == null) {
            switchFragment(new AdminDashboardFragment());
            nav.setSelectedItemId(R.id.nav_dashboard_admin);
        }

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard_admin) {
                switchFragment(new AdminDashboardFragment());
                return true;

            } else if (id == R.id.nav_orders_admin) {
                switchFragment(new AdminOrdersFragment());
                return true;

            } else if (id == R.id.nav_chat_admin) {
                switchFragment(new AdminChatFragment());
                return true;

            } else if (id == R.id.nav_setting_admin) {
                switchFragment(new AdminSettingFragment());
                return true;
            }

            return true;
        });
    }

    private boolean isAdmin(String role) {
        String r = role.trim().toUpperCase();
        return r.equals("ADMIN") || r.equals("ROLE_ADMIN") || r.equals("ADMINISTRATOR");
    }

    private void switchFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentAdminContainer, fragment)
                .commit();
    }

    @Override
    public void navigateToProductManagement() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentAdminContainer, new ProductManagementFragment())
                .addToBackStack(null) // Thêm vào back stack để người dùng có thể quay lại
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
