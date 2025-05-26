package com.its.notificationlibrary.Notifications;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Context context = getApplicationContext();




        String title = "";
        String message = "";
        JSONObject payloadJson = new JSONObject();
        Log.d(TAG, "From Module: " + remoteMessage.getData());

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



            Log.d(TAG, "Notification Title From Module: " + title);
            Log.d(TAG, "Notification Message From Module: " + message);

            // Send broadcast to app
            Intent intent = new Intent("com.its.notificationlibrary.NOTIFICATION_RECEIVED");
            intent.putExtra("title", title);
            intent.putExtra("body", message);
            sendBroadcast(intent);

            // Create Notification Channel (Android 8.0+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                String channelName = "ITS Notification Service";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }



            // Optional: Show a system notification
            showNotification(title, message);
        }

        NotificationUtil.updateNotificationStatus(context,"delivered", remoteMessage.getData().get("client_id"), remoteMessage.getData().get("transaction_id"));

    }

    @SuppressLint("MissingPermission")
    private void showNotification(String title, String message) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(getApplicationInfo().icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId = new Random().nextInt(9_999_999) + 1;
        notificationManager.notify(notificationId, builder.build());
    }



    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New Token: " + token);
        // Send token to your backend if needed
    }





}