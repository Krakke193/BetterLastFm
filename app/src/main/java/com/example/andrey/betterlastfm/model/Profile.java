package com.example.andrey.betterlastfm.model;

import java.util.ArrayList;

/**
 * Created by Andrey on 22.04.2015.
 */
public class Profile {
    private ArrayList<String> profileHeaderArray;
    private ArrayList<RecentTrack> profileRecentTracks;
    private ArrayList<TopArtist> profileTopArtists;

    public Profile(ArrayList<String> profileHeaderArray, ArrayList<RecentTrack> profileRecentTracks, ArrayList<TopArtist> profileTopArtists){
        this.setProfileHeaderArray(profileHeaderArray);
        this.setProfileRecentTracks(profileRecentTracks);
        this.setProfileTopArtists(profileTopArtists);
    }

    public ArrayList<String> getProfileHeaderArray() {
        return profileHeaderArray;
    }

    public void setProfileHeaderArray(ArrayList<String> profileHeaderArray) {
        this.profileHeaderArray = profileHeaderArray;
    }

    public ArrayList<RecentTrack> getProfileRecentTracks() {
        return profileRecentTracks;
    }

    public void setProfileRecentTracks(ArrayList<RecentTrack> profileRecentTracks) {
        this.profileRecentTracks = profileRecentTracks;
    }

    public ArrayList<TopArtist> getProfileTopArtists() {
        return profileTopArtists;
    }

    public void setProfileTopArtists(ArrayList<TopArtist> profileTopArtists) {
        this.profileTopArtists = profileTopArtists;
    }
}
