package com.example.andrey.betterlastfm.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.andrey.betterlastfm.services.ScrobbleService;

/**
 * Created by Andrey on 11.04.2015.
 */
public class CommonMusicReceiver extends BroadcastReceiver {
    private final String LOG_TAG = CommonMusicReceiver.class.getSimpleName();

    private static String storedTrack = "";

    public static final String EXTRA_TRACK_NAME = "EXTRA_TACK_NAME";
    public static final String EXTRA_ARTIST_NAME = "EXTRA_ARTIST_NAME";
    public static final String EXTRA_ALBUM_NAME = "EXTRA_ALBUM_NAME";
    public static final String EXTRA_TRACK_DURATION = "EXTRA_TACK_DURATION";

    @Override
    public void onReceive(Context context, Intent originalIntent) {
        if (!storedTrack.equals(originalIntent.getStringExtra("track"))){
            Intent service = new Intent(context, ScrobbleService.class);
            service.putExtra(EXTRA_TRACK_NAME, originalIntent.getStringExtra("track"));
            service.putExtra(EXTRA_ARTIST_NAME, originalIntent.getStringExtra("artist"));
            service.putExtra(EXTRA_ALBUM_NAME, originalIntent.getStringExtra("album"));
            service.putExtra(EXTRA_TRACK_DURATION, originalIntent.getLongExtra("duration", 0L));

            context.startService(service);

            storedTrack = originalIntent.getStringExtra("track");
        }
    }
}