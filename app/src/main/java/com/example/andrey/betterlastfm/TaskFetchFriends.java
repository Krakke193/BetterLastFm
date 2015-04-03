package com.example.andrey.betterlastfm;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by andrey on 11.03.15.
 */
public class TaskFetchFriends extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = this.getClass().getSimpleName();
    private Context context;
    private ArrayAdapter<Friend> arrayAdapter;
    private String friendsJsonStr = null;

    private String[] friendsArray = new String[10];
    private String[] friendsImageUrlArray = new String[10];

    TaskFetchFriends(Context context, ArrayAdapter<Friend> arrayAdapter){
        this.context = context;
        this.arrayAdapter = arrayAdapter;
    }

    private Void getFriendsFromJson(String friendsJsonStr) throws JSONException{

        final String NAME = "name";

        try {
            JSONObject friendsJson = new JSONObject(friendsJsonStr);
            JSONObject userJsonStr = friendsJson.getJSONObject("friends");
            JSONArray userJsonArray = userJsonStr.getJSONArray("user");

            for (int i=0; i<userJsonArray.length(); i++){

                friendsArray[i] = userJsonArray.getJSONObject(i).getString(NAME);

                JSONArray imageJson = userJsonArray.getJSONObject(i).getJSONArray("image");

                String imageURL = null;
                for (int j=0; j < imageJson.length(); j++){
                    if (imageJson.getJSONObject(j).getString("size").equals("medium")){
                        imageURL = imageJson.getJSONObject(j).getString("#text");
                    }
                }

                friendsImageUrlArray[i] = imageURL;
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected Void doInBackground(String... userName) {
        if (userName.length == 0)
            return null;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String user = userName[0];
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
                    .appendQueryParameter(USER, user)
                    .appendQueryParameter(API_KEY, apiKey)
                    .appendQueryParameter(LIMIT,limit)
                    .appendQueryParameter(FORMAT, format)
                    .build();
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null)
                return null;
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null){
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0)
                return null;

            friendsJsonStr = buffer.toString();

        } catch (IOException e){
            Log.e(LOG_TAG, "Error ", e);
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

        try {
            getFriendsFromJson(friendsJsonStr);
        } catch (JSONException e){
            Log.e(LOG_TAG, "Error parcing JSON friends: ", e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        arrayAdapter.clear();
        for (int i=0; i<friendsArray.length; i++){
            arrayAdapter.add(new Friend(friendsArray[i], friendsImageUrlArray[i]));
        }
    }
}
