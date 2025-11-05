package com.example.prm392_android_app_frontend.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.prm392_android_app_frontend.data.model.Notification;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationStore {

    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String NOTIFICATIONS_KEY = "notification_list";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public NotificationStore(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<Notification> getNotifications() {
        String json = sharedPreferences.getString(NOTIFICATIONS_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Notification>>() {}.getType();
        List<Notification> notifications = gson.fromJson(json, type);
        // Sắp xếp để thông báo mới nhất lên đầu
        Collections.sort(notifications, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
        return notifications;
    }

    public void addNotification(Notification notification) {
        List<Notification> notifications = getNotifications();
        notifications.add(notification);
        // Sắp xếp lại để đảm bảo thứ tự khi lưu
        Collections.sort(notifications, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
        String json = gson.toJson(notifications);
        sharedPreferences.edit().putString(NOTIFICATIONS_KEY, json).apply();
    }
    
    public void markAllAsRead() {
        List<Notification> notifications = getNotifications();
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        String json = gson.toJson(notifications);
        sharedPreferences.edit().putString(NOTIFICATIONS_KEY, json).apply();
    }

    public int getUnreadCount() {
        List<Notification> notifications = getNotifications();
        int count = 0;
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                count++;
            }
        }
        return count;
    }
}
