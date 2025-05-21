package com.fe.sdkparentapp;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {NotificationEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract NotificationDao notificationDao();
}
