package com.example.andrey.betterlastfm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Andrey on 09.04.2015.
 */
public class Util {
    private static final String LOG_TAG = Util.class.getSimpleName();

    public static final String SECRET = "5b332291ad05138bd2e441a22262e5b2";
    public static final String API_KEY = "f445e682840e750fc7c992898e868efb";
    public static final String ERROR = "Error";

    public static final String PROFILE_BASE_URL_TEXT = "http://ws.audioscrobbler.com/2.0/?";
    public static final String METHOD_TEXT = "method";
    public static final String USER_TEXT = "user";
    public static final String API_KEY_TEXT = "api_key";
    public static final String FORMAT_TEXT = "format";
    public static final String ARTIST_TEXT = "artist";

    public static final String METHOD_ARTIST_GET_INFO = "artist.getInfo";

    public static final String ARTIST_KEY = "artist";
    public static final String PREF_SCROBBLE_KEY = "pref_scrobble_key";
    public static final String USERNAME_KEY = "username";


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

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }


}
