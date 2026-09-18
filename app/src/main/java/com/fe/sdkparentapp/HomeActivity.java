package com.fe.sdkparentapp;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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


    private final BroadcastReceiver badgeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int count = intent.getIntExtra("count", 0);
            updateBadgeCount(count);
        }
    };

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

        notification_container.setOnClickListener(v -> {
            NotificationCounter.reset();
            startActivity(new Intent(HomeActivity.this, NotificationActivity.class));
        });

        // Show existing badge count
        updateBadgeCount(NotificationCounter.getCount());



        NotificationModule.handleNotificationIntent(getIntent());




        // 🔔 Handle notification click
        NotificationModule.setNotificationClickListener(id -> {

            Log.e("CLICK NOTIFICATION", "onCreate: ");

            new Thread(() -> {
                NotificationEntity notification = db.notificationDao().getNotificationById(id);
                try {
                    JSONObject jo = new JSONObject(notification.payload);

                    // Prepare all data
                    String clientId = jo.getString("client_id");
                    String transactionId = jo.getString("transaction_id");
                    String title = notification.title;
                    String message = notification.message;
                    String formattedDate = new SimpleDateFormat("MMM dd, yyyy - hh:mm:ss a", Locale.getDefault())
                            .format(new Date(notification.datetime));

                    // Switch to UI thread to start the activity
//                    runOnUiThread(() -> {
                        Intent i = new Intent(HomeActivity.this, ViewNotificationActivity.class);
                        i.putExtra("client_id", clientId);
                        i.putExtra("transaction_id", transactionId);
                        i.putExtra("title", title);
                        i.putExtra("message", message);
                        i.putExtra("datetime", formattedDate);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Log.d("NotificationClick", "Opening ViewNotificationActivity for: " + transactionId);
                        startActivity(i);
//                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }).start();
        });


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

            new Thread(() -> db.notificationDao().insertNotification(notification)).start();
            NotificationCounter.increment();

            // Send badge update
            Intent intent = new Intent("com.fe.sdkparentapp");
            intent.putExtra("count", NotificationCounter.getCount());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);



        });

        // 🔄 Listen for badge count updates
        LocalBroadcastManager.getInstance(this).registerReceiver(
                badgeReceiver, new IntentFilter("com.fe.sdkparentapp")
        );

        logoutIcon.setOnClickListener(v -> {
            new Thread(() -> {
                // Clear your Room database
                db.notificationDao().clearAllNotifications();

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

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(badgeReceiver);
        super.onDestroy();
    }
}
