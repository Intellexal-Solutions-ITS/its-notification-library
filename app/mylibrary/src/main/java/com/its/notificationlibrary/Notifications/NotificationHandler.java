package com.its.notificationlibrary.Notifications;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.its.notificationlibrary.Model.NotificationModel;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Random;

public class NotificationHandler extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "ITSNotificationService";

    // Host apps can override the small icon used for notifications by adding
    // this meta-data to their AndroidManifest.xml (same pattern Firebase's own
    // SDK uses for com.google.firebase.messaging.default_notification_icon):
    //
    // <meta-data
    //     android:name="com.its.notificationlibrary.notification_icon"
    //     android:resource="@drawable/ic_notification" />
    //
    // The referenced drawable must be a flat white/transparent silhouette —
    // Android auto-flattens anything else (like a full-color launcher icon)
    // into a plain white blob in the status bar. Android Studio's Image Asset
    // Studio ("Notification Icons" asset type) generates a correct one from
    // your existing app icon. Falls back to the app's launcher icon if no
    // meta-data is declared.
    private static final String NOTIFICATION_ICON_META_DATA = "com.its.notificationlibrary.notification_icon";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Context context = getApplicationContext();

        String title = "";
        String message = "";
        JSONObject payloadJson = new JSONObject();

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();

            if (!remoteMessage.getData().isEmpty()) {
                try {
                    for (String key : remoteMessage.getData().keySet()) {
                        payloadJson.put(key, remoteMessage.getData().get(key));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            NotificationListener listener = NotificationHandlerRegistry.getListener();
            if (listener != null) {
                NotificationModel myNotification = new NotificationModel(title, message, payloadJson);
                listener.onNotificationReceived(myNotification);
            }

            Intent i = new Intent("com.its.notificationlibrary.NOTIFICATION_RECEIVED");
            i.putExtra("title", title);
            i.putExtra("body", message);
            sendBroadcast(i);

            Intent intent = context.getPackageManager()
                    .getLaunchIntentForPackage(context.getPackageName());

            PendingIntent pendingIntent = null;
            if (intent != null) {
                intent.putExtra("id", remoteMessage.getData().get("transaction_id"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
            } else {
                Log.d(TAG, "No launch intent found for package, notification will not be tappable");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelName = "ITS Notification Service";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                } else {
                    Log.d(TAG, "Notification Manager is null: ");
                }
            }

            showNotification(title, message, pendingIntent);
        }

        NotificationUtil.updateNotificationStatus(this, "delivered", remoteMessage.getData().get("client_id"),
                remoteMessage.getData().get("transaction_id"), new NotificationStatusCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("ViewNotification", "Status update successful");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("ViewNotification", "Status update failed: " + e.getMessage());
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void showNotification(String title, String message, PendingIntent pendingIntent) {
        String updatedMessage = isUrdu(message) ? "‏" + message : message;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(resolveSmallIcon())
                .setContentTitle(title)
                .setContentText(updatedMessage)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(number, builder.build());
    }

    private int resolveSmallIcon() {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = ai.metaData;
            if (metaData != null) {
                int iconRes = metaData.getInt(NOTIFICATION_ICON_META_DATA);
                if (iconRes != 0) {
                    return iconRes;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to read notification icon meta-data, falling back to app icon", e);
        }
        return getApplicationInfo().icon;
    }

    public static boolean isUrdu(String text) {
        for (char c : text.toCharArray()) {
            if ((c >= '؀' && c <= 'ۿ') ||
                    (c >= 'ݐ' && c <= 'ݿ') ||
                    (c >= 'ﭐ' && c <= '﷿') ||
                    (c >= 'ﹰ' && c <= '﻿')) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM token refreshed");
    }
}
