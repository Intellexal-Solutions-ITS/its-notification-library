package com.its.notificationlibrary.Notifications;

import android.content.Context;
import android.util.Log;

import com.its.notificationlibrary.ApiClient;
import com.its.notificationlibrary.NetworkManager.NetworkManager;
import com.its.notificationlibrary.Prefs;

import org.json.JSONObject;

public class NotificationUtil {

    public static void updateNotificationStatus(Context ctx, String status) {
        // Always refresh token first
        NetworkManager.refreshToken(ctx, success -> {
            if (success) {
                String bearerToken = new Prefs(ctx).getBearerToken();

                ApiClient apiClient = new ApiClient("https://statusapp.free.beeceptor.com/api/", bearerToken);
                JSONObject jsonBody = new JSONObject();

                try {
                    jsonBody.put("status", status);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                apiClient.post("updateNotificationStatus", jsonBody, new ApiClient.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Log.d("API Response", "Status updated: " + response);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("API Error", "Update failed: " + e.getMessage());
                    }
                });
            } else {
                Log.e("Auth", "Token refresh failed, cannot send status.");
            }
        });
    }
}
