package com.example.travelmemory.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "travel_memory_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_CURRENT_USER_ID = "current_user_id";
    private static final String KEY_CURRENT_USERNAME = "current_username";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveLogin(int userId, String username) {
        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putInt(KEY_CURRENT_USER_ID, userId)
                .putString(KEY_CURRENT_USERNAME, username)
                .apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getCurrentUserId() {
        return preferences.getInt(KEY_CURRENT_USER_ID, -1);
    }

    public String getCurrentUsername() {
        return preferences.getString(KEY_CURRENT_USERNAME, "");
    }

    public void logout() {
        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_CURRENT_USER_ID)
                .remove(KEY_CURRENT_USERNAME)
                .apply();
    }
}
