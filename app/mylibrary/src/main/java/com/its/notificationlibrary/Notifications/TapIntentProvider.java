package com.its.notificationlibrary.Notifications;

import android.app.PendingIntent;
import android.content.Context;

import org.json.JSONObject;

public interface TapIntentProvider {
    PendingIntent getTapIntent(Context context, JSONObject payloadJson);
}