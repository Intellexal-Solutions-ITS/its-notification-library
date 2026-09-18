package com.its.notificationlibrary.NetworkManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.its.notificationlibrary.ApiClient.ApiClient;
import com.its.notificationlibrary.ApiClient.ApiConstants;
import com.its.notificationlibrary.Storage.Prefs;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NetworkManager {
    public interface RefreshCallback {
        void onComplete(boolean success);
    }


    public static void refreshToken(Context context, RefreshCallback callback) {
        String refreshToken = new Prefs(context).getRefreshToken();
        String authToken = new Prefs(context).getBearerToken();
        String fingerprint = new Prefs(context).getFingerprint();
        String tokenTime = new Prefs(context).getTokenTime();


        Date now = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        boolean needsRefresh = true;
        if (tokenTime != null) {
            try {
                Date parsedTokenDate = formatter.parse(tokenTime);
                long diffInMillis = now.getTime() - parsedTokenDate.getTime();
                long diffInMinutes = diffInMillis / (60 * 1000);
                needsRefresh = diffInMinutes >= 50;
            } catch (ParseException e) {
                e.printStackTrace();
                needsRefresh = true;
            }
        }

        if (!needsRefresh) {
            callback.onComplete(true);
            return;
        }

        if (refreshToken == null) {
            callback.onComplete(false);
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("refresh_token", refreshToken);

            ApiClient apiClient = new ApiClient(authToken, fingerprint);

            apiClient.post(ApiConstants.refreshToken, jsonBody, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.d("NetworkManager", "Token refresh succeeded");

                    try {
                        JSONObject json = new JSONObject(response);
                        String newBearer = json.getString("access_token");
                        String newRefresh = json.getString("refresh_token");
                        int expire_in = json.getInt("expires_in");
                        String tokenDate = formatter.format(new Date());

                        new Prefs(context).saveToken(newBearer, newRefresh, expire_in, tokenDate);
                        callback.onComplete(true);
                    } catch (Exception e) {
                        callback.onComplete(false);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onComplete(false);
                    Log.e("NetworkManager", "Token refresh failed: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            callback.onComplete(false);
        }
    }
}
