package com.example.miniproject.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.miniproject.model.User;

public class UserManager {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_ID = "user_id";
    private static final String KEY_FIRSTNAME = "firstname";
    private static final String KEY_LASTNAME = "lastname";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "hashed_password";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public UserManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }


    public void saveUser(User user) {
        editor.putString(KEY_ID, user.getId());
        editor.putString(KEY_FIRSTNAME, user.getFirstname());
        editor.putString(KEY_LASTNAME, user.getLastname());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_PASSWORD, user.getPassword());
        editor.apply();
    }

    public User getUser() {
        String id = sharedPreferences.getString(KEY_ID, null);
        String firstname = sharedPreferences.getString(KEY_FIRSTNAME, null);
        String lastname = sharedPreferences.getString(KEY_LASTNAME, null);
        String email = sharedPreferences.getString(KEY_EMAIL, null);
        String password = sharedPreferences.getString(KEY_PASSWORD, null);

        if (id != null && email != null && password != null) {
            return new User(firstname, lastname, email, password);
        }
        return null;
    }

    public boolean isLoggedIn() {
        return sharedPreferences.contains(KEY_ID);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
