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
        }else{
            return prefs.getString("username", null);
        }
    }

    /*Logs out user by clearing login data*/
    public static void clearLoginData() {
        prefs.edit().clear().apply();
    }
}
