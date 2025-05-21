package com.fe.mylibrary.NetworkManager;

import android.content.Context;
import android.util.Log;

import com.fe.mylibrary.ApiClient;
import com.fe.mylibrary.Prefs;

import org.json.JSONObject;

public class NetworkManager {
    public interface RefreshCallback {
        void onComplete(boolean success);
    }

    public static void refreshToken(Context context, RefreshCallback callback) {
        String refreshToken = new Prefs(context).getRefreshToken();

        if (refreshToken == null) {
            callback.onComplete(false);
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("refresh", refreshToken);

            ApiClient apiClient = new ApiClient("https://ca17b8fefbb0c166dbde.free.beeceptor.com/", null);
            apiClient.post("refreshToken", jsonBody, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.e("TAG", "onSuccess: "+response);

                    try {
                        JSONObject json = new JSONObject(response);
                        String newBearer = json.getString("bearer_token");
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
