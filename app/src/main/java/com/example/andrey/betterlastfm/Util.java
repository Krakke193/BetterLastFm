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
    public static final String PREF_SCROBBLE_KEY = "pref_scrobble_key";

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

    public static void setListViewHeightBasedOnChildren(Context context, ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, RadioGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount()));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static void setListViewHeightBasedOnChildren(Context context, GridView gridView1) {
        ListAdapter listAdapter = gridView1.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(gridView1.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;

        int bestOfThreeHeight;
        int rowCounter = 0;

        if (listAdapter.getCount() == 0)
            return;

        for (int i = 0; i < 3; i++) {
            bestOfThreeHeight = 0;

            for (int j=0; j<3; j++){
                view = listAdapter.getView(rowCounter, view, gridView1);
                if (i == 0)
                    view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, RadioGroup.LayoutParams.WRAP_CONTENT));

                view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                int temp = view.getMeasuredHeight();

                if (temp > bestOfThreeHeight)
                    bestOfThreeHeight = temp;

                rowCounter++;
            }

            //rowCounter = rowCounter + 3;
            totalHeight += bestOfThreeHeight + context.getResources().getDimension(R.dimen.grid_list_padding);
        }


//        for (int i = 0; i < listAdapter.getCount(); i++) {
//            view = listAdapter.getView(i, view, gridView1);
//            if (i == 0)
//                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, RadioGroup.LayoutParams.WRAP_CONTENT));
//
//            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
//            totalHeight += view.getMeasuredHeight();
//        }
        ViewGroup.LayoutParams params = gridView1.getLayoutParams();
        params.height = totalHeight;
        gridView1.setLayoutParams(params);
        gridView1.requestLayout();
    }

    public void setGridViewHeightBasedOnChildren(GridView gridView, int columns) {
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int items = listAdapter.getCount();
        int rows = 0;

        View listItem = listAdapter.getView(0, null, gridView);
        listItem.measure(0, 0);
        totalHeight = listItem.getMeasuredHeight();

        float x = 1;
        if( items > columns ){
            x = items/columns;
            rows = (int) (x + 1);
            totalHeight *= rows;
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);

    }
}
