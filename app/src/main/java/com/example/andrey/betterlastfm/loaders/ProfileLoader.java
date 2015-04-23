package com.example.andrey.betterlastfm.loaders;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.andrey.betterlastfm.ArtistActivity;
import com.example.andrey.betterlastfm.ProfileActivity;
import com.example.andrey.betterlastfm.R;
import com.example.andrey.betterlastfm.Util;
import com.example.andrey.betterlastfm.data.ProfileContract;
import com.example.andrey.betterlastfm.data.ProfileDbHelper;
import com.example.andrey.betterlastfm.data.RecentTracksProvider;
import com.example.andrey.betterlastfm.model.Profile;
import com.example.andrey.betterlastfm.model.RecentTrack;
import com.example.andrey.betterlastfm.model.TopArtist;
import com.squareup.picasso.Picasso;

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
 * Created by Андрей on 01.04.2015.
 */
public class ProfileLoader extends AsyncTaskLoader<Profile> {
    private static final String LOG_TAG = ProfileLoader.class.getSimpleName();

    private String mUserName;
    private Context mContext;
    private ProfileDbHelper dbHelper;
    private SQLiteDatabase mDbWrite;
    private SharedPreferences mShrdPrefs;

    private ArrayList<String> mProfileHeaderArray = new ArrayList<>();
    private ArrayList<RecentTrack> mProfileRecentTracks = new ArrayList<>();
    private ArrayList<TopArtist> mProfileTopArtists = new ArrayList<>();

    public ProfileLoader(Context context, String userName){
        super(context);
        this.mContext = context;
        this.mUserName = userName;

        dbHelper = new ProfileDbHelper(mContext);
        mDbWrite = dbHelper.getWritableDatabase();
        mShrdPrefs = mContext.getSharedPreferences("com.example.andrey.betterlastfm",Context.MODE_PRIVATE);
    }

