package com.fe.sdkparentapp;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class NotificationEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "message")
    public String message;

    @ColumnInfo(name = "payload")
    public String payload;
    @ColumnInfo(name = "status")
    public String status;
    @ColumnInfo(name = "datetime")
    public long datetime;

    // Constructor and getters/setters
}
