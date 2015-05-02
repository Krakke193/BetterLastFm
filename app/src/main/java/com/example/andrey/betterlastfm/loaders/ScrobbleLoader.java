package com.example.andrey.betterlastfm.loaders;

import android.content.AsyncTaskLoader;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.andrey.betterlastfm.Util;
import com.example.andrey.betterlastfm.data.ProfileContract;
import com.example.andrey.betterlastfm.data.RecentTracksProvider;
import com.example.andrey.betterlastfm.model.RecentTrack;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Andrey on 12.04.2015.
 */
public class ScrobbleLoader extends AsyncTaskLoader<Void> {
    private final String LOG_TAG = ScrobbleLoader.class.getSimpleName();

    private Context mContext;
    private String mApiKey;

    private ArrayList<RecentTrack> mProfileRecentTracks = new ArrayList<>();

    private boolean errFlag = false;
    private int scrobbledTracksCount = 0;
    private String errMessage;
    private String tmpArtistsForMD5;
    private String tmpAlbumsForMD5;
    private String tmpTimestampsForMD5;
    private String tmpTracksForMD5;

    public ScrobbleLoader(Context context, String apiKey, ArrayList<RecentTrack> profileRecentTracks){
        super(context);

        this.mContext = context;
        this.mApiKey = apiKey;
        this.mProfileRecentTracks = profileRecentTracks;
    }

    @Override
    public Void loadInBackground() {
        if (mProfileRecentTracks.size() > 10){
            ArrayList<RecentTrack> listRecentTracks = new ArrayList<>();
            for (int i=0; i<mProfileRecentTracks.size(); i++){

                listRecentTracks.add(mProfileRecentTracks.get(i));

                if (i % 9 == 0 && i != 0){
                    performScrobble(listRecentTracks);
                    listRecentTracks.clear();
                }
            }
            performScrobble(listRecentTracks);
        } else
            performScrobble(null);

        return null;
    }

    private void performScrobble(ArrayList<RecentTrack> limitedRecentTracks){
        ArrayList<RecentTrack> profileRecentTracks;

        if (limitedRecentTracks == null){
            profileRecentTracks = mProfileRecentTracks;
        } else {
            profileRecentTracks = limitedRecentTracks;
        }

        final String BASE_URL = "https://ws.audioscrobbler.com/2.0/";
        final String METHOD = "method";
        final String FORMAT = "format";

        String format = "json";
        String method = "track.scrobble";

        String tmpStringForMD5;

        BufferedReader reader = null;
        HttpPost httpPost = new HttpPost(BASE_URL + "?" + FORMAT + "=" + format);
        HttpClient httpclient = new DefaultHttpClient();

        SharedPreferences shrdPrfs = mContext
                .getSharedPreferences("com.example.andrey.betterlastfm",Context.MODE_PRIVATE);
        String sessionKey = shrdPrfs.getString("session_key", "ERROR");

        for (int i=0; i<profileRecentTracks.size(); i++){
            tmpAlbumsForMD5 += "album" +
                    "[" +
                    Integer.toString(i) +
                    "]" +
                    profileRecentTracks.get(i).getAlbum();

            tmpArtistsForMD5 += "artist" +
                    "[" +
                    Integer.toString(i) +
                    "]" +
                    profileRecentTracks.get(i).getTrackArtist();

            tmpTimestampsForMD5 += "timestamp" +
                    "[" +
                    Integer.toString(i) +
                    "]" +
                    profileRecentTracks.get(i).getTrackDate();

            tmpTracksForMD5 += "track" +
                    "[" +
                    Integer.toString(i) +
                    "]" +
                    profileRecentTracks.get(i).getTrackName();
        }

        tmpStringForMD5 = tmpAlbumsForMD5.replaceAll("null", "") +
                "api_key" + mApiKey +
                tmpArtistsForMD5.replaceAll("null", "") +
                "method" + method +
                "sk" + sessionKey +
                tmpTimestampsForMD5.replaceAll("null", "") +
                tmpTracksForMD5.replaceAll("null", "") +
                Util.SECRET;

        tmpAlbumsForMD5 = "";
        tmpArtistsForMD5 = "";
        tmpTracksForMD5 = "";
        tmpTimestampsForMD5 = "";

        String apiSignature = Util.md5(tmpStringForMD5);
        Log.d(LOG_TAG, "String for md5: " + tmpStringForMD5);
        Log.d(LOG_TAG, "Api signature: " + apiSignature);

        if (sessionKey.equals("ERROR")){
            Log.d(LOG_TAG, "Error in username: something wrong!");
            return;
        }

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        for (int i=0; i<profileRecentTracks.size(); i++){
            nameValuePairs.add(new BasicNameValuePair(
                    "album" +
                            "[" +
                            Integer.toString(i) +
                            "]",
                    profileRecentTracks.get(i).getAlbum()
            ));
            nameValuePairs.add(new BasicNameValuePair(
                    "artist" +
                            "[" +
                            Integer.toString(i) +
                            "]",
                    profileRecentTracks.get(i).getTrackArtist())
            );
            nameValuePairs.add(new BasicNameValuePair(
                    "track" +
                            "[" +
                            Integer.toString(i) +
                            "]",
                    profileRecentTracks.get(i).getTrackName())
            );
            nameValuePairs.add(new BasicNameValuePair(
                    "timestamp" +
                            "[" +
                            Integer.toString(i) +
                            "]",
                    profileRecentTracks.get(i).getTrackDate())
            );

            scrobbledTracksCount++;
        }

        nameValuePairs.add(new BasicNameValuePair("api_key", mApiKey));
        Log.d(LOG_TAG, "api_key" + mApiKey);
        nameValuePairs.add(new BasicNameValuePair("api_sig", apiSignature));
        Log.d(LOG_TAG, "api_sig" + apiSignature);
        nameValuePairs.add(new BasicNameValuePair("sk", sessionKey));
        Log.d(LOG_TAG, "sk" + sessionKey);
        nameValuePairs.add(new BasicNameValuePair("method", method));
        Log.d(LOG_TAG, "method" + method);

        try{
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            HttpResponse response = httpclient.execute(httpPost);

            InputStream inputStream = response.getEntity().getContent();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null)
                return;
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0)
                return;

            String jsonStr = buffer.toString();

            errFlag = isErrorGetFromJson(jsonStr);

            Log.d(LOG_TAG, jsonStr);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private boolean isErrorGetFromJson(String jsonStr){
        try {
            JSONObject rootJson = new JSONObject(jsonStr);

            try {
                String numberOfError = rootJson.getString("error");
                String errMessg = rootJson.getString("message");
                errMessage = numberOfError + errMessg;
                return true;
            } catch (JSONException e){
                return false;
            }

        } catch (JSONException e){
            e.printStackTrace();
        }
        return true;

    }

    @Override
    public void deliverResult(Void data) {
        super.deliverResult(data);

        if (errFlag) {
            Toast.makeText(mContext, errMessage, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext,
                    "Succesfully scrobbled " + Integer.toString(scrobbledTracksCount) + "tracks",
                    Toast.LENGTH_SHORT)
                    .show();
            ContentValues contentValues = new ContentValues();
            contentValues.put(ProfileContract.RecentTracksEntry.COLUMN_SCROBBLEABLE_FLAG, 0);
            int cnt = mContext
                    .getContentResolver()
                    .update(RecentTracksProvider.TRACKS_CONTENT_URI, contentValues, null, null);

            Log.d(LOG_TAG, "Scrobble status removed from " + Integer.toString(cnt) + " rows");
        }
    }
}