    /**
     * Parsing JSON data for profile info and inserting database values!
     * @param profileJsonStr
     * @return profileHeaderArray
     */
    private ArrayList<String> getProfileInfoFromJson(String profileJsonStr) throws JSONException{
        final String IMAGE_URL = "image";
        final String PROFILE_NAME = "name";
        final String REAL_NAME = "realname";
        final String AGE = "age";
        final String COUNTRY = "country";
        final String PLAYCOUNT = "playcount";
        final String REGISTRATION_DATE = "registered";

        ArrayList<String> profileHeaderArray = new ArrayList<>();

        try {
            JSONObject profileJson = new JSONObject(profileJsonStr);
            JSONObject userJson = profileJson.getJSONObject("user");
            JSONObject dateJson = userJson.getJSONObject(REGISTRATION_DATE);
            JSONArray imageJson = userJson.getJSONArray(IMAGE_URL);

            String imageURL = null;
            for (int i=0; i < imageJson.length(); i++){
                if (imageJson.getJSONObject(i).getString("size").equals("large")){
                    imageURL = imageJson.getJSONObject(i).getString("#text");
                }
            }
            profileHeaderArray.add(imageURL);
            profileHeaderArray.add(userJson.getString(PROFILE_NAME));
            profileHeaderArray.add(userJson.getString(REAL_NAME));
            profileHeaderArray.add(userJson.getString(AGE));
            profileHeaderArray.add(userJson.getString(COUNTRY));
            profileHeaderArray.add(userJson.getString(PLAYCOUNT));
            profileHeaderArray.add(dateJson.getString("#text"));
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        //Database insertion!

        if (mUserName.equals("se0ko")){

            mDbWrite.delete(
                    ProfileContract.HeaderEntry.TABLE_NAME,
                    null,
                    null
            );

            ContentValues headerValues = new ContentValues();
            headerValues
                    .put(ProfileContract.HeaderEntry.COLUMN_HEADER_ICON_URL, profileHeaderArray.get(0));
            headerValues
                    .put(ProfileContract.HeaderEntry.COLUMN_HEADER_NAME, profileHeaderArray.get(1));
            headerValues
                    .put(ProfileContract.HeaderEntry.COLUMN_HEADER_REAL_NAME, profileHeaderArray.get(2));
            headerValues
                    .put(ProfileContract.HeaderEntry.COLUMN_HEADER_AGE, profileHeaderArray.get(3));
            headerValues
                    .put(ProfileContract.HeaderEntry.COLUMN_HEADER_COUNTRY, profileHeaderArray.get(4));
            headerValues
                    .put(ProfileContract.HeaderEntry.COLUMN_HEADER_PLAYCOUNT, profileHeaderArray.get(5));
            headerValues
                    .put(ProfileContract.HeaderEntry.COLUMN_HEADER_REGISTRY_DATE, profileHeaderArray.get(6));
            
            mDbWrite.insert(ProfileContract.HeaderEntry.TABLE_NAME, null, headerValues);
        }
        return profileHeaderArray;
    }

    /**
     * Parsing JSON data for recent tracks info and inserting database values!
     * @param tracksJsonStr
     * @return profileRecentTracks
     */
    private ArrayList<RecentTrack> getRecentTracksFromJson (String tracksJsonStr) throws JSONException{
        final String ARTIST = "#text";
        final String NAME = "name";

        ArrayList<RecentTrack> profileRecentTracks = new ArrayList<>();

        try {
            JSONObject tracksJson = new JSONObject(tracksJsonStr);
            JSONObject recentTracksJson = tracksJson.getJSONObject("recenttracks");
            JSONArray recentTracksArr = recentTracksJson.getJSONArray("track");

            for (int i=0; i<recentTracksArr.length(); i++){
                JSONArray imageJson = recentTracksArr.getJSONObject(i).getJSONArray("image");

                String imageURL = null;
                for (int j=0; j < imageJson.length(); j++){
                    if (imageJson.getJSONObject(j).getString("size").equals("medium")){
                        imageURL = imageJson.getJSONObject(j).getString("#text");
                    }
                }

                String recentTrackDate;
                try {
                    recentTrackDate = recentTracksArr
                            .getJSONObject(i)
                            .getJSONObject("date")
                            .getString("#text");

                } catch (JSONException e){
                    e.printStackTrace();
                    recentTrackDate = "Now playing";
                }

                profileRecentTracks.add(new RecentTrack(
                        recentTracksArr.getJSONObject(i).getString(NAME),
                        recentTracksArr.getJSONObject(i).getJSONObject("artist").getString(ARTIST),
                        "",
                        imageURL,
                        recentTrackDate
                ));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        //Database insertion!

        if (mUserName.equals("se0ko")){
            ContentValues recentTrackValues = new ContentValues();

            mContext.getContentResolver()
                    .delete(RecentTracksProvider.TRACKS_CONTENT_URI, null, null);

            Log.d(LOG_TAG, Integer.toString(profileRecentTracks.size()));

            for (int i = profileRecentTracks.size()-1; i >= 0; i--){

                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ARTIST,
                        profileRecentTracks.get(i).getTrackArtist());
                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_NAME,
                        profileRecentTracks.get(i).getTrackName());
                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ALBUM,
                        profileRecentTracks.get(i).getAlbum());
                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ICON_URL,
                        profileRecentTracks.get(i).getTrackImageURL());
                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_TIMESTAMP,
                        profileRecentTracks.get(i).getTrackDate());
                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_SCROBBLEABLE_FLAG, 0);

