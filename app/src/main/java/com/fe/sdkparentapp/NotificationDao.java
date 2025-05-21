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
}
