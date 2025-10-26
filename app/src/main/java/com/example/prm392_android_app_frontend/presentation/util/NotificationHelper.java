package com.example.prm392_android_app_frontend.presentation.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.prm392_android_app_frontend.R;

public class NotificationHelper {

    private static final String CART_CHANNEL_ID = "cart_channel";
    private static final int NOTIFICATION_ID = 1;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Cart Notifications";
            String description = "Channel for cart notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CART_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(true);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void showCartNotification(Context context, int itemCount) {
        // Bắt buộc phải có quyền POST_NOTIFICATIONS trên Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Nếu không có quyền, không hiển thị thông báo
                // Bạn có thể yêu cầu quyền ở đây nếu cần
                return;
            }
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (itemCount == 0) {
            // Hủy thông báo nếu giỏ hàng trống
            notificationManager.cancel(NOTIFICATION_ID);
            return;
        }

        // Tạo thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CART_CHANNEL_ID)
                .setContentTitle("Giỏ hàng")
                .setContentText("Bạn có " + itemCount + " sản phẩm trong giỏ hàng.")
                .setSmallIcon(R.drawable.ic_nav_cart) // Đảm bảo bạn có icon này
                .setNumber(itemCount)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Hiển thị thông báo
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
