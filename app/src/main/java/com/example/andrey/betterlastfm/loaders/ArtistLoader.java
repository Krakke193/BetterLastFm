package com.example.andrey.betterlastfm.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.example.andrey.betterlastfm.Util;
import com.example.andrey.betterlastfm.model.Artist;
import com.example.andrey.betterlastfm.network.ArtistDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Andrey on 21.04.2015.
 */
public class ArtistLoader extends AsyncTaskLoader<Artist> {
    private final String LOG_TAG = ArtistLoader.class.getSimpleName();

    private Artist mArtist;
    private String mArtistName;

    public ArtistLoader(Context context, String artistName){
        super(context);
        this.mArtistName = artistName;
    }

    @Override
    public Artist loadInBackground() {
        SharedPreferences shrdPref = getContext()
                .getSharedPreferences("com.example.andrey.betterlastfm", Context.MODE_PRIVATE);
        String userName = shrdPref.getString(Util.USERNAME_KEY, Util.ERROR);
        if (userName.equals(Util.ERROR))
            return null;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            if (!Util.isInternetAvailable()){
                return null;
            }
            Uri builtUri = Uri.parse(Util.PROFILE_BASE_URL).buildUpon()
                    .appendQueryParameter(Util.METHOD_TEXT, Util.METHOD_ARTIST_GET_INFO)
                    .appendQueryParameter(Util.ARTIST_TEXT, mArtistName)
                    .appendQueryParameter(Util.USER_TEXT, userName)
                    .appendQueryParameter(Util.API_KEY_TEXT, Util.API_KEY)
                    .appendQueryParameter(Util.FORMAT_TEXT, "json")
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
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0)
                return null;

            String artistJson = buffer.toString();
            mArtist = parseArtistFromJson(artistJson);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, e.toString());
        }

        return mArtist;
    }

    public Artist parseArtistFromJson(String jsonStr){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Artist.class, new ArtistDeserializer())
                .create();
        return gson.fromJson(jsonStr, Artist.class);
    }

    @Override
    public void deliverResult(Artist data) {
        super.deliverResult(data);
    }
}
