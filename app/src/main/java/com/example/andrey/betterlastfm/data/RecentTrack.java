package com.example.andrey.betterlastfm.data;

/**
 * Created by Andrey on 08.04.2015.
 */
public class RecentTrack {

    public String trackInfo;
    public String trackImageURL;

    public RecentTrack (String trackInfo, String trackImageURL){
        this.trackInfo = trackInfo;
        this.trackImageURL = trackImageURL;
    }

    @Override
    public String toString() {
        return trackInfo;
    }
}
