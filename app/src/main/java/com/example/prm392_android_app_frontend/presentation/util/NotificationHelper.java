package com.example.prm392_android_app_frontend.presentation.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (itemCount == 0) {
            notificationManager.cancel(NOTIFICATION_ID);
            return;
        }

        // SỬA LỖI DỨT ĐIỂM: Chuyển đổi Adaptive Icon (ic_launcher) thành Bitmap
        Bitmap largeIcon = null;
        try {
            Drawable drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher);
            if (drawable != null) {
                largeIcon = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(largeIcon);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
            }
        } catch (Exception e) {
            // Bỏ qua nếu có lỗi, không làm crash app
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CART_CHANNEL_ID)
                .setContentTitle("Giỏ hàng")
                .setContentText("Bạn có " + itemCount + " sản phẩm trong giỏ hàng.")
                .setSmallIcon(R.drawable.ic_nav_cart) // Đảm bảo icon này đơn sắc
                .setNumber(itemCount)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        
        // Chỉ đặt largeIcon nếu chuyển đổi thành công
        if (largeIcon != null) {
            builder.setLargeIcon(largeIcon);
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
