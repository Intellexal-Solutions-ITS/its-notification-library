package com.fe.mylibrary.Model;

import org.json.JSONObject;

public class NotificationModel {
    private final String title;
    private final String message;
    private final JSONObject payload;

    public NotificationModel(String title, String message, JSONObject payload) {
        this.title = title;
        this.message = message;
        this.payload = payload;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public JSONObject getPayload() {
        return payload;
    }
}
