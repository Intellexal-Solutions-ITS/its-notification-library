package com.fe.sdkparentapp;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.its.notificationlibrary.Notifications.NotificationHandlerRegistry;
import com.its.notificationlibrary.Notifications.NotificationModule;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    TextView badgeTextView;
    RelativeLayout notification_container;
    AppDatabase db;

    ImageView logoutIcon;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "notifications-db")
                .fallbackToDestructiveMigration()
                .build();

        badgeTextView = findViewById(R.id.badge_count);
        notification_container = findViewById(R.id.notification_container);
        TextView phoneText = findViewById(R.id.textPhone);
        logoutIcon = findViewById(R.id.logout_icon);


        String phone = getIntent().getStringExtra("phone");
        phoneText.setText("Hello " + phone);
        Log.d("HomeActivity", "Received phone: " + phone);

        // Bell icon click opens NotificationActivity

        notification_container.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, NotificationActivity.class)));

        // 🔔 Handle notification click — must be registered before handleNotificationIntent,
        // otherwise a cold-start tap finds the listener still null and silently drops.
        NotificationModule.setNotificationClickListener(id -> {

            Log.d("NotificationClick", "Notification tapped, id: " + id);

            new Thread(() -> {
                NotificationEntity notification = db.notificationDao().getNotificationById(id);
                if (notification == null) {
                    Log.e("NotificationClick", "No local notification found for id: " + id);
                    return;
                }
                try {
                    JSONObject jo = new JSONObject(notification.payload);

                    // Prepare all data
                    String clientId = jo.getString("client_id");
                    String transactionId = jo.getString("transaction_id");
                    String title = notification.title;
                    String message = notification.message;
                    String formattedDate = new SimpleDateFormat("MMM dd, yyyy - hh:mm:ss a", Locale.getDefault())
                            .format(new Date(notification.datetime));

                    Intent i = new Intent(HomeActivity.this, ViewNotificationActivity.class);
                    i.putExtra("client_id", clientId);
                    i.putExtra("transaction_id", transactionId);
                    i.putExtra("title", title);
                    i.putExtra("message", message);
                    i.putExtra("datetime", formattedDate);
                    i.putExtra("status", notification.status);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Log.d("NotificationClick", "Opening ViewNotificationActivity for: " + transactionId);
                    startActivity(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }).start();
        });

        NotificationModule.handleNotificationIntent(getIntent());


        // 🔔 Handle notification received
        NotificationHandlerRegistry.setListener(remoteMessage -> {
            Log.d("HostApp", "Title: " + remoteMessage.getTitle());
            Log.d("HostApp", "Message: " + remoteMessage.getMessage());
            Log.d("HostApp", "Payload: " + remoteMessage.getPayload().toString());

            NotificationEntity notification = new NotificationEntity();
            notification.title = remoteMessage.getTitle();
            notification.message = remoteMessage.getMessage();
            notification.payload = remoteMessage.getPayload().toString();
            notification.status = "delivered";
            notification.datetime = System.currentTimeMillis();
            try {
                notification.notification_id = remoteMessage.getPayload().getString("transaction_id");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            new Thread(() -> {
                db.notificationDao().insertNotification(notification);
                int count = db.notificationDao().getUnreadCount();
                runOnUiThread(() -> updateBadgeCount(count));
            }).start();
        });

        logoutIcon.setOnClickListener(v -> {
            new Thread(() -> {
                // Clear your Room database
                db.notificationDao().clearAllNotifications();
                SessionManager.clearSession(HomeActivity.this);

                runOnUiThread(() -> {
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            }).start();
        });

    }

    private void updateBadgeCount(int count) {
        if (count > 0) {
            badgeTextView.setText(String.valueOf(count));
            badgeTextView.setVisibility(View.VISIBLE);
        } else {
            badgeTextView.setVisibility(View.GONE);
        }
    }

    private void refreshBadgeCount() {
        new Thread(() -> {
            int count = db.notificationDao().getUnreadCount();
            runOnUiThread(() -> updateBadgeCount(count));
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBadgeCount();
    }
}
