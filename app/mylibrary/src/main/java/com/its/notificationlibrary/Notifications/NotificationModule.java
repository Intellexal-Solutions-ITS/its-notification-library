package com.its.notificationlibrary.Notifications;

import android.content.Context;
import android.util.Log;

import com.its.notificationlibrary.ApiClient.ApiClient;
import com.its.notificationlibrary.ApiClient.ApiConstants;
import com.its.notificationlibrary.NetworkManager.NetworkManager;
import com.its.notificationlibrary.Prefs;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

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
                        new Prefs(context).saveFingerPrint(fingerprint, context);
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
                    // Get the unique identifier of the user
                    String userId = user.getUid(); // Unique identifier for the user

                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(task -> {
                                if (!task.isSuccessful()) {
                                    Log.e(TAG, "Fetching FCM token failed", task.getException());
                                    return;
                                }

                                // Get the FCM token
                                String token = task.getResult();
                                Log.d(TAG, "FCM Token: " + token);
                                String packageName = ctx.getPackageName();
                                Log.d(TAG, "Package Name: " + packageName);

                                initSDKCall(ctx,token,packageName,client_id,api_key);
                                // You can store this token or pass it to your library's consumer
                            });
                    // Log or use the user info as needed
                    Log.d(TAG, "User ID: " + userId);

                }
            } else {
                // If sign-in fails, log the error
                Log.e(TAG, "Anonymous sign-in failed", result.getException());
            }
        });

    }


    private static void initSDKCall(Context ctx,String device_token, String packageName, String client_id, String api_key){
        String fingerprint = new Prefs(ctx).getFingerprint();

        ApiClient apiClient = new ApiClient( null,fingerprint);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("app_id", packageName);
            jsonBody.put("device_token", device_token);
            jsonBody.put("fcm_token", "1234");
            jsonBody.put("client_id", client_id);
            jsonBody.put("api_key", api_key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Request Body", String.valueOf(jsonBody));

        apiClient.post(ApiConstants.issueToken, jsonBody, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("API Response", response);
                try {
                    JSONObject  jsonResponse = new JSONObject(response);
                    String status = jsonResponse.optString("status", "");

                        Log.d("API Response", "Success: " + response);
                        String bearer = jsonResponse.optString("access_token", "");
                        String refresh = jsonResponse.optString("refresh_token", "");
                        Prefs secureStorage = new Prefs(ctx);
                        secureStorage.saveToken(bearer, refresh);


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
//                    if (userPhone != null && !userPhone.isEmpty()) {
//                        jsonBody.put("customer_name", userName);
                        jsonBody.put("customer_name", "sumair yaseen");
//                    }
//                    if (userName != null && !userName.isEmpty()) {
//                        jsonBody.put("email", userEmail);
                        jsonBody.put("email", "sumairbhutto09@gmail.com");
//                    }
//                    if (userEmail != null && !userEmail.isEmpty()) {
                        jsonBody.put("phone_number", userPhone);
//                        jsonBody.put("phone_number", "03059423919");
//                    }

                    if (jsonBody.length() > 0) {
                        apiClient.post(ApiConstants.deviceRegister, jsonBody, new ApiClient.ApiCallback() {
                            @Override
                            public void onSuccess(String response) {
                                Log.d("API Response", "User registered: " + response);
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



}
