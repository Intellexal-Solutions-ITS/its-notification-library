package com.fe.mylibrary;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class NotificationModule {

    public static void initializeFirebase(Context context,String apiKey) {



        try {
            FirebaseOptions firebaseOptions =new FirebaseOptions.Builder()
                    .setApplicationId("1:93074134066:android:2a4baddf6c4e6c4659982b")
                    .setApiKey(apiKey)
                    .setProjectId("push-notification-sdk-c8f7f")
                    .setGcmSenderId("93074134066")
                    .build();


                    try {
                        FirebaseApp.initializeApp(context, firebaseOptions);
                        getFcmToken(context);
                    }
                    catch (Exception ex){
                        Log.e(TAG, "initializeFirebase: "+ex.getMessage() );
                    }




        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static final String TAG = "LibraryTAG";

    public static void getFcmToken(Context ctx) {
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

                                initSDKCall(ctx,token,userId,packageName,"hello");
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


    private static void initSDKCall(Context ctx,String device_token, String auth_token, String app_id, String secret_key){
        ApiClient apiClient = new ApiClient("https://ca065bfd45a463302f9c.free.beeceptor.com/api/", "");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("app_id", app_id);
            jsonBody.put("device_token", device_token);
            jsonBody.put("fcm_auth_token", auth_token);
            jsonBody.put("secrect_key", secret_key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        apiClient.post("initSDK", jsonBody, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("API Response", response);
                try {
                    JSONObject  jsonResponse = new JSONObject(response);
                    String status = jsonResponse.optString("status", "");


                    if ("success".equalsIgnoreCase(status)) {
                        Log.d("API Response", "Success: " + response);
                        String bearer = jsonResponse.optString("bearer_token", "");
                        String refresh = jsonResponse.optString("refresh_token", "");
                        Prefs secureStorage = new Prefs(ctx);
                        secureStorage.saveToken(bearer, refresh);
                    } else {
                        Log.e("API Response", "Status is not success");
                    }

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







    public static void registerUser(Context ctx,String userPhone, String userName, String userEmail) {


        Prefs secureStorage = new Prefs(ctx);
        String bearerToken = secureStorage.getBearerToken();
        ApiClient apiClient = new ApiClient("https://caab744a8f5b7df61638.free.beeceptor.com/api/", bearerToken);

        // Create a JSON object to hold the parameters
        JSONObject jsonBody = new JSONObject();

        try {
            // Add the values to the JSON object if they are not null
            if (userPhone != null && !userPhone.isEmpty()) {
                jsonBody.put("phone", userPhone);
            }
            if (userName != null && !userName.isEmpty()) {
                jsonBody.put("username", userName);
            }
            if (userEmail != null && !userEmail.isEmpty()) {
                jsonBody.put("email", userEmail);
            }

            // Perform the API call if any of the parameters exist
            if (jsonBody.length() > 0) {
                apiClient.post("register", jsonBody, new ApiClient.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        // Handle success
                        Log.d("API Response", "User registered: " + response);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Handle failure
                        Log.e("API Error", "Registration failed: " + e.getMessage());
                    }
                });
            } else {
                Log.e("API Error", "No valid data provided for registration.");
            }

        } catch (Exception e) {
            Log.e("API Error", "Failed to build JSON body: " + e.getMessage());
        }
    }


}
