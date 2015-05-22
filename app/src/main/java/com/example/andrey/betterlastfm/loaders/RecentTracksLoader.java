package com.example.andrey.betterlastfm.loaders;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.andrey.betterlastfm.Util;
import com.example.andrey.betterlastfm.data.ProfileContract;
import com.example.andrey.betterlastfm.data.RecentTracksProvider;
import com.example.andrey.betterlastfm.model.RecentTrack;

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
public class RecentTracksLoader extends AsyncTaskLoader<ArrayList<RecentTrack>> {
    private final String LOG_TAG = RecentTracksLoader.class.getSimpleName();

    private Context mContext;
    private ArrayAdapter<RecentTrack> mListAdapter;
    private String mUserName;

    private ArrayList<RecentTrack> mProfileRecentTracks = new ArrayList<>();

    public RecentTracksLoader(Context context,
                              ArrayAdapter<RecentTrack> listAdapter, String userName){
        super(context);
        this.mContext = context;
        this.mListAdapter = listAdapter;
        this.mUserName = userName;
    }

    @Override
    public ArrayList<RecentTrack> loadInBackground() {
        String profileRecentTracksJsonStr;

        String user = mUserName;
        String format = "json";
        String apiKey = Util.API_KEY;

        String methodTypeRecentTracks = "user.getrecenttracks";

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        if (mUserName.equals(""))
            return null;

        try {
            if (!Util.isInternetAvailable()){
                return null;
            }
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

            mProfileRecentTracks = getRecentTracksFromJson(profileRecentTracksJsonStr);

        } catch (IOException e) {
            return null;

        } catch (JSONException e){
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

        return mProfileRecentTracks;
    }

    private ArrayList<RecentTrack> getRecentTracksFromJson (String tracksJsonStr)
            throws JSONException{
        final String ARTIST = "#text";
        final String NAME = "name";

        ArrayList<RecentTrack> recentTrackArrayList = new ArrayList<>();

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

                recentTrackArrayList.add(new RecentTrack(
                        recentTracksArr.getJSONObject(i).getString(NAME),
                        recentTracksArr.getJSONObject(i).getJSONObject("artist").getString(ARTIST),
                        "",
                        imageURL,
                        recentTrackDate
                ));
            }

            SharedPreferences shrdPrfs = mContext
                    .getSharedPreferences("com.example.andrey.betterlastfm",Context.MODE_PRIVATE);

            String username = shrdPrfs.getString("username", Util.ERROR);
            if (username.equals(Util.ERROR))
                Toast.makeText(mContext, "Ooops! something went wrong!", Toast.LENGTH_SHORT).show();

            if (mUserName.equals(username)){
                ContentValues recentTrackValues = new ContentValues();

                mContext.getContentResolver()
                        .delete(RecentTracksProvider.TRACKS_CONTENT_URI, null, null);

                for (int i = 9; i >= 0; i--){
                    recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ICON_URL,
                            recentTrackArrayList.get(i).getTrackImageURL());
                    recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ARTIST,
                            recentTrackArrayList.get(i).getTrackArtist());
                    recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_NAME,
                            recentTrackArrayList.get(i).getTrackName());
                    recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ALBUM,
                            recentTrackArrayList.get(i).getAlbum());
                    recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_TIMESTAMP,
                            recentTrackArrayList.get(i).getTrackDate());
                    recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_SCROBBLEABLE_FLAG, 0);

                    mContext.getContentResolver()
                            .insert(RecentTracksProvider.TRACKS_CONTENT_URI, recentTrackValues);
                }
            }
        } catch (JSONException e) {
            return null;
        }

        return recentTrackArrayList;
    }

    @Override
    public void deliverResult(ArrayList<RecentTrack> data) {
        super.deliverResult(data);
    }
}
