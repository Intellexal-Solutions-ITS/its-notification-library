package com.its.notificationlibrary;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class Prefs {

    private SharedPreferences sharedPreferences;

    public Prefs(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveToken(String bearerToken, String refreshToken) {
        sharedPreferences.edit()
                .putString("bearer_token", bearerToken)
                .putString("refresh_token", refreshToken)
                .apply();
    }

    public String getBearerToken() {
        return sharedPreferences.getString("bearer_token", null);
    }

    public  String getRefreshToken() {
        return sharedPreferences.getString("refresh_token", null);
    }

    public void clearTokens() {
        sharedPreferences.edit().clear().apply();
    }
}
