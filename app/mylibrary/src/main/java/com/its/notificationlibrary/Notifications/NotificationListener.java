package com.its.notificationlibrary.Notifications;

import com.its.notificationlibrary.Model.NotificationModel;

public interface NotificationListener {
    void onNotificationReceived(NotificationModel remoteMessage);
}
