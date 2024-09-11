package com.daon.fido.sdk.sample.basic.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private final String SHARED_PREF_TABLE = "daon_shared_pref";
    public final static String SHARED_PREF_EMAIL = "shared_pref_email";
    public final static String SHARED_PREF_IS_FACE_ONLY = "shared_pref_is_face_only";

    public void storeStringData(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_TABLE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getStringData(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_TABLE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public void storeBooleanData(Context context, String key, boolean value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_TABLE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public Boolean getBooleanData(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_TABLE, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, false);
    }

}
