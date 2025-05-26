package com.fe.sdkparentapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.Room;

import com.its.notificationlibrary.Notifications.NotificationHandlerRegistry;
import com.its.notificationlibrary.Notifications.NotificationListener;
import com.its.notificationlibrary.Model.NotificationModel;
import com.its.notificationlibrary.Notifications.NotificationModule;

public class MainActivity extends AppCompatActivity {
    TextView badgeTextView;

    EditText edtPhone;
    Button btnRegister;

    RelativeLayout notification_container;

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        requestNotificationPermission();
        NotificationModule.initializeFirebase(this,"AIzaSyD__p1BKi0hDE4B6dPsaF1X0bHHtBK2ujs", "8C4AB43E-1599-43E6-8C6A-B086E21E97F8", "d1f8c1e2-4b7a-4a95-9e2f-0b75f84e6d5a");

        LocalBroadcastManager.getInstance(this).registerReceiver(
                badgeReceiver, new IntentFilter("com.fe.sdkparentapp")
        );
        edtPhone = findViewById(R.id.edtPhone);
        btnRegister = findViewById(R.id.btnRegister);
        badgeTextView = findViewById(R.id.badge_count);
        notification_container = findViewById(R.id.notification_container);


        notification_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,NotificationActivity.class));
//                Toast.makeText(getApplicationContext(),"Tapped",Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValidPhoneNumber(edtPhone.getText().toString().trim())){
                    NotificationModule.registerUser(MainActivity.this,edtPhone.getText().toString(),null,null);
                }
                else{
                    Toast.makeText(MainActivity.this,"Invalid Phone",Toast.LENGTH_SHORT).show();
                }
            }
        });

        NotificationHandlerRegistry.setListener(new NotificationListener() {
            @Override
            public void onNotificationReceived(NotificationModel remoteMessage) {
                Log.d("HostApp", "Title: " + remoteMessage.getTitle());
                Log.d("HostApp", "Message: " + remoteMessage.getMessage());
                Log.d("HostApp", "Payload: " + remoteMessage.getPayload().toString());
                AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "notifications-db")
                        .fallbackToDestructiveMigration()
                        .build();

                NotificationEntity notification = new NotificationEntity();
                notification.title = remoteMessage.getTitle();
                notification.message = remoteMessage.getMessage();
                notification.payload = remoteMessage.getPayload().toString();
                notification.status = "delivered";
                notification.datetime = System.currentTimeMillis();

                new Thread(() -> db.notificationDao().insertNotification(notification)).start();
                NotificationCounter.increment();

                // Broadcast or directly update the badge view in UI
                Intent intent = new Intent("com.fe.sdkparentapp");
                intent.putExtra("count", NotificationCounter.getCount());
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);


            }
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
    public static boolean isValidPhoneNumber(String phone) {
        // Define the regex for the phone number
        String phoneRegex = "^03[0-5]\\d{8}$";
        return phone.matches(phoneRegex);
    }

    private final BroadcastReceiver badgeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int count = intent.getIntExtra("count", 0);
            badgeTextView.setText(String.valueOf(count));
            badgeTextView.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        }
    };

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(badgeReceiver);
        super.onDestroy();
    }


    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }
}