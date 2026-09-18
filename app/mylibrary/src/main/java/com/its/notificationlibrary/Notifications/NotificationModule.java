package com.its.notificationlibrary.Notifications;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.its.notificationlibrary.ApiClient.ApiClient;
import com.its.notificationlibrary.ApiClient.ApiConstants;
import com.its.notificationlibrary.NetworkManager.NetworkManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.its.notificationlibrary.Storage.Prefs;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import okhttp3.HttpUrl;

public class NotificationModule {

    public static void initializeFirebase(Context context,String apiKey, String client_id,String api_key) {



        try {
            FirebaseOptions firebaseOptions =new FirebaseOptions.Builder()
                    .setApplicationId("1:992052778259:android:490847258e7d584274f6e6")
                    .setApiKey(apiKey)
                    .setProjectId("itsomni-notification")
                    .setGcmSenderId("992052778259")
                    .build();


                    try {
                        FirebaseApp.initializeApp(context, firebaseOptions);
                        String fingerprint = UUID.randomUUID().toString();
                        new Prefs(context).saveFingerPrint(fingerprint);
                        getFcmToken(context, client_id, api_key);
                    }
                    catch (Exception ex){
                        Log.e(TAG, "initializeFirebase: "+ex.getMessage() );
                    }




        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static final String TAG = "LibraryTAG";

    public static void getFcmToken(Context ctx, String client_id,String api_key) {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(result ->{

            if (result.isSuccessful()) {
                // Authentication was successful
                Log.d(TAG, "Anonymous sign-in successful");

                // Retrieve the current user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(task -> {
                                if (!task.isSuccessful()) {
                                    Log.e(TAG, "Fetching FCM token failed", task.getException());
                                    return;
                                }

                                // Get the FCM token
                                String token = task.getResult();
                                String packageName = ctx.getPackageName();

                                initSDKCall(ctx,token,packageName,client_id,api_key);
                            });
                }
            } else {
                // If sign-in fails, log the error
                Log.e(TAG, "Anonymous sign-in failed", result.getException());
            }
        });

    }


    private static void initSDKCall(Context ctx,String device_token, String packageName, String client_id, String api_key){
        Context appContext = ctx.getApplicationContext();
        String fingerprint = new Prefs(appContext).getFingerprint();

        ApiClient apiClient = new ApiClient( null,fingerprint);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("app_id", packageName);
            jsonBody.put("device_token", device_token);
            jsonBody.put("fcm_token", device_token);
            jsonBody.put("client_id", client_id);
            jsonBody.put("api_key", api_key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        apiClient.post(ApiConstants.issueToken, jsonBody, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject  jsonResponse = new JSONObject(response);

                        Log.d("NotificationModule", "Device registration succeeded");
                        String bearer = jsonResponse.optString("access_token", "");
                        String refresh = jsonResponse.optString("refresh_token", "");
                        int expire_in = jsonResponse.getInt("expires_in");
                    Date now = new Date();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String tokenDate = formatter.format(now);


                    Prefs secureStorage = new Prefs(ctx);
                        secureStorage.saveToken(bearer, refresh,expire_in,tokenDate);


                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("API Error", "Failed: " + e.getMessage());
            }
        });
    }







    public static void registerUser(Context ctx, String userPhone, String userName, String userEmail) {
        // Always refresh token first
        NetworkManager.refreshToken(ctx, success -> {
            if (success) {
                String bearerToken = new Prefs(ctx).getBearerToken();
                String fingerprint = new Prefs(ctx).getFingerprint();

                ApiClient apiClient = new ApiClient( bearerToken,fingerprint);

                JSONObject jsonBody = new JSONObject();

                try {
                    jsonBody.put("customer_name", userName);
                    jsonBody.put("email", userEmail);
                    jsonBody.put("phone_number", userPhone);

                    if (jsonBody.length() > 0) {
                        apiClient.post(ApiConstants.deviceRegister, jsonBody, new ApiClient.ApiCallback() {
                            @Override
                            public void onSuccess(String response) {
                                Log.d("NotificationModule", "User registered successfully");
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("API Error", "Registration failed: " + e.getMessage());
                            }
                        });
                    } else {
                        Log.e("API Error", "No valid data provided for registration.");
                    }

                } catch (Exception e) {
                    Log.e("API Error", "Failed to build JSON body: " + e.getMessage());
                }
            } else {
                Log.e("Auth", "Token refresh failed, cannot register user.");
            }
        });
    }


    private static NotificationClickListener clickListener;

    public static void setNotificationClickListener(NotificationClickListener listener) {
        clickListener = listener;
    }
    public static void handleNotificationIntent(Intent intent) {
        if (intent != null && intent.hasExtra("id") && clickListener != null) {
            String id = intent.getStringExtra("id");
            clickListener.onNotificationClicked(id);
        }
    }

    public interface NotificationCallBack {
        void onSuccess(String responseBody); // or pass parsed data if needed
        void onError(String errorMessage);
    }

    public static void getNotifications(Context ctx, int page, int pageSize, NotificationCallBack callback) {
        NetworkManager.refreshToken(ctx, success -> {
            if (success) {
                String bearerToken = new Prefs(ctx).getBearerToken();
                String fingerprint = new Prefs(ctx).getFingerprint();

                ApiClient apiClient = new ApiClient(bearerToken, fingerprint);

                HttpUrl url = HttpUrl.parse(ApiConstants.notificationList)
                        .newBuilder()
                        .addQueryParameter("channel", "1")
                        .addQueryParameter("page", String.valueOf(page))
                        .addQueryParameter("page_size", String.valueOf(pageSize))
                        .build();

                apiClient.get(url, new ApiClient.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("TransactionAPI", "Failed to fetch notifications: " + e.getMessage());
                        callback.onError(e.getMessage());
                    }
                });
            } else {
                Log.e("TransactionAPI", "Token refresh failed");
                callback.onError("Token refresh failed");
            }
        });
    }

}
