package com.fe.sdkparentapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.its.notificationlibrary.Notifications.NotificationUtil;

public class ViewNotificationActivity extends AppCompatActivity {


    TextView tvTitle,tvMessage,tvDateTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent obj =getIntent();
        String clientID = obj.getStringExtra("client_id");
        String transactionID = obj.getStringExtra("transaction_id");
        String title = obj.getStringExtra("title");
        String message = obj.getStringExtra("message");
        String dt = obj.getStringExtra("datetime");
        Toolbar toolbar = findViewById(R.id.toolbarView);
        setSupportActionBar(toolbar);


        tvTitle = findViewById(R.id.tvtitle);
        tvMessage = findViewById(R.id.tvmessage);
        tvDateTime = findViewById(R.id.tvdattime);
        tvTitle.setText(title);
        tvMessage.setText(message);
        tvDateTime.setText(dt);



        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        NotificationUtil.updateNotificationStatus(this,"read", clientID, transactionID);

    }
}