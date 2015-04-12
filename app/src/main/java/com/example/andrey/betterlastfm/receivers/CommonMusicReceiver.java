package com.example.andrey.betterlastfm.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.andrey.betterlastfm.services.ScrobbleService;

/**
 * Created by Andrey on 11.04.2015.
 */
public class CommonMusicReceiver extends BroadcastReceiver {
    private final String LOG_TAG = CommonMusicReceiver.class.getSimpleName();

    private static boolean flag = false;
    private static String storedTrack = "";

    public static final String PLAYSTATE_CHANGED = "com.android.music.playstatechanged";
    public static final String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";
    public static final String PAUSE_ACTION = "com.android.music.musicservicecommand.pause";
    public static final String PREVIOUS_ACTION = "com.android.music.musicservicecommand.previous";
    public static final String NEXT_ACTION = "com.android.music.musicservicecommand.next";
    public static final String EXTRA_TRACK_NAME = "EXTRA_TACK_NAME";
    public static final String EXTRA_ARTIST_NAME = "EXTRA_ARTIST_NAME";
    public static final String EXTRA_TRACK_DURATION = "EXTRA_TACK_DURATION";

    @Override
    public void onReceive(Context context, Intent originalIntent) {




        if (!storedTrack.equals(originalIntent.getStringExtra("track"))){


            Log.d(LOG_TAG, "playing");
            Log.d(LOG_TAG, originalIntent.getAction());

            Log.d(LOG_TAG, "Player name: " + originalIntent.getAction().substring(0, originalIntent.getAction().lastIndexOf('.')));
            Log.d(LOG_TAG, "Track name: " + originalIntent.getStringExtra("track"));
            Log.d(LOG_TAG, "Artist name: " + originalIntent.getStringExtra("artist"));
            Log.d(LOG_TAG, "Track duration: " + Long.toString(originalIntent.getLongExtra("duration", 0L)));
            Log.d(LOG_TAG, "*************************************************************************");

            Intent service = new Intent(context, ScrobbleService.class);
            service.putExtra(EXTRA_TRACK_NAME, originalIntent.getStringExtra("track"));
            service.putExtra(EXTRA_ARTIST_NAME, originalIntent.getStringExtra("artist"));
            service.putExtra(EXTRA_TRACK_DURATION, originalIntent.getLongExtra("duration", 0L));
            context.startService(service);

            //service.setAction(ScrobbleService.START_PLAYING);


            flag = true;
            storedTrack = originalIntent.getStringExtra("track");
        }



    }
}