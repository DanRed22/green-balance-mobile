package com.example.greenbalance.utility;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;

public class Prefs {
    private static SharedPreferences prefs;
    private static final String PREFS_NAME = "user_prefs";
    private static String KEY_USERNAME = "username";
    private static String KEY_EMAIL = "password";
    private static String KEY_ID = "id";

    public static void init(Context context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    public static void saveLoginData(JSONObject obj) {

        try {
            // Iterate through the keys in the JSONObject
            Iterator<String> keys = obj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = obj.getString(key);
                prefs.edit().putString(key, value).apply();
            }
            Log.d("saveLoginData", "Login data saved successfully" + prefs.getAll().toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("saveLoginData", "Error saving login data: " + e.getMessage());
        }
    }

    /*Gets login data*/
    public static String getLoginData() {
        if(prefs == null){
            return null;
        } else {
            Log.d("getLoginData", "Login data retrieved successfully" + prefs.getAll().toString());
            KEY_USERNAME = prefs.getString("username", null);
            KEY_EMAIL = prefs.getString("password", null);
            KEY_ID = prefs.getString("id", null);
            return KEY_USERNAME;
        }
    }

    /*Logs out user by clearing login data*/
    public static void clearLoginData() {
        prefs.edit().clear().apply();
    }

    public static String getKeyUsername() {
        return KEY_USERNAME;
    }

    public static String getKeyEmail() {
        return KEY_EMAIL;
    }

    public static String getKeyId() {
        return KEY_ID;
    }

    public static void setUsername(String username) {
        prefs.edit().putString(KEY_USERNAME, username).apply();
        KEY_USERNAME = username;
    }
}
