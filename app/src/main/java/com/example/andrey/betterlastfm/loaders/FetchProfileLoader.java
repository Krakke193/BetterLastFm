package com.example.andrey.betterlastfm.loaders;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.example.andrey.betterlastfm.ProfileActivity;
import com.example.andrey.betterlastfm.R;
import com.example.andrey.betterlastfm.data.ProfileContract;
import com.example.andrey.betterlastfm.data.ProfileDbHelper;
import com.example.andrey.betterlastfm.model.RecentTrack;
import com.example.andrey.betterlastfm.data.RecentTracksProvider;
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

/**
 * Created by Андрей on 01.04.2015.
 */
public class FetchProfileLoader extends AsyncTaskLoader<Void> {

    private static final String LOG_TAG = FetchProfileLoader.class.getSimpleName();

    final Uri CONTACT_URI = Uri
            .parse("content://com.example.andrey.betterlastfm/tracks");

    private ArrayAdapter<RecentTrack> mListAdapter;
    private ArrayAdapter<TopArtist> mGridAdapter;
    private String mUserName;
    private Context mContext;
    private ProfileDbHelper dbHelper;
    private SQLiteDatabase mDbWrite;

    private String[] profileHeaderArray = new String[7];
    private String[] profileRecentTracksTitle = new String[11];
    private String[] profileRecentTracksArtist = new String[11];
    //private String[] profileRecentTracksArray = new String[11];
    private String[] profileRecentTracksUrlArray = new String[11];
    private String[] profileTopArtistsArray = new String[8];
    private String[] profileTopArtistsArrayPlaycount = new String[8];
    private String[] profileTopArtistsUrlArray = new String[8];

    public FetchProfileLoader(Context context, ArrayAdapter<RecentTrack> listAdapter,
                              ArrayAdapter<TopArtist> gridAdapter, String userName){
        super(context);
        this.mContext = context;
        this.mListAdapter = listAdapter;
        this.mGridAdapter = gridAdapter;
        this.mUserName = userName;

        dbHelper = new ProfileDbHelper(mContext);
        mDbWrite = dbHelper.getWritableDatabase();
    }

    /** Parsing JSON data for profile info and inserting database values! */
    private String[] getProfileInfoFromJson(String profileJsonStr) throws JSONException{

        final String IMAGE_URL = "image";
        final String PROFILE_NAME = "name";
        final String REAL_NAME = "realname";
        final String AGE = "age";
        final String COUNTRY = "country";
        final String PLAYCOUNT = "playcount";
        final String REGISTRATION_DATE = "registered";

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
            profileHeaderArray[0] = imageURL;
            profileHeaderArray[1] = userJson.getString(PROFILE_NAME);
            profileHeaderArray[2] = userJson.getString(REAL_NAME);
            profileHeaderArray[3] = userJson.getString(AGE);
            profileHeaderArray[4] = userJson.getString(COUNTRY);
            profileHeaderArray[5] = userJson.getString(PLAYCOUNT);
            profileHeaderArray[6] = dateJson.getString("#text");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        /**
         * Database insertion!
         */
        int counter = 0;
        if (mUserName.equals("se0ko")){

            mDbWrite.delete(
                    ProfileContract.HeaderEntry.TABLE_NAME,
                    null,
                    null
            );

            ContentValues headerValues = new ContentValues();
            headerValues.put(ProfileContract.HeaderEntry.COLUMN_HEADER_ICON_URL, profileHeaderArray[0]);
            headerValues.put(ProfileContract.HeaderEntry.COLUMN_HEADER_NAME, profileHeaderArray[1]);
            headerValues.put(ProfileContract.HeaderEntry.COLUMN_HEADER_REAL_NAME, profileHeaderArray[2]);
            headerValues.put(ProfileContract.HeaderEntry.COLUMN_HEADER_AGE, profileHeaderArray[3]);
            headerValues.put(ProfileContract.HeaderEntry.COLUMN_HEADER_COUNTRY, profileHeaderArray[4]);
            headerValues.put(ProfileContract.HeaderEntry.COLUMN_HEADER_PLAYCOUNT, profileHeaderArray[5]);
            headerValues.put(ProfileContract.HeaderEntry.COLUMN_HEADER_REGISTRY_DATE, profileHeaderArray[6]);
            mDbWrite.insert(ProfileContract.HeaderEntry.TABLE_NAME, null, headerValues);
            counter++;
        }
        Log.d(LOG_TAG, "Inserted " + Integer.toString(counter) + " header notes");
        return profileHeaderArray;
    }

    /** Parsing JSON data for recent tracks info and inserting database values! */
    private String[] getRecentTracksFromJson (String tracksJsonStr) throws JSONException{

        final String ARTIST = "#text";
        final String NAME = "name";

        try {
            JSONObject tracksJson = new JSONObject(tracksJsonStr);
            JSONObject recentTracksJson = tracksJson.getJSONObject("recenttracks");
            JSONArray recentTracksArr = recentTracksJson.getJSONArray("track");

            for (int i=0; i<recentTracksArr.length(); i++){
                profileRecentTracksArtist[i] = recentTracksArr.getJSONObject(i).getJSONObject("artist").getString(ARTIST);
                profileRecentTracksTitle[i] = recentTracksArr.getJSONObject(i).getString(NAME);


//                profileRecentTracksArray[i] = recentTracksArr.getJSONObject(i).getJSONObject("artist").getString(ARTIST)
//                        + " - " + recentTracksArr.getJSONObject(i).getString(NAME);

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

        /**
         * Database insertion!
         */

        if (mUserName.equals("se0ko")){
            ContentValues recentTrackValues = new ContentValues();

            int deleted = mContext.getContentResolver()
                    .delete(RecentTracksProvider.TRACKS_CONTENT_URI, null, null);
            Log.d(LOG_TAG, "deleted " + deleted + "rows!");


            for (int i = 10; i > 0 /* 9*/; i--){
                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ICON_URL,
                        profileRecentTracksUrlArray[i]);
                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ARTIST,
                        profileRecentTracksArtist[i]);
                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_NAME,
                        profileRecentTracksTitle[i]);
                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_TIMESTAMP,
                        "");

                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_SCROBBLEABLE_FLAG,
                        0);

