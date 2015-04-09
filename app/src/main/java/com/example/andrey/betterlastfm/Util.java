package com.example.andrey.betterlastfm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Andrey on 09.04.2015.
 */
public class Util {
    private static final String LOG_TAG = Util.class.getSimpleName();

    public static String md5(String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void setSessionKey(Context context, String sessionKey){
        SharedPreferences mPref = context.getSharedPreferences(
                "com.example.andrey.betterlastfm", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString("session_key", sessionKey);
        editor.commit();
        Log.d(LOG_TAG, "Session key: " + mPref.getString("session_key", "Error setting session key"));

    }

    public static void setUsername(Context context, String username){
        SharedPreferences mPref = context.getSharedPreferences(
                "com.example.andrey.betterlastfm", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString("username", username);
        editor.commit();
        Log.d(LOG_TAG, "Username: " + mPref.getString("username", "Error setting username"));

    }
}
