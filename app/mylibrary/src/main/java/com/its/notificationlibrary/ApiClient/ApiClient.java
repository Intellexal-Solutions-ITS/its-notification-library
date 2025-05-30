package com.its.notificationlibrary.ApiClient;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

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
    private final String fingerPrint;  // Optional - use for secure APIs

    public ApiClient( String authToken,String fingerPrint) {
        this.client = new OkHttpClient();
        this.baseUrl = ApiConstants.baseUrl;
        this.authToken = authToken;
        this.fingerPrint = fingerPrint;
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
//                .addHeader("X-Device-Fingerprint", fingerprint);

        if (authToken != null && !authToken.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + authToken);
        }
        if (fingerPrint != null && !fingerPrint.isEmpty()) {
            builder.addHeader("X-Device-Fingerprint", fingerPrint);
        }

        Log.d("API HEADER", String.valueOf(builder.build()));


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