                Uri newUri = mContext.getContentResolver()
                        .insert(RecentTracksProvider.TRACKS_CONTENT_URI, recentTrackValues);

                Log.d(LOG_TAG, "insert, result Uri : " + newUri.toString());

            }

        }
        return profileRecentTracksUrlArray;
    }

    /** Parsing JSON data for top artists and inserting database values! */
    private String[] getTopArtistsFromJson (String topArtistsJsonStr) throws JSONException{

        final String ARTIST_NAME = "name";
        final String PLAYCOUNT = "playcount";

        try {
            JSONObject tracksJson = new JSONObject(topArtistsJsonStr);
            JSONObject topArtistsJson = tracksJson.getJSONObject("topartists");
            JSONArray topArtistsArr = topArtistsJson.getJSONArray("artist");

            for (int i=0; i<topArtistsArr.length(); i++){
                profileTopArtistsArray[i] = topArtistsArr.getJSONObject(i).getString(ARTIST_NAME);
                profileTopArtistsArrayPlaycount[i] = topArtistsArr.getJSONObject(i).getString(PLAYCOUNT) + "plays";

                JSONArray imageJson = topArtistsArr.getJSONObject(i).getJSONArray("image");

                String imageURL = null;
                for (int j=0; j < imageJson.length(); j++){
                    if (imageJson.getJSONObject(j).getString("size").equals("extralarge")){
                        imageURL = imageJson.getJSONObject(j).getString("#text");
                    }
                }

                profileTopArtistsUrlArray[i] = imageURL;
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        /**
         * Database insertion!
         */
        int counter = 0;
        if (mUserName.equals("se0ko")){

            mDbWrite.delete(
                    ProfileContract.TopArtistsEntry.TABLE_NAME,
                    null,
                    null
            );

            ContentValues topArtistsValues = new ContentValues();

            for (int i = 0; i < profileTopArtistsArray.length; i++){
                topArtistsValues.put(ProfileContract.TopArtistsEntry.COLUMN_ARTIST_ICON_URL, profileTopArtistsUrlArray[i]);
                topArtistsValues.put(ProfileContract.TopArtistsEntry.COLUMN_ARTIST_NAME, profileTopArtistsArray[i]);
                topArtistsValues.put(ProfileContract.TopArtistsEntry.COLUMN_ARTIST_PLAYCOUNT, profileTopArtistsArrayPlaycount[i]);

                mDbWrite.insert(
                        ProfileContract.TopArtistsEntry.TABLE_NAME,
                        null,
                        topArtistsValues
                );
                counter++;
            }
            Log.d(LOG_TAG, "Inserted " + counter + " artists rows");
        }

        return profileTopArtistsUrlArray;
    }

    @Override
    public Void loadInBackground() {
        String profileJsonStr = null;
        String profileRecentTracksJsonStr = null;
        String profileTopArtistsJsonStr = null;

        String user = mUserName;
        String format = "json";
        String apiKey = "90167bec56ea0d23c263e7a59a395eb2";
        String limit = "8";

        String methodTypeHeaderInfo = "user.getinfo";
        String methodTypeRecentTracks = "user.getrecenttracks";
        String methodTypeTopArtists = "user.gettopartists";

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        if (mUserName.equals(""))
            return null;

        /** Getting header info!*/
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

            profileJsonStr = buffer.toString();

            getProfileInfoFromJson(profileJsonStr);

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

        /** Getting recent tracks!*/
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

        /** Getting top artists! */
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

            profileTopArtistsJsonStr = buffer.toString();

            getTopArtistsFromJson(profileTopArtistsJsonStr);

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

    @Override
    public void deliverResult(Void data) {
        super.deliverResult(data);

        Picasso.with(mContext).load(profileHeaderArray[0]).into(ProfileActivity.ivProfilePic);
        ProfileActivity.tvProfileName.setText(profileHeaderArray[1]);
        ProfileActivity.tvProfileDetails.setText(
                profileHeaderArray[2] +
                ", " +
                profileHeaderArray[3] +
                ", " +
                profileHeaderArray[4]
        );
        ProfileActivity.tvProfileListens.setText(
                profileHeaderArray[5] +
                mContext.getString(R.string.profile_header_listens) +
                " " +
                profileHeaderArray[6]
        );

        mListAdapter.clear();
        for(int i=0; i<5 /*profileRecentTracksArray.length*/; i++) {
            mListAdapter.add(new RecentTrack(
                    profileRecentTracksArtist[i] + " - " + profileRecentTracksTitle[i],
                    profileRecentTracksUrlArray[i]
            ));
        }

        mGridAdapter.clear();
        for (int i=0; i<profileTopArtistsArray.length; i++){
            mGridAdapter.add(new TopArtist(
                    profileTopArtistsArray[i],
                    profileTopArtistsArrayPlaycount[i],
                    profileTopArtistsUrlArray[i]
            ));
        }

        ProfileActivity.profileProgressBar.setVisibility(View.INVISIBLE);
    }
}
