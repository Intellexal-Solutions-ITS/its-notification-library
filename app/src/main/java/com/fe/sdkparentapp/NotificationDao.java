package com.fe.sdkparentapp;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomDatabase;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    void insertNotification(NotificationEntity notification);

    @Query("SELECT * FROM notifications")
    List<NotificationEntity> getAllNotifications();

    @Query("SELECT * FROM notifications WHERE notification_id = :transactionId")
    NotificationEntity getNotificationById(String transactionId);

    @Query("UPDATE notifications SET status = :status WHERE notification_id = :notificationId")
    void updateStatus(String notificationId, String status);

    @Query("DELETE FROM notifications")
    void clearAllNotifications();

}
