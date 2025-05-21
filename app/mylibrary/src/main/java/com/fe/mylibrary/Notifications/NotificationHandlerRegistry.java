package com.fe.mylibrary.Notifications;

public class NotificationHandlerRegistry {
    private static NotificationListener listener;

    public static void setListener(NotificationListener notificationListener) {
        listener = notificationListener;
    }

    public static NotificationListener getListener() {
        return listener;
    }
}
