package com.example.greenbalance.utility;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.greenbalance.properties.Configuration;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class User {

    private static final String BASE_URL = Configuration.API_URL;

    /**
     * Creates a new user account in the backend and returns a CompletableFuture.
     *
     * @param context   Application context for Toast and API request
     * @param userId    The Firebase-generated user ID
     * @param username  The user's chosen username
     * @param email     The user's email
     * @return A CompletableFuture representing the success or failure of the API call.
     */
    public static CompletableFuture<ApiResponse> createUserAccount(Context context, String userId, String username, String email){
        // This will hold the result of the API call
        CompletableFuture<ApiResponse> future = new CompletableFuture<>();

        // Build the JSON payload
        JSONObject userObject = new JSONObject();
        try {
            userObject.put("id", userId);
            userObject.put("username", username);
            userObject.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
            future.complete(new ApiResponse(false, true, "Error creating user account"));
            return future;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL + "/v1/users",
                userObject,
                response -> {
                    // Handle success
                    Log.d("[User] API Response", "Response: " + response);
                    ApiResponse apiResponse = new ApiResponse(true, false, "User created successfully!", response);
                    future.complete(apiResponse); // Complete the future with success result
                },
                error -> {
                    // Handle error
                    Log.e("[User] API Error", "Error: " + error.getMessage());
                    ApiResponse apiResponse = new ApiResponse(false, true, parseError(error));
                    future.complete(apiResponse); // Complete the future with error result
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("x-api-key", Configuration.API_KEY);  // Add API key to headers
                Log.d("API Request Headers", "Headers: " + headers);  // Log headers here
                return headers;
            }
        };

        // Add the request to the queue for execution
        requestQueue.add(request);
        // Log the URL and the request to ensure it's being sent
        Log.d("[User] API Request", "Sending request to: " + BASE_URL);
        Log.d("[User] API Request", "Payload: " + userObject.toString());

        return future;
    }

    public static CompletableFuture<ApiResponse> loginUser(Context context, String email) {
        CompletableFuture<ApiResponse> future = new CompletableFuture<>();
        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL + "/v1/auth/login",
                body,
        response -> {
            JSONObject dataResponse = new JSONObject();
            String username = null;
            Log.d("API Result Response", "Response: " + response.toString());

            if(response.has("data")){
                try {
                    JSONObject jsonData = new JSONObject(response.toString());
                    jsonData = jsonData.getJSONObject("data");
                    username = jsonData.getString("username");
                    dataResponse = jsonData;
                }catch (JSONException e){
                    throw new RuntimeException(e);
                }
            }else{
                Log.d("API Result Response", "WAY TOKEN: ");
            }
            Log.d("[User] API Result Response", dataResponse.toString());
            ApiResponse apiResponse = new ApiResponse(true, false, "User logged in successfully!", dataResponse);
            future.complete(apiResponse);
        },
                error ->{
                    Log.e("[User] API Error", "Error: " + error.toString());
                    ApiResponse apiResponse = new ApiResponse(false, true, parseError(error));
                    future.complete(apiResponse);

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("x-api-key", Configuration.API_KEY);  // Add API key to headers
                Log.d("API Request Headers", "Headers: " + headers);  // Log headers here
                return headers;
            }
        };


        requestQueue.add(request);
        return future;

    }

    /**
     * Parses an error from Volley and returns a user-friendly message.
     *
     * @param error VolleyError object
     * @return Parsed error message
     */
    private static String parseError(VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                return new JSONObject(new String(error.networkResponse.data)).optString("message", "Unknown error");
            } catch (JSONException e) {
                return "Error parsing server response";
            }
        }
        return "Network error: " + error.getMessage();
    }
}
