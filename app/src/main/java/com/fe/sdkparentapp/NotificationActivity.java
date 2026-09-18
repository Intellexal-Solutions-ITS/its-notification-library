package com.fe.sdkparentapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.its.notificationlibrary.Notifications.NotificationModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> viewNotificationLauncher;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private NotificationAdapter adapter;
    private List<NotificationEntity> notificationList = new ArrayList<>();
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE); // Show loader

        viewNotificationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        refreshNotifications(); // Reload data from DB after status update
                    }
                });

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "notifications-db").build();

        NotificationModule.getNotifications(getApplicationContext(), 1, 20, new NotificationModule.NotificationCallBack() {
            @Override
            public void onSuccess(String responseBody) {
                List<NotificationEntity> apiList = parseApiResponse(responseBody);

                new Thread(() -> {
                    for (NotificationEntity apiItem : apiList) {
                        NotificationEntity existing = db.notificationDao().getNotificationById(apiItem.notification_id);
                        if (existing == null) {
                            db.notificationDao().insertNotification(apiItem);
                        }
                    }

                    List<NotificationEntity> updatedList = db.notificationDao().getAllNotifications();
                    Collections.reverse(updatedList);

                    runOnUiThread(() -> {
                        adapter = new NotificationAdapter(updatedList);
                        adapter.setOnItemClickListener(notification -> {
                            Log.d("NotificationAdapter", "Item clicked: " + notification.notification_id);

                            if (notification.payload == null || notification.payload.trim().isEmpty()) {
                                Log.e("NotificationActivity", "Payload is null or empty");
                                return;
                            }
                            Log.e("Notification Payload", "Payload is" + notification.payload);
                            Log.e("Notification Payload", "Notification ID is" + notification.notification_id);

                            try {
                                Intent i = new Intent(getApplicationContext(), ViewNotificationActivity.class);
                                JSONObject jo = new JSONObject(notification.payload);

                                if (jo.has("client_id")) {
                                    i.putExtra("client_id", jo.getString("client_id"));
                                } else if (jo.has("ClientId")) {
                                    i.putExtra("client_id", jo.getString("ClientId"));
                                }

                                i.putExtra("transaction_id", notification.notification_id);
                                i.putExtra("title", notification.title);
                                i.putExtra("message", notification.message);

                                String formattedDate = new SimpleDateFormat("MMM dd, yyyy - hh:mm:ss a", Locale.getDefault())
                                        .format(new Date(notification.datetime));
                                i.putExtra("datetime", formattedDate);
                                i.putExtra("status", notification.status);

                                viewNotificationLauncher.launch(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });
                        recyclerView.setAdapter(adapter);
                        progressBar.setVisibility(View.GONE); // Hide loader
                    });
                }).start();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("TransactionAPI", "Error: " + errorMessage);
                runOnUiThread(() -> progressBar.setVisibility(View.GONE)); // Hide loader on error
            }
        });
    }

    private List<NotificationEntity> parseApiResponse(String responseBody) {
        List<NotificationEntity> apiList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            if (jsonObject.optString("status").equals("success")) {
                JSONArray dataArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject obj = dataArray.getJSONObject(i);
                    NotificationEntity notification = new NotificationEntity();

                    notification.notification_id = String.valueOf(obj.optInt("Id"));
                    notification.title = obj.optString("Title", "");
                    notification.message = obj.optString("MsgData", "");
                    notification.status = obj.optString("Status", "");
                    notification.datetime = parseDatetime(obj.optString("CreatedDatetime"));
                    notification.payload = obj.toString();

                    apiList.add(notification);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return apiList;
    }

    private long parseDatetime(String datetime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
            Date date = sdf.parse(datetime);
            return date != null ? date.getTime() : System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
            return System.currentTimeMillis();
        }
    }


    private void refreshNotifications() {
        new Thread(() -> {
            List<NotificationEntity> updatedList = db.notificationDao().getAllNotifications();
            Collections.reverse(updatedList);

            runOnUiThread(() -> {
                adapter.updateData(updatedList); // You’ll need to implement this in adapter
            });
        }).start();
    }
}
