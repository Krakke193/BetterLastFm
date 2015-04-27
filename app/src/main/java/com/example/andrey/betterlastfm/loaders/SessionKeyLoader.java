package com.example.andrey.betterlastfm.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

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
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrey on 09.04.2015.
 */
public class SessionKeyLoader extends AsyncTaskLoader<String> {
    private final String LOG_TAG = SessionKeyLoader.class.getSimpleName();

    private Context mContext;
    private String username;
    private String password;
    private String apiKey;
    private String apiSignature;
    private String loginJsonStr;

    public SessionKeyLoader(Context context, String username, String password,
                            String apiKey, String apiSignature){
        super(context);
        this.mContext = context;
        this.username = username;
        this.password = password;
        this.apiKey = apiKey;
        this.apiSignature = apiSignature;
    }

    @Override
    public String loadInBackground() {
        final String BASE_URL = "https://ws.audioscrobbler.com/2.0/?";
        final String METHOD = "method";
        final String FORMAT = "format";

        String format = "json";
        String method = "auth.getMobileSession";

        BufferedReader reader;
        HttpPost httpPost = new HttpPost(BASE_URL + METHOD + "=" + method + "&"
            + FORMAT + "=" + format);
        HttpClient httpclient = new DefaultHttpClient();

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
        nameValuePairs.add(new BasicNameValuePair("username", username));
        nameValuePairs.add(new BasicNameValuePair("password", password));
        nameValuePairs.add(new BasicNameValuePair("api_key", apiKey));
        nameValuePairs.add(new BasicNameValuePair("api_sig", apiSignature));


        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httpPost);

            InputStream inputStream = response.getEntity().getContent();
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

            loginJsonStr = buffer.toString();

            if (isErrorGetFromJson(loginJsonStr))
                return Util.ERROR;
            else {
                String sessionKey = parseSessionKeyFromJson(loginJsonStr);
                Util.setSessionKey(mContext, sessionKey);

                String username = parseUsernameFromJson(loginJsonStr);
                Util.setUsername(mContext, username);

                return sessionKey;
            }



        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void deliverResult(String data) {
        super.deliverResult(data);
    }

    private boolean isErrorGetFromJson(String jsonStr){
        try {
            JSONObject rootJson = new JSONObject(jsonStr);

            try {
                String numberOfError = rootJson.getString("error");
                String errMessg = rootJson.getString("message");
                //errMessage = numberOfError + errMessg;
                Log.d(LOG_TAG,"Error here");
                return true;
            } catch (JSONException e){
                return false;
            }

        } catch (JSONException e){
            e.printStackTrace();
        }
        return true;

    }

    private String parseSessionKeyFromJson(String loginJsonStr) throws JSONException {
        try{
            JSONObject loginJson = new JSONObject(loginJsonStr);

            Log.d(LOG_TAG, "Login json str: " + loginJsonStr);
            JSONObject sessionJson = loginJson.getJSONObject("session");

            return sessionJson.getString("key");

        } catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    private String parseUsernameFromJson(String loginJsonStr) throws JSONException {
        try{
            JSONObject loginJson = new JSONObject(loginJsonStr);
            JSONObject sessionJson = loginJson.getJSONObject("session");

            return sessionJson.getString("name");
        } catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }
}
