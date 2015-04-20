package com.example.andrey.betterlastfm.model;

/**
 * Created by Andrey on 20.04.2015.
 */
public class TopArtist {
    private String artistName;
    private String artistPlays;
    private String artistPicURL;

    public TopArtist(String artistName, String artistPlays, String artistPicURL){
        this.setArtistName(artistName);
        this.setArtistPlays(artistPlays);
        this.setArtistPicURL(artistPicURL);
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistPlays() {
        return artistPlays;
    }

    public void setArtistPlays(String artistPlays) {
        this.artistPlays = artistPlays;
    }

    public String getArtistPicURL() {
        return artistPicURL;
    }

    public void setArtistPicURL(String artistPicURL) {
        this.artistPicURL = artistPicURL;
    }
}
