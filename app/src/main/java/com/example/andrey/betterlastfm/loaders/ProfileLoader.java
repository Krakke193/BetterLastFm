package com.example.andrey.betterlastfm.loaders;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
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

import com.example.andrey.betterlastfm.ProfileActivity;
import com.example.andrey.betterlastfm.R;
import com.example.andrey.betterlastfm.data.ProfileContract;
import com.example.andrey.betterlastfm.data.ProfileDbHelper;
import com.example.andrey.betterlastfm.data.RecentTracksProvider;
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
public class ProfileLoader extends AsyncTaskLoader<Void> {
    private static final String LOG_TAG = ProfileLoader.class.getSimpleName();

    private String mUserName;
    private Context mContext;
    private ProfileDbHelper dbHelper;
    private SQLiteDatabase mDbWrite;
    private SharedPreferences mShrdPrefs;

    private ArrayList<RecentTrack> mProfileRecentTracks = new ArrayList<>();
    private ArrayList<TopArtist> mProfileTopArtists = new ArrayList<>();

    private String[] profileHeaderArray = new String[7];

    public ProfileLoader(Context context, String userName){
        super(context);
        this.mContext = context;
        this.mUserName = userName;

        dbHelper = new ProfileDbHelper(mContext);
        mDbWrite = dbHelper.getWritableDatabase();
        mShrdPrefs = mContext.getSharedPreferences("com.example.andrey.betterlastfm",Context.MODE_PRIVATE);
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

        //Database insertion!

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
        }
        return profileHeaderArray;
    }

    /** Parsing JSON data for recent tracks info and inserting database values! */
    private void getRecentTracksFromJson (String tracksJsonStr) throws JSONException{
        final String ARTIST = "#text";
        final String NAME = "name";

        mProfileRecentTracks.clear();

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
                    recentTrackDate = recentTracksArr.getJSONObject(i).getJSONObject("date").getString("#text");
                } catch (JSONException e){
                    e.printStackTrace();
                    recentTrackDate = "Now playing";
                }

                mProfileRecentTracks.add(new RecentTrack(
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

            Log.d(LOG_TAG, Integer.toString(mProfileRecentTracks.size()));

            for (int i = mProfileRecentTracks.size()-1; i >= 0; i--){

                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ARTIST,
                        mProfileRecentTracks.get(i).getTrackArtist());
                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_NAME,
                        mProfileRecentTracks.get(i).getTrackName());
                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ALBUM,
                        mProfileRecentTracks.get(i).getAlbum());
                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ICON_URL,
                        mProfileRecentTracks.get(i).getTrackImageURL());
                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_TIMESTAMP,
                        mProfileRecentTracks.get(i).getTrackDate());
                recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_SCROBBLEABLE_FLAG, 0);

                mContext.getContentResolver()
                        .insert(RecentTracksProvider.TRACKS_CONTENT_URI, recentTrackValues);
            }
        }
    }

    /** Parsing JSON data for top artists and inserting database values! */
    private void getTopArtistsFromJson (String topArtistsJsonStr) throws JSONException{
        final String ARTIST_NAME = "name";
        final String PLAYCOUNT = "playcount";

        mProfileTopArtists.clear();
        try {
            JSONObject tracksJson = new JSONObject(topArtistsJsonStr);
            JSONObject topArtistsJson = tracksJson.getJSONObject("topartists");
            JSONArray topArtistsArr = topArtistsJson.getJSONArray("artist");

            for (int i=0; i<topArtistsArr.length(); i++){
                JSONArray imageJson = topArtistsArr.getJSONObject(i).getJSONArray("image");

                String imageURL = null;
                for (int j=0; j < imageJson.length(); j++){
                    if (imageJson.getJSONObject(j).getString("size").equals("extralarge")){
                        imageURL = imageJson.getJSONObject(j).getString("#text");
                    }
                }

                mProfileTopArtists.add(new TopArtist(
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

        int counter = 0;
        if (mUserName.equals("se0ko")){
            mDbWrite.delete(
                    ProfileContract.TopArtistsEntry.TABLE_NAME,
                    null,
                    null
            );

            ContentValues topArtistsValues = new ContentValues();

            for (int i = 0; i < mProfileTopArtists.size(); i++){
                topArtistsValues.put(ProfileContract.TopArtistsEntry.COLUMN_ARTIST_ICON_URL, mProfileTopArtists.get(i).getArtistPicURL());
                topArtistsValues.put(ProfileContract.TopArtistsEntry.COLUMN_ARTIST_NAME, mProfileTopArtists.get(i).getArtistName());
                topArtistsValues.put(ProfileContract.TopArtistsEntry.COLUMN_ARTIST_PLAYCOUNT, mProfileTopArtists.get(i).getArtistPlays());

                mDbWrite.insert(
                        ProfileContract.TopArtistsEntry.TABLE_NAME,
                        null,
                        topArtistsValues
                );
                counter++;
            }
            //Log.d(LOG_TAG, "Inserted " + counter + " artists rows");
        }
    }

    @Override
    public Void loadInBackground() {
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

        //Getting recent tracks!

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


        Log.d(LOG_TAG, "Finished loading in background");
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

        ProfileActivity.tracksListLinearLayout.removeAllViews();

        for(int i=0; i<5; i++) {
            View recentTrackView = LayoutInflater.from(mContext).inflate(R.layout.item_recent_tracks_list, null);
            ((TextView) recentTrackView.findViewById(R.id.list_textview))
                    .setText(mProfileRecentTracks.get(i).getTrackArtist() +
                            " - " +
                            mProfileRecentTracks.get(i).getTrackName());

            ((TextView) recentTrackView.findViewById(R.id.recent_tracks_list_date))
                    .setText(mProfileRecentTracks.get(i).getTrackDate());


            ImageView imageView = (ImageView) recentTrackView.findViewById(R.id.list_imageview);

            if (!TextUtils.isEmpty(mProfileRecentTracks.get(i).getTrackImageURL()))
                Picasso.with(mContext).load(mProfileRecentTracks.get(i).getTrackImageURL()).into(imageView);

            ProfileActivity.tracksListLinearLayout.addView(recentTrackView);

            ImageView divider1 = new ImageView(mContext);
            LinearLayout.LayoutParams lp1 =
                    new LinearLayout.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, 1);
            lp1.setMargins(5, 0, 5, 0);
            divider1.setLayoutParams(lp1);
            divider1.setBackgroundColor(Color.rgb(200, 200, 200));
            ProfileActivity.tracksListLinearLayout.addView(divider1);
        }

        ProfileActivity.artistsListLinearLayout.removeAllViews();

        int fullPlays = Integer.parseInt(mProfileTopArtists.get(0).getArtistPlays().replaceAll("plays", ""));
        int currentFullWidth = mShrdPrefs.getInt("full_width", 50);

        for (int i=0; i<mProfileTopArtists.size(); i++){
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_artists_list, null);

            ImageView imageView = (ImageView) view.findViewById(R.id.artists_list_imageview);
            ((TextView) view.findViewById(R.id.artists_list_name_textview))
                    .setText(mProfileTopArtists.get(i).getArtistName());
            ((TextView) view.findViewById(R.id.artists_list_plays_textview))
                    .setText(mProfileTopArtists.get(i).getArtistPlays());

            float percentage = (Integer.parseInt(mProfileTopArtists.get(i).getArtistPlays().replaceAll("plays","")) * 100 / fullPlays);
            //Log.d(LOG_TAG, Float.toString(percentage));

            ImageView relativeBar = (ImageView) view.findViewById(R.id.artists_relativebar_imageview);

            ViewGroup.LayoutParams params = relativeBar.getLayoutParams();
            //params.width = (int) ((percentage * fullWidth)) / 100;
            params.width = (int) ((currentFullWidth * percentage) / 100);
            relativeBar.setLayoutParams(params);

            if (!TextUtils.isEmpty(mProfileTopArtists.get(i).getArtistPicURL()))
                Picasso.with(mContext).load(mProfileTopArtists.get(i).getArtistPicURL()).into(imageView);

            ProfileActivity.artistsListLinearLayout.addView(view);


        }


        Log.d(LOG_TAG, "Result delivered?");
    }
}
