package com.example.andrey.betterlastfm.services;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.example.andrey.betterlastfm.R;
import com.example.andrey.betterlastfm.data.ProfileContract;
import com.example.andrey.betterlastfm.data.RecentTracksProvider;
import com.example.andrey.betterlastfm.receivers.CommonMusicReceiver;

import java.sql.Timestamp;

/**
 * Created by Andrey on 11.04.2015.
 */
public class ScrobbleService extends Service {
    private final String LOG_TAG = ScrobbleService.class.getSimpleName();

    private final String DELETE_IN_TRACK = "Track name: ";
    private final String DELETE_IN_ARTIST = "Artist name: ";

    public static final String START_PLAYING = "com.example.andrey.betterlastfm.services.action.START_PLAYING";
    public static final String STOP_PLAYING = "com.example.andrey.betterlastfm.services.action.STOP_PLAYING";


//    public ScrobbleService(){
//        super("ScrobblingService");
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String trackName = intent.getStringExtra(CommonMusicReceiver.EXTRA_TRACK_NAME);
            String artistName = intent.getStringExtra(CommonMusicReceiver.EXTRA_ARTIST_NAME);

            String trackTimestamp = Long.toString(System.currentTimeMillis() / 1000L);

            String trackTitleInfo = trackName.replaceAll(DELETE_IN_TRACK, "");
            String trackArtistInfo = artistName.replaceAll(DELETE_IN_ARTIST, "");

            Log.d(LOG_TAG, trackTitleInfo + " - " + trackArtistInfo);

            ContentValues recentTrackValues = new ContentValues();
            recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ICON_URL, R.drawable.female);
            recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ARTIST, trackArtistInfo);
            recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_NAME, trackTitleInfo);
            recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_TIMESTAMP, trackTimestamp);
            recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_SCROBBLEABLE_FLAG, 1);
//            recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ICON_URL,
//                    profileRecentTracksUrlArray[i]);
//            recentTrackValues.put(ProfileContract.RecentTracksEntry.COLUMN_TRACK_NAME,
//                    profileRecentTracksArray[i]);
//
//            Uri newUri = mContext.getContentResolver()
//                    .insert(RecentTracksProvider.TRACKS_CONTENT_URI, recentTrackValues);
            //getApplicationContext().getContentResolver().delete(RecentTracksProvider.TRACKS_CONTENT_URI, null, null);
            getApplicationContext().getContentResolver().insert(RecentTracksProvider.TRACKS_CONTENT_URI, recentTrackValues);
        }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void handleActionPlay(){

    }

    protected void handleActionStop(){

    }
}
