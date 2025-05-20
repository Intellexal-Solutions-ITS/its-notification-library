package com.fe.mylibrary;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class NotificationHandler extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "ITSNotificationService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Context context = getApplicationContext();

        Log.d(TAG, "From Module: " + remoteMessage.getFrom());

        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String message = remoteMessage.getNotification().getBody();

            Log.d(TAG, "Notification Title From Module: " + title);
            Log.d(TAG, "Notification Message From Module: " + message);

            // Send broadcast to app
            Intent intent = new Intent("com.fe.mylibrary.NOTIFICATION_RECEIVED");
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

        updateNotificationStatus(context,"delivered");
    }

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


    private static void updateNotificationStatus(Context ctx,String status) {


        Prefs secureStorage = new Prefs(ctx);
        String bearerToken = secureStorage.getBearerToken();
        ApiClient apiClient = new ApiClient("https://statusapp.free.beeceptor.com/api/", bearerToken);



        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("status", status);

        } catch (Exception e) {
            e.printStackTrace();
        }

        apiClient.post("updateNotificationStatus", jsonBody, new ApiClient.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        // Handle success
                        Log.d("API Response", "User registered: " + response);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Handle failure
                        Log.e("API Error", "Registration failed: " + e.getMessage());
                    }
                });

    }




}