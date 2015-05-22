package com.example.andrey.betterlastfm.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.andrey.betterlastfm.Util;
import com.example.andrey.betterlastfm.model.Friend;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Andrey on 08.04.2015.
 */
public class FriendsLoader extends AsyncTaskLoader<ArrayList<Friend>> {
    private final String LOG_TAG = FriendsLoader.class.getSimpleName();

    private Context mContext;
    private String mUserName;
    private ArrayList<Friend> friends = new ArrayList<>();


    public FriendsLoader(Context context, String userName){
        super(context);
        this.mContext = context;
        this.mUserName = userName;
    }

    @Override
    public ArrayList<Friend> loadInBackground() {
        if (mUserName.equals(""))
            return null;
        if (!Util.isInternetAvailable()){
            return null;
        }
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String format = "json";
        String apiKey = "90167bec56ea0d23c263e7a59a395eb2";
        String limit = "10";
        String methodType = "user.getfriends";

        try {
            final String PROFILE_BASE_URL = "http://ws.audioscrobbler.com/2.0/?";
            final String METHOD_TYPE = "method";
            final String USER = "user";
            final String API_KEY = "api_key";
            final String LIMIT = "limit";
            final String FORMAT = "format";

            Uri builtUri = Uri.parse(PROFILE_BASE_URL).buildUpon()
                    .appendQueryParameter(METHOD_TYPE, methodType)
                    .appendQueryParameter(USER, mUserName)
                    .appendQueryParameter(API_KEY, apiKey)
                    .appendQueryParameter(LIMIT,limit)
                    .appendQueryParameter(FORMAT, format)
                    .build();
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null)
                return null;
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null){
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0)
                return null;

            String friendsJsonStr = buffer.toString();

            friends = getFriendsFromJson(friendsJsonStr);

        } catch (IOException e) {
            return null;
        } catch (JSONException e) {
            return null;
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e){
                    Log.e(LOG_TAG, " Error closing stream ", e);
                }
            }
        }

        return friends;
    }

    private ArrayList<Friend> getFriendsFromJson(String friendsJsonStr) throws JSONException{
        final String NAME = "name";

        ArrayList<Friend> friendArrayList = new ArrayList<>();

        try {
            JSONObject friendsJson = new JSONObject(friendsJsonStr);
            JSONObject userJsonStr = friendsJson.getJSONObject("friends");
            JSONArray userJsonArray = userJsonStr.getJSONArray("user");

            for (int i=0; i<userJsonArray.length(); i++){

                JSONArray imageJson = userJsonArray.getJSONObject(i).getJSONArray("image");

                String imageURL = null;
                for (int j=0; j < imageJson.length(); j++){
                    if (imageJson.getJSONObject(j).getString("size").equals("medium")){
                        imageURL = imageJson.getJSONObject(j).getString("#text");
                    }
                }

                friendArrayList.add(
                        new Friend(userJsonArray.getJSONObject(i).getString(NAME), imageURL));
            }
        } catch (JSONException e) {
            return null;
        }

        return friendArrayList;
    }

    @Override
    public void deliverResult(ArrayList<Friend> data) {
        super.deliverResult(data);
    }
}
