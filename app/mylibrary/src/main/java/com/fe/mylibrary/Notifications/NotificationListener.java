package com.fe.mylibrary.Notifications;

import com.fe.mylibrary.Model.NotificationModel;

public interface NotificationListener {
    void onNotificationReceived(NotificationModel remoteMessage);
}
