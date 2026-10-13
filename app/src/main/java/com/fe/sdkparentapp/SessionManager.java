package com.fe.sdkparentapp;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS_NAME = "session_prefs";
    private static final String KEY_PHONE = "phone";

    public static void saveSession(Context context, String phone) {
        prefs(context).edit().putString(KEY_PHONE, phone).apply();
    }

    public static String getPhone(Context context) {
        return prefs(context).getString(KEY_PHONE, null);
    }

    public static boolean isLoggedIn(Context context) {
        String phone = getPhone(context);
        return phone != null && !phone.isEmpty();
    }

    public static void clearSession(Context context) {
        prefs(context).edit().clear().apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
