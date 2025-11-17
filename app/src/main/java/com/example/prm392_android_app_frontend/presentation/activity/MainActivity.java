package com.example.prm392_android_app_frontend.presentation.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.presentation.component.NavbarManager;
import com.example.prm392_android_app_frontend.presentation.util.NotificationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    private NavbarManager navMgr;

    // Launcher để yêu cầu quyền
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Đã cấp quyền hiển thị thông báo.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Bạn sẽ không nhận được thông báo về đơn hàng nếu không cấp quyền.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        // Giữ lại việc tạo channel
        NotificationHelper.createNotificationChannel(this);
        
        // Yêu cầu quyền hiển thị thông báo
        requestNotificationPermission();

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        navMgr = new NavbarManager(
                this,
                getSupportFragmentManager(),
                R.id.fragment_container,
                nav,
                toolbar,
                this // Truyền Activity vào làm ViewModelStoreOwner
        );
        navMgr.init(savedInstanceState);
    }
    
    private void requestNotificationPermission() {
        // Chỉ cần thiết cho Android 13 (API 33) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Kiểm tra xem quyền đã được cấp chưa
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Nếu chưa, hiển thị hộp thoại xin quyền
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        navMgr.handleSelectTabExtra(getIntent());
    }
}
