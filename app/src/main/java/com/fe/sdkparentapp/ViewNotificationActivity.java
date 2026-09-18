package com.fe.sdkparentapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.its.notificationlibrary.Notifications.NotificationStatusCallback;
import com.its.notificationlibrary.Notifications.NotificationUtil;

public class ViewNotificationActivity extends AppCompatActivity {

    ProgressBar progressBar;
    LinearLayout contentLayout;
    TextView tvTitle, tvMessage, tvDateTime, tvstatus;

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
        progressBar = findViewById(R.id.progressBar);
        CardView cardNotification = findViewById(R.id.cardNotification);


        tvTitle = findViewById(R.id.tvtitle);
        tvMessage = findViewById(R.id.tvmessage);
        tvDateTime = findViewById(R.id.tvdattime);
        tvstatus = findViewById(R.id.tvstatus);

        Toolbar toolbar = findViewById(R.id.toolbarView);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Intent obj = getIntent();
        String clientID = obj.getStringExtra("client_id");
        String transactionID = obj.getStringExtra("transaction_id");
        String title = obj.getStringExtra("title");
        String message = obj.getStringExtra("message");
        String dt = obj.getStringExtra("datetime");
        String status = obj.getStringExtra("status");

        // Populate initial data
        tvTitle.setText(title);
        tvMessage.setText(message);
        tvDateTime.setText(dt);
        tvstatus.setText(status);

        boolean statusNeedsUpdate = status != null
                && (status.trim().equalsIgnoreCase("delivered") || status.trim().equalsIgnoreCase("sent"));
        boolean hasIds = clientID != null && !clientID.isEmpty()
                && transactionID != null && !transactionID.isEmpty();

        if (statusNeedsUpdate && hasIds) {

            NotificationUtil.updateNotificationStatus(this, "read", clientID, transactionID, new NotificationStatusCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.d("ViewNotification", "Status update successful");

                    new Thread(() -> {
                        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "notifications-db").build();
                        db.notificationDao().updateStatus(transactionID, "read");
                        Log.d("DB_UPDATE", "Updated status to 'read' for: " + clientID + " / " + transactionID);

                        runOnUiThread(() -> {
                            tvstatus.setText("read");
                            progressBar.setVisibility(View.GONE);
                            cardNotification.setVisibility(View.VISIBLE);
                            setResult(RESULT_OK);
                        });
                    }).start();
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        cardNotification.setVisibility(View.VISIBLE);
                        Log.e("ViewNotification", "Status update failed: " + e.getMessage());
                    });
                }
            });

        } else {
            // Show content without updating status
            progressBar.setVisibility(View.GONE);
            cardNotification.setVisibility(View.VISIBLE);
        }
    }
}
