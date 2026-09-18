package com.fe.sdkparentapp;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.Room;

import com.its.notificationlibrary.Notifications.NotificationModule;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    EditText edtPhone;
    Button btnRegister;


    ProgressBar progressInsideButton;
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
        NotificationModule.initializeFirebase(
                this,
                "AIzaSyD__p1BKi0hDE4B6dPsaF1X0bHHtBK2ujs",
                "8C4AB43E-1599-43E6-8C6A-B086E21E97F8".toLowerCase(),
                "d1f8c1e2-4b7a-4a95-9e2f-0b75f84e6d5a");
        requestNotificationPermission();
        progressInsideButton = findViewById(R.id.progressInsideButton);
        edtPhone = findViewById(R.id.edtPhone);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = edtPhone.getText().toString().trim();
                hideKeyboard(v);
                if (isValidPhoneNumber(phone)) {
                    // Disable and show loader inside button
                    btnRegister.setEnabled(false);
                    btnRegister.setText(""); // Hide text
                    progressInsideButton.setVisibility(View.VISIBLE);

                    // Simulate network call or registration
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000); // Simulate delay
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(() -> {
                            // Restore button state
                            progressInsideButton.setVisibility(View.GONE);
                            btnRegister.setText("Register");
                            btnRegister.setEnabled(true);
                            // Continue registration
                            NotificationModule.registerUser(MainActivity.this, phone, null, null);
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            intent.putExtra("phone", phone); // Pass phone number
                            startActivity(intent);
                            edtPhone.setText("");
                            finish();
                        });
                    }).start();
                } else {
                    Toast.makeText(MainActivity.this, "Invalid Phone", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public static boolean isValidPhoneNumber(String phone) {
        // Define the regex for the phone number
        String phoneRegex = "^03[0-5]\\d{8}$";
        return phone.matches(phoneRegex);
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

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}