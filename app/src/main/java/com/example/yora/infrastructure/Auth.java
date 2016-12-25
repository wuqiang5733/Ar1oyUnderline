package com.example.yora.infrastructure;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.yora.activities.LoginActivity;

public class Auth {
    private static final String AUTH_PREFERENCES = "AUTH_PREFERENCES";
    private static final String AUTH_PREFERENCES_TOKEN = "AUTH_PREFERENCES_TOKEN";

    private final Context _context;
    private final SharedPreferences _preferences;
    private User _user;
    private String _authToken;


    public Auth(Context context) {
        _context = context;
        _user = new User();
        _preferences = context.getSharedPreferences(AUTH_PREFERENCES, Context.MODE_PRIVATE);
        _authToken = _preferences.getString(AUTH_PREFERENCES_TOKEN, null);
    }

    public User getUser() {
        return _user;
    }

    public String getAuthToken() {
        return _authToken;
    }

    public void setAuthToken(String authToken) {
        _authToken = authToken;

        SharedPreferences.Editor editor = _preferences.edit();
        editor.putString(AUTH_PREFERENCES_TOKEN, authToken);
        editor.commit();
    }

    public boolean hasAuthToken() {
        return _authToken != null && !_authToken.isEmpty();
    }

    public void logout() {
        setAuthToken(null);

        Intent loginIntent = new Intent(_context, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(loginIntent);
    }
}