                mContext.getContentResolver()
                        .insert(RecentTracksProvider.TRACKS_CONTENT_URI, recentTrackValues);
            }
        }

        return profileRecentTracks;
    }

    /**
     * Parsing JSON data for top artists and inserting database values!
     * @param topArtistsJsonStr
     * @return profileTopArtists
     */
    private ArrayList<TopArtist> getTopArtistsFromJson (String topArtistsJsonStr)
            throws JSONException{
        final String ARTIST_NAME = "name";
        final String PLAYCOUNT = "playcount";

        ArrayList<TopArtist> profileTopArtists = new ArrayList<>();
        try {
            JSONObject tracksJson = new JSONObject(topArtistsJsonStr);
            JSONObject topArtistsJson = tracksJson.getJSONObject("topartists");
            JSONArray topArtistsArr = topArtistsJson.getJSONArray("artist");

            for (int i=0; i<topArtistsArr.length(); i++){
                JSONArray imageJson = topArtistsArr.getJSONObject(i).getJSONArray("image");

                String imageURL = null;
                for (int j=0; j < imageJson.length(); j++){
                    if (imageJson.getJSONObject(j).getString("size").equals("medium")){
                        imageURL = imageJson.getJSONObject(j).getString("#text");
                    }
                }

                profileTopArtists.add(new TopArtist(
                        topArtistsArr.getJSONObject(i).getString(ARTIST_NAME),
                        topArtistsArr.getJSONObject(i).getString(PLAYCOUNT) + "plays",
                        imageURL
                ));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        //Database insertion!

        if (mUserName.equals("se0ko")){
            mDbWrite.delete(
                    ProfileContract.TopArtistsEntry.TABLE_NAME,
                    null,
                    null
            );

            ContentValues topArtistsValues = new ContentValues();

            for (int i = 0; i < profileTopArtists.size(); i++){
                topArtistsValues
                        .put(ProfileContract.TopArtistsEntry.COLUMN_ARTIST_ICON_URL,
                                profileTopArtists.get(i).getArtistPicURL());

                topArtistsValues
                        .put(ProfileContract.TopArtistsEntry.COLUMN_ARTIST_NAME,
                                profileTopArtists.get(i).getArtistName());

                topArtistsValues
                        .put(ProfileContract.TopArtistsEntry.COLUMN_ARTIST_PLAYCOUNT,
                                profileTopArtists.get(i).getArtistPlays());

                mDbWrite.insert(
                        ProfileContract.TopArtistsEntry.TABLE_NAME,
                        null,
                        topArtistsValues
                );
            }
        }

        return profileTopArtists;
    }

    @Override
    public Profile loadInBackground() {
        String profileJsonStr;
        String profileRecentTracksJsonStr;
        String profileTopArtistsJsonStr;

        String user = mUserName;
        String format = "json";
        String apiKey = "90167bec56ea0d23c263e7a59a395eb2";
        String limit = "9";

        String methodTypeHeaderInfo = "user.getinfo";
        String methodTypeRecentTracks = "user.getrecenttracks";
        String methodTypeTopArtists = "user.gettopartists";

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        if (mUserName.equals(""))
            return null;

        //Getting header info!

        try {
            final String PROFILE_BASE_URL = "http://ws.audioscrobbler.com/2.0/?";
            final String METHOD_TYPE = "method";
            final String USER = "user";
            final String API_KEY = "api_key";
            final String FORMAT = "format";

            Uri builtUri = Uri.parse(PROFILE_BASE_URL).buildUpon()
                    .appendQueryParameter(METHOD_TYPE, methodTypeHeaderInfo)
                    .appendQueryParameter(USER, user)
                    .appendQueryParameter(API_KEY, apiKey)
                    .appendQueryParameter(FORMAT, format)
                    .build();
            URL url = new URL(builtUri.toString());

            Log.d(LOG_TAG, url.toString());

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

            profileJsonStr = buffer.toString();

            mProfileHeaderArray = getProfileInfoFromJson(profileJsonStr);

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

        //Getting recent tracks!

        try {
            final String PROFILE_BASE_URL = "http://ws.audioscrobbler.com/2.0/?";
            final String METHOD_TYPE = "method";
            final String USER = "user";
            final String API_KEY = "api_key";
            final String FORMAT = "format";
            Uri builtUri = Uri.parse(Util.PROFILE_BASE_URL).buildUpon()
                    .appendQueryParameter(METHOD_TYPE, methodTypeRecentTracks)
                    .appendQueryParameter(USER, user)
                    .appendQueryParameter(API_KEY, apiKey)
                    .appendQueryParameter(FORMAT, format)
                    .build();
            URL url = new URL(builtUri.toString());

            Log.d(LOG_TAG, url.toString());

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

            profileRecentTracksJsonStr = buffer.toString();

            mProfileRecentTracks = getRecentTracksFromJson(profileRecentTracksJsonStr);

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

        // Getting top artists!

        try {
            final String PROFILE_BASE_URL = "http://ws.audioscrobbler.com/2.0/?";
            final String METHOD_TYPE = "method";
            final String USER = "user";
            final String API_KEY = "api_key";
            final String LIMIT = "limit";
            final String FORMAT = "format";
            Uri builtUri = Uri.parse(PROFILE_BASE_URL).buildUpon()
                    .appendQueryParameter(METHOD_TYPE, methodTypeTopArtists)
                    .appendQueryParameter(USER, user)
                    .appendQueryParameter(API_KEY, apiKey)
                    .appendQueryParameter(LIMIT, limit)
                    .appendQueryParameter(FORMAT, format)
                    .build();
            URL url = new URL(builtUri.toString());

            Log.d(LOG_TAG, url.toString());

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

            profileTopArtistsJsonStr = buffer.toString();

            mProfileTopArtists = getTopArtistsFromJson(profileTopArtistsJsonStr);

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

        Profile profile = new Profile(mProfileHeaderArray, mProfileRecentTracks, mProfileTopArtists);
        Log.d(LOG_TAG, "Finished loading in background");
        return profile;
    }

    @Override
    public void deliverResult(Profile data) {
        super.deliverResult(data);
        Log.d(LOG_TAG, "Result delivered?");
    }
}
