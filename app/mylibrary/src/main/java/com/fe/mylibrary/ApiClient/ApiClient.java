package com.fe.mylibrary;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final String baseUrl;
    private final String authToken;  // Optional - use for secure APIs

    public ApiClient(String baseUrl, String authToken) {
        this.client = new OkHttpClient();
        this.baseUrl = baseUrl;
        this.authToken = authToken;
    }

    public interface ApiCallback {
        void onSuccess(String response);
        void onFailure(Exception e);
    }

    public void post(String endpoint, JSONObject body, ApiCallback callback) {
        RequestBody requestBody = RequestBody.create(body.toString(), JSON);

        Request.Builder builder = new Request.Builder()
                .url(baseUrl + endpoint)
                .post(requestBody)
                .addHeader("Content-Type", "application/json");

        if (authToken != null && !authToken.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + authToken);
        }

        Request request = builder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "API call failed", e);
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure(new IOException("Unexpected code " + response));
                } else {
                    String responseBody = response.body().string();
                    callback.onSuccess(responseBody);
                }
            }
        });
    }
}
