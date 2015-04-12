package com.example.andrey.betterlastfm.model;

/**
 * Created by Andrey on 08.04.2015.
 */
public class TopArtist {

    public String artistInfo;
    public String artistPlaycount;
    public String artistImageURL;

    public TopArtist (String artistName, String artistPlaycount, String artistImageURL){
        this.artistInfo = artistName;
        this.artistPlaycount = artistPlaycount;
        this.artistImageURL = artistImageURL;
    }
}
