package com.example.andrey.betterlastfm.network;

import android.text.Html;

import com.example.andrey.betterlastfm.model.Artist;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Andrey on 22.04.2015.
 */
public class ArtistDeserializer implements JsonDeserializer<Artist> {
    private Artist artist;

    @Override
    public Artist deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonObject artistJson = jsonObject.getAsJsonObject("artist");

        artist = new Artist();

        // Name
        artist.setArtistName(artistJson.getAsJsonPrimitive("name").getAsString());

        // Listeners
        JsonObject statsJson = artistJson.getAsJsonObject("stats");
        artist.setArtistListeners(statsJson.getAsJsonPrimitive("listeners").getAsString());

        // Playcounts
        artist.setArtistPlaycount(statsJson.getAsJsonPrimitive("playcount").getAsString());

        // Image
        JsonArray artistPics = artistJson.getAsJsonArray("image");
        for (JsonElement artistPic : artistPics)
            if (artistPic.getAsJsonObject().get("size").getAsString().equals("mega"))
                artist.setArtistPic(artistPic.getAsJsonObject().get("#text").getAsString());

        // Tags
        ArrayList<String> artistTags = new ArrayList<>();
        JsonObject tagsJson = artistJson.getAsJsonObject("tags");
        JsonArray tags = tagsJson.getAsJsonArray("tag");
        for (JsonElement tag : tags)
            artistTags.add(tag.getAsJsonObject().get("name").getAsString());
        artist.setArtistTags(artistTags);

        // Info
        JsonObject bioJson = artistJson.getAsJsonObject("bio");
        artist.setArtistInfo(Html.fromHtml((String) bioJson.get("summary").getAsString()).toString());

        return artist;
    }
}
