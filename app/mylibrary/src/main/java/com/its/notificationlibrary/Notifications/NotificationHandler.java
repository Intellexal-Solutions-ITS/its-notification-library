package com.its.notificationlibrary.Notifications;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.its.notificationlibrary.Model.NotificationModel;

import org.json.JSONObject;

import java.util.Map;
import java.util.Random;

/**
 * Plain callable handler for an ITS-originated FCM message — NOT a
 * FirebaseMessagingService. A host app that already owns its own FCM
 * receiving (e.g. via the firebase_messaging Flutter plugin) keeps that
 * ownership; call {@link #handleRemoteMessage} from that existing pipeline
 * once you've identified the message as ITS-originated (e.g. presence of
 * a "transaction_id" key in the data payload).
 */
public class NotificationHandler {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "ITSNotificationService";

    public static void handleRemoteMessage(Context context, String title, String message, Map<String, String> data) {
        JSONObject payloadJson = new JSONObject();
        try {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                payloadJson.put(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        NotificationListener listener = NotificationHandlerRegistry.getListener();
        if (listener != null) {
            NotificationModel myNotification = new NotificationModel(title, message, payloadJson);
            listener.onNotificationReceived(myNotification);
        }

        Intent broadcast = new Intent("com.its.notificationlibrary.NOTIFICATION_RECEIVED");
        broadcast.putExtra("title", title);
        broadcast.putExtra("body", message);
        context.sendBroadcast(broadcast);

        Intent launchIntent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());

        PendingIntent pendingIntent = null;
        if (launchIntent != null) {
            launchIntent.putExtra("id", data.get("transaction_id"));
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        } else {
            Log.d(TAG, "No launch intent found for package, notification will not be tappable");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "ITS Notification Service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            } else {
                Log.d(TAG, "Notification Manager is null: ");
            }
        }

        showNotification(context, title, message, pendingIntent);

        NotificationUtil.updateNotificationStatus(context, "delivered", data.get("client_id"),
                data.get("transaction_id"), new NotificationStatusCallback() {
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
    private static void showNotification(Context context, String title, String message, PendingIntent pendingIntent) {
        String updatedMessage = isUrdu(message) ? "\u200F" + message : message;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(context.getApplicationInfo().icon)
                .setContentTitle(title)
                .setContentText(updatedMessage)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(number, builder.build());
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
}
