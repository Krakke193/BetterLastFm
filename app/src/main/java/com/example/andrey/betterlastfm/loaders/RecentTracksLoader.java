package com.example.andrey.betterlastfm.loaders;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.andrey.betterlastfm.data.ProfileContract;
import com.example.andrey.betterlastfm.data.RecentTrack;
import com.example.andrey.betterlastfm.data.RecentTracksProvider;

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
 * Created by Andrey on 08.04.2015.
 */
public class RecentTracksLoader extends AsyncTaskLoader<Void> {
    private final String LOG_TAG = RecentTracksLoader.class.getSimpleName();

    private Context mContext;
    private ArrayAdapter<RecentTrack> mListAdapter;
    private String mUserName;

    private String[] profileRecentTracksArray = new String[11];
    private String[] profileRecentTracksUrlArray = new String[11];

    public RecentTracksLoader(Context context,
                              ArrayAdapter<RecentTrack> listAdapter, String userName){
        super(context);
        this.mContext = context;
        this.mListAdapter = listAdapter;
        this.mUserName = userName;

    }

    @Override
    public Void loadInBackground() {
        String profileRecentTracksJsonStr = null;

        String user = mUserName;
        String format = "json";
        String apiKey = "90167bec56ea0d23c263e7a59a395eb2";

        String methodTypeRecentTracks = "user.getrecenttracks";

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        if (mUserName.equals(""))
            return null;

        try {
            final String PROFILE_BASE_URL = "http://ws.audioscrobbler.com/2.0/?";
            final String METHOD_TYPE = "method";
            final String USER = "user";
            final String API_KEY = "api_key";
            final String FORMAT = "format";
            Uri builtUri = Uri.parse(PROFILE_BASE_URL).buildUpon()
                    .appendQueryParameter(METHOD_TYPE, methodTypeRecentTracks)
                    .appendQueryParameter(USER, user)
                    .appendQueryParameter(API_KEY, apiKey)
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
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0)
                return null;

            profileRecentTracksJsonStr = buffer.toString();

            getRecentTracksFromJson(profileRecentTracksJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;

        } catch (JSONException e){
            Log.e(LOG_TAG, "Error parcing JSON header: ", e);
            e.printStackTrace();

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

        return null;
    }

    private String[] getRecentTracksFromJson (String tracksJsonStr) throws JSONException{

        final String ARTIST = "#text";
        final String NAME = "name";

        try {
            JSONObject tracksJson = new JSONObject(tracksJsonStr);
            JSONObject recentTracksJson = tracksJson.getJSONObject("recenttracks");
            JSONArray recentTracksArr = recentTracksJson.getJSONArray("track");

            for (int i=0; i<recentTracksArr.length(); i++){
                profileRecentTracksArray[i] = recentTracksArr.getJSONObject(i).getJSONObject("artist").getString(ARTIST)
                        + " - " + recentTracksArr.getJSONObject(i).getString(NAME);

                JSONArray imageJson = recentTracksArr.getJSONObject(i).getJSONArray("image");

                String imageURL = null;
                for (int j=0; j < imageJson.length(); j++){
                    if (imageJson.getJSONObject(j).getString("size").equals("medium")){
                        imageURL = imageJson.getJSONObject(j).getString("#text");
                    }
                }

                profileRecentTracksUrlArray[i] = imageURL;
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return profileRecentTracksArray;
    }

    @Override
    public void deliverResult(Void data) {
        super.deliverResult(data);

        mListAdapter.clear();
        for(int i=0; i<10 /*profileRecentTracksArray.length*/; i++) {
            mListAdapter.add(new RecentTrack(
                    profileRecentTracksArray[i],
                    profileRecentTracksUrlArray[i]
            ));
        }
    }
}
