package com.fe.sdkparentapp;

public class NotificationCounter {
    private static int count = 0;

    public static void increment() {
        count++;
    }

    public static int getCount() {
        return count;
    }

    public static void reset() {
        count = 0;
    }
}

