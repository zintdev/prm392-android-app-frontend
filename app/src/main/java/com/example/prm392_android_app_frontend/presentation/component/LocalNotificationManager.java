package com.example.prm392_android_app_frontend.presentation.component;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.model.Notification;
import com.example.prm392_android_app_frontend.data.repository.NotificationStore;
import com.example.prm392_android_app_frontend.presentation.activity.MainActivity;

public class LocalNotificationManager {

    private static final String CHANNEL_ID = "payment_success_channel";
    private static final String CHANNEL_NAME = "Payment Notifications";
    private static final String CHANNEL_DESC = "Notifications for successful payments";

    private final Context context;
    private final NotificationStore notificationStore;

    public LocalNotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.notificationStore = new NotificationStore(context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Hiển thị thông báo khi THANH TOÁN THÀNH CÔNG (VNPay).
     */
    public void showPaymentSuccessNotification(String orderId) {
        String title = "Thanh toán thành công!";
        String content = "Đơn hàng #" + orderId + " của bạn đã được thanh toán.";
        createAndShowNotification(title, content);
    }

    /**
     * Hiển thị thông báo khi ĐẶT HÀNG THÀNH CÔNG (COD).
     */
    public void showOrderPlacedNotification(String orderId) {
        String title = "Đặt hàng thành công!";
        String content = "Đơn hàng #" + orderId + " đã được tiếp nhận và đang chờ xử lý.";
        createAndShowNotification(title, content);
    }

    private void createAndShowNotification(String title, String content) {
        long timestamp = System.currentTimeMillis();

        // 1. Lưu thông báo vào store
        Notification newNotification = new Notification(title, content, timestamp);
        notificationStore.addNotification(newNotification);

        // 2. Tạo Intent để khi nhấn vào sẽ mở tab thông báo
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(NavbarManager.EXTRA_SELECT_TAB, R.id.nav_notification);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) timestamp, // Dùng timestamp làm request code để mỗi intent là duy nhất
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 3. Xây dựng và hiển thị thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify((int) timestamp, builder.build());
        } catch (SecurityException e) {
            // Xử lý trường hợp không có quyền POST_NOTIFICATIONS trên Android 13+
            android.util.Log.e("LocalNotificationManager", "Missing permission to post notifications.", e);
        }
    }
}
