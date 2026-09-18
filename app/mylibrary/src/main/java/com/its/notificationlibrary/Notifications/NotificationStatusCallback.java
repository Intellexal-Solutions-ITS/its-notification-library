package com.its.notificationlibrary.Notifications;

public interface NotificationStatusCallback {
    void onSuccess(String response);
    void onFailure(Exception e);
}
