package com.example.andrey.betterlastfm.model;

import java.util.ArrayList;

/**
 * Created by Andrey on 21.04.2015.
 */
public class Artist {
    private String artistName;
    private String artistListeners;
    private String artistPlaycount;
    private String artistPic;
    private String artistInfo;
    private ArrayList<String> artistTags;
    private ArrayList<Track> artistTopTracks;
    private ArrayList<Album> artistAlbums;

    public Artist(){

    }

    public Artist(String artistPic, String artistInfo, ArrayList<String> artistTags,
                  ArrayList<Track> artistTopTracks, ArrayList<Album> artistAlbums){
        this.setArtistPic(artistPic);
        this.setArtistInfo(artistInfo);
        this.setArtistTags(artistTags);
        this.setArtistTopTracks(artistTopTracks);
        this.setArtistAlbums(artistAlbums);
    }

    public String getArtistPic() {
        return artistPic;
    }

    public void setArtistPic(String artistPic) {
        this.artistPic = artistPic;
    }

    public String getArtistInfo() {
        return artistInfo;
    }

    public void setArtistInfo(String artistInfo) {
        this.artistInfo = artistInfo;
    }

    public ArrayList<Album> getArtistAlbums() {
        return artistAlbums;
    }

    public void setArtistAlbums(ArrayList<Album> artistAlbums) {
        this.artistAlbums = artistAlbums;
    }

    public ArrayList<String> getArtistTags() {
        return artistTags;
    }

    public void setArtistTags(ArrayList<String> artistTags) {
        this.artistTags = artistTags;
    }

    public ArrayList<Track> getArtistTopTracks() {
        return artistTopTracks;
    }

    public void setArtistTopTracks(ArrayList<Track> artistTopTracks) {
        this.artistTopTracks = artistTopTracks;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistListeners() {
        return artistListeners;
    }

    public void setArtistListeners(String artistListeners) {
        this.artistListeners = artistListeners;
    }

    public String getArtistPlaycount() {
        return artistPlaycount;
    }

    public void setArtistPlaycount(String artistPlaycount) {
        this.artistPlaycount = artistPlaycount;
    }
}