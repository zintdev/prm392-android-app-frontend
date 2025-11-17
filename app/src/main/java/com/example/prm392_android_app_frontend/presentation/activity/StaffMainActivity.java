package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.presentation.fragment.staff.StaffInventoryFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.staff.StaffOrdersFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.staff.StaffProfileFragment;
import com.example.prm392_android_app_frontend.presentation.viewmodel.StaffSharedViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Entry point for staff role. Contains a bottom navigation bar that lets staff
 * switch between order management, inventory management, chat with admin and profile screens.
 */
public class StaffMainActivity extends AppCompatActivity {

    public static final String EXTRA_SELECT_TAB = "select_tab";

    private BottomNavigationView bottomNav;
    private StaffSharedViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!TokenStore.isLoggedIn(this) || !TokenStore.isStaff(this)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_staff_main_page);

        MaterialToolbar toolbar = findViewById(R.id.toolbarStaff);
        if (toolbar != null) {
            toolbar.setTitle(R.string.staff_toolbar_title);
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_open_map) {
                    startActivity(new Intent(this, MapsActivity.class));
                    return true;
                }
                return false;
            });
        }

        bottomNav = findViewById(R.id.bottomNavStaff);
        sharedViewModel = new ViewModelProvider(this).get(StaffSharedViewModel.class);
        sharedViewModel.loadCurrentStaff();

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_staff_orders);
            switchFragment(new StaffOrdersFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_staff_orders) {
                switchFragment(new StaffOrdersFragment());
                return true;
            }
            if (id == R.id.nav_staff_inventory) {
                switchFragment(new StaffInventoryFragment());
                return true;
            }
            if (id == R.id.nav_staff_profile) {
                switchFragment(new StaffProfileFragment());
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        int nextTab = getIntent().getIntExtra(EXTRA_SELECT_TAB, -1);
        if (nextTab != -1 && bottomNav != null) {
            bottomNav.setSelectedItemId(nextTab);
            getIntent().removeExtra(EXTRA_SELECT_TAB);
        }
    }

    private void switchFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentStaffContainer, fragment)
                .commit();
    }
}
