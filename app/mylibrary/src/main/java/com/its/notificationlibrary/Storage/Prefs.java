package com.its.notificationlibrary.Storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Prefs {

    private static final String TAG = "Prefs";

    private SharedPreferences sharedPreferences;
    private boolean encrypted;

    public Prefs(Context context) {
        try {
            sharedPreferences = createEncryptedPrefs(context);
            encrypted = true;
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Failed to create encrypted storage, deleting corrupt key and retrying", e);

            // This likely means the key is invalid/corrupted. Delete and retry once.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.deleteSharedPreferences("secure_prefs");
            }

            try {
                sharedPreferences = createEncryptedPrefs(context);
                encrypted = true;
            } catch (Exception innerEx) {
                Log.e(TAG, "Encrypted storage unavailable on this device; falling back to " +
                        "UNENCRYPTED SharedPreferences. Auth tokens will be stored in plaintext.", innerEx);
                sharedPreferences = context.getSharedPreferences("fallback_prefs", Context.MODE_PRIVATE);
                encrypted = false;
            }
        }
    }

    private static SharedPreferences createEncryptedPrefs(Context context) throws GeneralSecurityException, IOException {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                context,
                "secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    /** Whether tokens are currently stored in encrypted storage, or plaintext fallback. */
    public boolean isEncrypted() {
        return encrypted;
    }

    public void saveToken(String bearerToken, String refreshToken,int expireIn,String tokenTime) {
        sharedPreferences.edit()
                .putString("bearer_token", bearerToken)
                .putString("refresh_token", refreshToken)
                .putInt("expire_in",expireIn)
                .putString("token_time",tokenTime)
                .apply();
    }



    public void saveFingerPrint(String fingerPrint) {
        String existing = getFingerprint();
        if (existing == null || existing.isEmpty()) {
            sharedPreferences.edit()
                    .putString("device_fingerprint", fingerPrint)
                    .apply();
        }
    }



    public String getBearerToken() {
        return sharedPreferences.getString("bearer_token", null);
    }

    public  String getRefreshToken() {
        return sharedPreferences.getString("refresh_token", null);
    }

    public String getFingerprint() {
        return sharedPreferences.getString("device_fingerprint", "");
    }

    public int getExpireIn() {
        return sharedPreferences.getInt("expire_in", 0);
    }

    public String getTokenTime() {
        return sharedPreferences.getString("token_time", null);
    }

    public void clearTokens() {
        sharedPreferences.edit().clear().apply();
    }
}
