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
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.its.notificationlibrary.R;

import org.json.JSONObject;

import java.util.Random;

public class NotificationHandler extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "ITSNotificationService";


    private static NotificationClickListener clickListener;

    public static void setNotificationClickListener(NotificationClickListener listener) {
        clickListener = listener;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Context context = getApplicationContext();

        String title = "";
        String message = "";
        JSONObject payloadJson = new JSONObject();

        if (remoteMessage.getNotification() != null) {
            // ✅ These will work correctly with Urdu if the payload is UTF-8 encoded
            title = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();

            // ✅ Safely store custom data into JSON object
            if (!remoteMessage.getData().isEmpty()) {
                try {
                    for (String key : remoteMessage.getData().keySet()) {
                        payloadJson.put(key, remoteMessage.getData().get(key));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // ✅ Listener logic (Make sure NotificationModel supports Unicode strings)
            NotificationListener listener = NotificationHandlerRegistry.getListener();
            if (listener != null) {
                NotificationModel myNotification = new NotificationModel(title, message, payloadJson);
                listener.onNotificationReceived(myNotification);
            }

            // ✅ Send broadcast to host app
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



            // ✅ Create notification channel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelName = "ITS Notification Service";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
                else {
                    Log.d(TAG, "Notification Manager is null: ");

                }
            }

            // ✅ Show system notification with Urdu text
            showNotification(title, message,pendingIntent);
        }

        // ✅ Update delivery status
        NotificationUtil.updateNotificationStatus(this, "delivered", remoteMessage.getData().get("client_id"),
                remoteMessage.getData().get("transaction_id"), new NotificationStatusCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("ViewNotification", "Status update successful");
                // You could show a toast or update local DB here
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("ViewNotification", "Status update failed: " + e.getMessage());
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void showNotification(String title, String message,PendingIntent pendingIntent) {
        String updatedMessage = isUrdu(message) ? "\u200F" + message : message;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(getApplicationInfo().icon)
                .setContentTitle(title) // Urdu-safe
                .setContentText(updatedMessage) // Urdu-safe
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // Important for long Urdu
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(number, builder.build());
    }

    public static boolean isUrdu(String text) {
        for (char c : text.toCharArray()) {
            if ((c >= '\u0600' && c <= '\u06FF') ||
                    (c >= '\u0750' && c <= '\u077F') ||
                    (c >= '\uFB50' && c <= '\uFDFF') ||
                    (c >= '\uFE70' && c <= '\uFEFF')) {
                return true; // Found Urdu character
            }
        }
        return false; // No Urdu characters found
    }



    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM token refreshed");
        // Send token to your backend if needed
    }





}