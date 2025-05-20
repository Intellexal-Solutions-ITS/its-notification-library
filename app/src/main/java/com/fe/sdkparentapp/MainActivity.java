package com.fe.sdkparentapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fe.mylibrary.NotificationModule;

public class MainActivity extends AppCompatActivity {


    EditText edtPhone;
    Button btnRegister;
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



        NotificationModule.initializeFirebase(this,"");


        edtPhone = findViewById(R.id.edtPhone);
        btnRegister = findViewById(R.id.btnRegister);


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
    }

    public static boolean isValidPhoneNumber(String phone) {
        // Define the regex for the phone number
        String phoneRegex = "^03[0-5]\\d{8}$";
        return phone.matches(phoneRegex);
    }

}