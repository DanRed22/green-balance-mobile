package com.example.greenbalance.utility;

import org.json.JSONObject;

public class ApiResponse {
    private final boolean success;
    private final boolean error;
    private final String message;
    private JSONObject body;

    public ApiResponse(boolean success, boolean error, String message) {
        this.success = success;
        this.error = error;
        this.message = message;
    }

    public ApiResponse(boolean success, boolean error, String message, JSONObject o) {
        this.success = success;
        this.error = error;
        this.message = message;
        this.body = o;
    }

    public boolean isSuccessful() {
        return success && !error;
    }

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public JSONObject getBody() {
        return body;
    }


}
