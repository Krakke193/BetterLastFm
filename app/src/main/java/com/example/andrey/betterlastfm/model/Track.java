package com.example.andrey.betterlastfm.model;

/**
 * Created by Andrey on 21.04.2015.
 */
public abstract class Track {
    protected String trackName;
    protected String trackArtist;
    protected String trackImageURL;

    protected abstract String getTrackName();

    protected abstract void setTrackName(String trackName);

    protected abstract String getTrackArtist();

    protected abstract void setTrackArtist(String trackArtist);

    protected abstract String getTrackImageURL();

    protected abstract void setTrackImageURL(String trackImageURL);
}
