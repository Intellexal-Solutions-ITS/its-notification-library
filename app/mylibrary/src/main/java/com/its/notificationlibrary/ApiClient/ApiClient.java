package com.its.notificationlibrary.ApiClient;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CertificatePinner;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Pins the itsfcm.its.com.pk leaf certificate plus its issuing CA (Sectigo Public
    // Server Authentication CA DV R36), so the app keeps working across routine leaf
    // cert renewal but still rejects a MITM using a different, untrusted CA.
    public static final CertificatePinner CERT_PINNER = new CertificatePinner.Builder()
            .add("itsfcm.its.com.pk", "sha256/8R7Tpg0boGFRcYs90oN8ulbyDEhmsq3wVzgMI6z3wwE=")
            .add("itsfcm.its.com.pk", "sha256/a9khLOZJxlnJyrxstg/P+seiDCm+Yf3OsrXyFocBaI0=")
            .build();

    private final OkHttpClient client;
    private final String baseUrl;
    private final String authToken;  // Optional - use for secure APIs
    private final String fingerPrint;  // Optional - use for secure APIs

    public ApiClient(String authToken, String fingerPrint) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .certificatePinner(CERT_PINNER)
                .build();
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

        Request.Builder builder = authorizedBuilder(baseUrl + endpoint)
                .post(requestBody);

        execute(builder.build(), callback);
    }

    /** @param url full request URL, including any query parameters. */
    public void get(HttpUrl url, ApiCallback callback) {
        Request.Builder builder = authorizedBuilder(url.toString())
                .get();

        execute(builder.build(), callback);
    }

    private Request.Builder authorizedBuilder(String url) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Connection", "keep-alive");

        if (authToken != null && !authToken.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + authToken);
        }
        if (fingerPrint != null && !fingerPrint.isEmpty()) {
            builder.addHeader("X-Device-Fingerprint", fingerPrint);
        }

        return builder;
    }

    private void execute(Request request, ApiCallback callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "API call failed: " + request.url(), e);
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    callback.onFailure(new IOException("Unexpected code " + response.code() + ": " + errorBody));
                } else {
                    String responseBody = response.body().string();
                    callback.onSuccess(responseBody);
                }
            }
        });
    }
}
