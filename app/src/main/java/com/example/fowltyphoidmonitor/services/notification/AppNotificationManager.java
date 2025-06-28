package com.example.fowltyphoidmonitor.services.notification;

import com.example.fowltyphoidmonitor.ui.common.NotificationItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AppNotificationManager {
    private static AppNotificationManager instance;
    private List<NotificationItem> notifications;
    private List<NotificationListener> listeners;

    public interface NotificationListener {
        void onNotificationsChanged();
    }

    private AppNotificationManager() {
        notifications = new CopyOnWriteArrayList<>();
        listeners = new CopyOnWriteArrayList<>();
    }

    public static synchronized AppNotificationManager getInstance() {
        if (instance == null) {
            instance = new AppNotificationManager();
        }
        return instance;
    }

    public void addListener(NotificationListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (NotificationListener listener : listeners) {
            try {
                listener.onNotificationsChanged();
            } catch (Exception e) {
                // Handle listener errors gracefully
            }
        }
    }

    public void addNotification(NotificationItem notification) {
        if (notification != null) {
            notifications.add(0, notification); // Add to beginning for newest first
            notifyListeners();
        }
    }

    public void removeNotification(int notificationId) {
        // FIXED: Don't use iterator.remove() with CopyOnWriteArrayList
        NotificationItem toRemove = null;
        for (NotificationItem item : notifications) {
            if (item.getId() == notificationId) {
                toRemove = item;
                break;
            }
        }

        if (toRemove != null) {
            notifications.remove(toRemove);
            notifyListeners();
        }
    }

    // Add the String parameter version that's causing the crash
    public void dismissNotification(String notificationId) {
        // API level 23 compatible version (no removeIf)
        NotificationItem toRemove = null;
        for (NotificationItem item : notifications) {
            if (String.valueOf(item.getId()).equals(notificationId)) {
                toRemove = item;
                break;
            }
        }

        if (toRemove != null) {
            notifications.remove(toRemove);
            notifyListeners();
        }
    }

    public void markAsRead(int notificationId) {
        for (NotificationItem notification : notifications) {
            if (notification.getId() == notificationId) {
                notification.setRead(true);
                notifyListeners();
                break;
            }
        }
    }

    public void dismissNotification(int notificationId) {
        // FIXED: Don't use iterator.remove() with CopyOnWriteArrayList
        NotificationItem toRemove = null;
        for (NotificationItem item : notifications) {
            if (item.getId() == notificationId) {
                item.setDismissed(true);
                toRemove = item;
                break;
            }
        }

        if (toRemove != null) {
            notifications.remove(toRemove);
            notifyListeners();
        }
    }

    public List<NotificationItem> getAllNotifications() {
        return new ArrayList<>(notifications);
    }

    public List<NotificationItem> getUnreadNotifications() {
        List<NotificationItem> unread = new ArrayList<>();
        for (NotificationItem notification : notifications) {
            if (!notification.isRead() && !notification.isDismissed()) {
                unread.add(notification);
            }
        }
        return unread;
    }

    public int getUnreadCount() {
        int count = 0;
        for (NotificationItem notification : notifications) {
            if (!notification.isRead() && !notification.isDismissed()) {
                count++;
            }
        }
        return count;
    }

    public void clearAllNotifications() {
        notifications.clear();
        notifyListeners();
    }

    public void markAllAsRead() {
        for (NotificationItem notification : notifications) {
            notification.setRead(true);
        }
        notifyListeners();
    }
}