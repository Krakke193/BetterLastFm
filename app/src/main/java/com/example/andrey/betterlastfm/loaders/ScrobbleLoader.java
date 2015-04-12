package com.example.andrey.betterlastfm.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.andrey.betterlastfm.Util;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrey on 12.04.2015.
 */
public class ScrobbleLoader extends AsyncTaskLoader<Void> {
    private final String LOG_TAG = ScrobbleLoader.class.getSimpleName();

    private Context mContext;
    private String mApiKey;
    private ArrayList<String> mTracks;
    private ArrayList<String> mArtists;
    private ArrayList<String> mTimestamps;

    private String tmpArtistsForMD5;
    private String tmpTimestampsForMD5;
    private String tmpTracksForMD5;

    public ScrobbleLoader(Context context, String apiKey, ArrayList<String> tracks,
                          ArrayList<String> artists, ArrayList<String> timestamps){
        super(context);
        this.mContext = context;
        this.mApiKey = apiKey;
        this.mTracks = tracks;
        this.mArtists = artists;
        this.mTimestamps = timestamps;
    }

    @Override
    public Void loadInBackground() {
        final String BASE_URL = "https://ws.audioscrobbler.com/2.0/";
        final String METHOD = "method";
        final String FORMAT = "format";

        String format = "json";
        String method = "track.scrobble";

        BufferedReader reader = null;
        HttpPost httpPost = new HttpPost(BASE_URL + "?" + FORMAT + "=" + format);
        HttpClient httpclient = new DefaultHttpClient();

        SharedPreferences shrdPrfs = mContext
                .getSharedPreferences("com.example.andrey.betterlastfm",Context.MODE_PRIVATE);
        String sessionKey = shrdPrfs.getString("session_key", "ERROR");

        for (int i=0; i<mArtists.size(); i++){
            tmpArtistsForMD5 += "artist" + "[" + Integer.toString(i) + "]" + mArtists.get(i);
        }
        for (int i=0; i<mTimestamps.size(); i++){
            tmpTimestampsForMD5 += "timestamp" + "[" + Integer.toString(i) + "]" + mTimestamps.get(i);
        }
        for (int i=0; i<mTracks.size(); i++){
            tmpTracksForMD5 += "track" + "[" + Integer.toString(i) + "]" + mTracks.get(i);
        }


        String tmpStringForMD5 = "api_key" + mApiKey + tmpArtistsForMD5.replaceAll("null", "") + "method" + method +
                "sk" + sessionKey + tmpTimestampsForMD5.replaceAll("null", "") + tmpTracksForMD5.replaceAll("null", "") + Util.SECRET;
        String apiSignature = Util.md5(tmpStringForMD5);
        Log.d(LOG_TAG, "String for md5: " + tmpStringForMD5);
        Log.d(LOG_TAG, "Api signature: " + apiSignature);

        if (sessionKey.equals("ERROR")){
            Log.d(LOG_TAG, "Error in username: something wrong!");
            return null;
        }

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        for (int i=0; i<mArtists.size(); i++){
            nameValuePairs.add(
                    new BasicNameValuePair("artist" + "[" + Integer.toString(i) + "]", mArtists.get(i))

            );
            Log.d(LOG_TAG, "artist" + "[" + Integer.toString(i) + "]" + mArtists.get(i));
        }
        for (int i=0; i<mTracks.size(); i++){
            nameValuePairs.add(
                    new BasicNameValuePair("track" + "[" + Integer.toString(i) + "]", mTracks.get(i))
            );
            Log.d(LOG_TAG, "track" + "[" + Integer.toString(i) + "]" + mTracks.get(i));
        }
        for (int i=0; i<mTimestamps.size(); i++){
            nameValuePairs.add(
                    new BasicNameValuePair("timestamp" + "[" + Integer.toString(i) + "]", mTimestamps.get(i))
            );
            Log.d(LOG_TAG, "timestamp" + "[" + Integer.toString(i) + "]" + mTimestamps.get(i));
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
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httpPost);

            InputStream inputStream = response.getEntity().getContent();
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

            String jsonStr = buffer.toString();

            Log.d(LOG_TAG, jsonStr);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        // array of tracks;
        // array of artists;
        // array of timestamps;
        // api_key;
        // api_sig;
        // sessionkey
        // method;

//        nameValuePairs.add(new BasicNameValuePair("api_key", apiKey));
//        nameValuePairs.add(new BasicNameValuePair("api_sig", apiSignature));
        return null;
    }


}
