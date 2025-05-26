package com.its.notificationlibrary.NetworkManager;

import android.content.Context;
import android.util.Log;

import com.its.notificationlibrary.ApiClient.ApiClient;
import com.its.notificationlibrary.ApiClient.ApiConstants;
import com.its.notificationlibrary.Prefs;

import org.json.JSONObject;

public class NetworkManager {
    public interface RefreshCallback {
        void onComplete(boolean success);
    }

    public static void refreshToken(Context context, RefreshCallback callback) {
        String refreshToken = new Prefs(context).getRefreshToken();
        String authToken = new Prefs(context).getBearerToken();
        String fingerprint = new Prefs(context).getFingerprint();

        if (refreshToken == null) {
            callback.onComplete(false);
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("refresh_token", refreshToken);

            ApiClient apiClient = new ApiClient(authToken,fingerprint);
            Log.e("REQUEST BODY", String.valueOf(jsonBody));

            apiClient.post(ApiConstants.refreshToken, jsonBody, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.e("TAG", "onSuccess: "+response);

                    try {
                        JSONObject json = new JSONObject(response);
                        String newBearer = json.getString("access_token");
                        String newRefresh = json.getString("refresh_token");

                        new Prefs(context).saveToken(newBearer, newRefresh);
                        callback.onComplete(true);
                    } catch (Exception e) {
                        callback.onComplete(false);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onComplete(false);
                    Log.e("TAG", "onFailure: "+e.getMessage());
                }
            });
        } catch (Exception e) {
            callback.onComplete(false);
        }
    }
}
