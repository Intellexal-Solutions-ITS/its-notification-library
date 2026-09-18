package com.its.notificationlibrary.Notifications;

import android.content.Context;
import android.util.Log;

import com.its.notificationlibrary.ApiClient.ApiClient;
import com.its.notificationlibrary.ApiClient.ApiConstants;
import com.its.notificationlibrary.NetworkManager.NetworkManager;
import com.its.notificationlibrary.Storage.Prefs;

import org.json.JSONObject;

public class NotificationUtil {

    public static void updateNotificationStatus(Context ctx, String status, String client_id, String transaction_id,
                                                NotificationStatusCallback callback) {

        NetworkManager.refreshToken(ctx, success -> {
            if (success) {
                String bearerToken = new Prefs(ctx).getBearerToken();
                String fingerprint = new Prefs(ctx).getFingerprint();

                ApiClient apiClient = new ApiClient(bearerToken, fingerprint);
                JSONObject jsonBody = new JSONObject();

                try {
                    jsonBody.put("transaction_id", transaction_id);
                    jsonBody.put("client_id", client_id);
                    jsonBody.put("status", status);

                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                    return;
                }

                apiClient.post(ApiConstants.updateNotificationStatus, jsonBody, new ApiClient.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Log.d("NotificationUtil", "Status updated");
                        if (callback != null) {
                            callback.onSuccess(response);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("NotificationUtil", "Update failed: " + e.getMessage());
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
            } else {
                Log.e("Auth", "Token refresh failed, cannot send status.");
                if (callback != null) {
                    callback.onFailure(new Exception("Token refresh failed"));
                }
            }
        });
    }
}
