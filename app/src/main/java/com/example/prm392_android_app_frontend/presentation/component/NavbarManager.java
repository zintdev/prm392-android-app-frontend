package com.example.prm392_android_app_frontend.presentation.component;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.CartDto;
import com.example.prm392_android_app_frontend.data.repository.NotificationStore;
import com.example.prm392_android_app_frontend.presentation.activity.LoginActivity;
import com.example.prm392_android_app_frontend.presentation.activity.MapsActivity;
import com.example.prm392_android_app_frontend.presentation.fragment.user.AccountFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.user.BlogListFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.user.CartFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.user.HomeFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.user.NotificationFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.user.SettingFragment;
import com.example.prm392_android_app_frontend.presentation.viewmodel.CartViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavbarManager {

    public static final String EXTRA_SELECT_TAB = "select_tab";

    private final Activity activity;
    private final FragmentManager fm;
    private final @IdRes int containerId;
    private final BottomNavigationView bottomNav;
    private final MaterialToolbar toolbar;
    private final CartViewModel cartViewModel;
    private final NotificationStore notificationStore;

    public NavbarManager(Activity activity,
                         FragmentManager fm,
                         @IdRes int containerId,
                         BottomNavigationView bottomNav,
                         MaterialToolbar toolbar,
                         ViewModelStoreOwner viewModelStoreOwner) {
        this.activity = activity;
        this.fm = fm;
        this.containerId = containerId;
        this.bottomNav = bottomNav;
        this.toolbar = toolbar;
        this.cartViewModel = new ViewModelProvider(viewModelStoreOwner).get(CartViewModel.class);
        this.notificationStore = new NotificationStore(activity.getApplicationContext());
    }

    /**
     * Gọi trong onCreate() của MainActivity
     */
    public void init(@Nullable Bundle savedInstanceState) {
        setupToolbarMenu();
        setupBottomNav();
        observeCartViewModel();
        updateNotificationBadge(); // Cập nhật badge thông báo khi khởi tạo

        if (savedInstanceState == null) {
            Intent intent = activity.getIntent();
            int selectTab = intent.getIntExtra(EXTRA_SELECT_TAB, -1);
            if (selectTab != -1) {
                bottomNav.setSelectedItemId(selectTab);
                switchByMenuId(selectTab);
                intent.removeExtra(EXTRA_SELECT_TAB);
            } else {
                bottomNav.setSelectedItemId(R.id.nav_home);
                switchFragment(new HomeFragment());
            }
        }
    }

    private void observeCartViewModel() {
        cartViewModel.getCartLiveData().observeForever(this::updateCartBadge);
    }

    private void updateCartBadge(CartDto cartDto) {
        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_cart);
        if (cartDto != null && cartDto.getItems() != null && !cartDto.getItems().isEmpty()) {
            badge.setVisible(true);
            badge.setNumber(cartDto.getItems().size());
        } else {
            badge.setVisible(false);
        }
    }

    public void updateNotificationBadge() {
        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_notification);
        int unreadCount = notificationStore.getUnreadCount();
        if (unreadCount > 0) {
            badge.setVisible(true);
            badge.setNumber(unreadCount);
        } else {
            badge.setVisible(false);
        }
    }

    /**
     * Gọi trong onResume() của MainActivity để xử lý chọn tab qua Intent
     */
    public void handleSelectTabExtra(Intent intent) {
        if (intent == null) return;
        int nextTab = intent.getIntExtra(EXTRA_SELECT_TAB, -1);
        if (nextTab != -1) {
            bottomNav.setSelectedItemId(nextTab);
            switchByMenuId(nextTab);
            intent.removeExtra(EXTRA_SELECT_TAB);
        }
        // Luôn cập nhật badge khi resume
        updateNotificationBadge();
    }

    private void setupToolbarMenu() {
        if (toolbar == null) return;
        toolbar.setOnMenuItemClickListener(this::onToolbarMenuItemClicked);
    }

    private boolean onToolbarMenuItemClicked(MenuItem item) {
        if (item.getItemId() == R.id.action_open_map) {
            activity.startActivity(new Intent(activity, MapsActivity.class));
            return true;
        }
        return false;
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            boolean ok = switchByMenuId(item.getItemId());
            return ok;
        });
    }

    private boolean switchByMenuId(int id) {
        if (id == R.id.nav_home) return switchFragment(new HomeFragment());
        else if (id == R.id.nav_blog) return switchFragment(new BlogListFragment());
        else if (id == R.id.nav_cart) return handleCartTab();
        else if (id == R.id.nav_notification) {
            boolean switched = switchFragment(new NotificationFragment());
            if (switched) {
                // Sau khi chuyển đến tab, cập nhật lại badge
                updateNotificationBadge();
            }
            return switched;
        }
//        else if (id == R.id.nav_setting) return switchFragment(new SettingFragment());
        else if (id == R.id.nav_account) return handleAccountTab();
        return false;
    }

    private boolean handleCartTab() {
        if (TokenStore.isLoggedIn(activity)) {
            return switchFragment(new CartFragment());
        } else {
            new AlertDialog.Builder(activity)
                    .setTitle("Yêu cầu đăng nhập")
                    .setMessage("Bạn cần đăng nhập để sử dụng tính năng này.")
                    .setPositiveButton("ĐĂNG NHẬP",
                            (d, w) -> activity.startActivity(new Intent(activity, LoginActivity.class)))
                    .setNegativeButton("HỦY", null)
                    .show();
            return false;
        }
    }

    private boolean handleAccountTab() {
        if (TokenStore.isLoggedIn(activity)) {
            return switchFragment(new AccountFragment());
        } else {
            new AlertDialog.Builder(activity)
                    .setTitle("Yêu cầu đăng nhập")
                    .setMessage("Bạn cần đăng nhập để sử dụng tính năng này.")
                    .setPositiveButton("Đăng nhập",
                            (d, w) -> activity.startActivity(new Intent(activity, LoginActivity.class)))
                    .setNegativeButton("Hủy", null)
                    .show();
            return false;
        }
    }

    private boolean switchFragment(Fragment fragment) {
        fm.beginTransaction()
                .replace(containerId, fragment)
                .commit();
        return true;
    }

    //    public void setBottomNavVisible(boolean visible) {
    //        bottomNav.setVisibility(visible ? View.VISIBLE : View.GONE);
    //    }
}
