package com.example.andrey.betterlastfm.model;

/**
 * Created by Andrey on 08.04.2015.
 */
public class RecentTrack {

    private String trackName;
    private String trackArtist;
    private String trackImageURL;
    private String trackDate;
    private String album;

    public RecentTrack (String trackName, String trackArtist, String album, String trackImageURL, String trackDate){
        this.setTrackArtist(trackArtist);
        this.setTrackName(trackName);
        this.setTrackImageURL(trackImageURL);
        this.setTrackDate(trackDate);
        this.setAlbum(album);
    }

    @Override
    public String toString() {
        return getTrackName() + " - " + getTrackArtist();
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getTrackArtist() {
        return trackArtist;
    }

    public void setTrackArtist(String trackArtist) {
        this.trackArtist = trackArtist;
    }

    public String getTrackImageURL() {
        return trackImageURL;
    }

    public void setTrackImageURL(String trackImageURL) {
        this.trackImageURL = trackImageURL;
    }

    public String getTrackDate() {
        return trackDate;
    }

    public void setTrackDate(String trackDate) {
        this.trackDate = trackDate;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
}
