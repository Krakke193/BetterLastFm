package com.example.andrey.betterlastfm.data;

import android.provider.BaseColumns;

/**
 * Created by andrey on 13.03.15.
 */
public class ProfileContract {

    public ProfileContract(){}

    public static abstract class ProfileEntry implements BaseColumns{
        public static final String TABLE_NAME = "profile";
        public static final String USER_ID = "user_id";
        public static final String COLUMN_HEADER_ID = "header_id"; // Ссылка на таблицу с инфой профиля!!
        public static final String COLUMN_RECENT_TRACKS_ID = "recent_tracks_id"; // Сслыка на последние композиции!
        public static final String COLUMN_TOP_ARTISTS_ID = "top_artists_id"; // Ссылка на

        /**
         * TODO: Three public static final String fields for three inner data bases:
         * TODO: headerTable, recentTracks, topArtists
         */

    }

    public static abstract class HeaderEntry implements BaseColumns{
        public static final String TABLE_NAME = "header";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_HEADER_ICON_URL = "header_icon_url";
        public static final String COLUMN_HEADER_NAME = "header_name";
        public static final String COLUMN_HEADER_REAL_NAME = "header_real_name";
        public static final String COLUMN_HEADER_AGE = "header_age";
        public static final String COLUMN_HEADER_COUNTRY = "header_country";
        public static final String COLUMN_HEADER_PLAYCOUNT = "header_playcount";
        public static final String COLUMN_HEADER_REGISTRY_DATE = "header_registry_date";
    }

    public static abstract class RecentTracksEntry implements BaseColumns{
        public static final String TABLE_NAME = "recent_tracks";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_TRACK_ARTIST = "track_artist";
        public static final String COLUMN_TRACK_NAME = "track_title";
        public static final String COLUMN_TRACK_TIMESTAMP = "track_timestamp";
        public static final String COLUMN_TRACK_ICON_URL = "track_icon_url";
        public static final String COLUMN_SCROBBLEABLE_FLAG = "scrobbleable_flag";
    }

    public static abstract class TopArtistsEntry implements BaseColumns{
        public static final String TABLE_NAME = "top_artists";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_ARTIST_NAME = "artist_title";
        public static final String COLUMN_ARTIST_PLAYCOUNT = "artist_playcount";
        public static final String COLUMN_ARTIST_ICON_URL = "artist_icon_url";
    }
}
