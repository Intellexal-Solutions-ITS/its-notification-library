package com.fe.sdkparentapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
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

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "notifications-db").build();

        // Load notifications in background
        new Thread(() -> {
            notificationList = db.notificationDao().getAllNotifications();

            runOnUiThread(() -> {
                adapter = new NotificationAdapter(notificationList);
                recyclerView.setAdapter(adapter);
                adapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(NotificationEntity notification) {

                        try {
                            Intent i = new Intent(getApplicationContext(), ViewNotificationActivity.class);
                            Log.d("NOTIFICATION PAYLOAD",  notification.payload);

                            JSONObject jo = new JSONObject(notification.payload);
                            i.putExtra("client_id",jo.getString("client_id"));
                            i.putExtra("transaction_id",jo.getString("transaction_id"));
                            i.putExtra("title",notification.title);
                            i.putExtra("message",notification.message);
                            String formattedDate = new SimpleDateFormat("MMM dd, yyyy - hh:mm:ss a", Locale.getDefault())
                                    .format(new Date(notification.datetime));
                            i.putExtra("datetime",formattedDate);
                            Log.e("NOTIFICATION DATA", jo.getString("client_id"));
                            Log.e("NOTIFICATION DATA", jo.getString("transaction_id"));
                            startActivity(i);

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            });
        }).start();


    }
}