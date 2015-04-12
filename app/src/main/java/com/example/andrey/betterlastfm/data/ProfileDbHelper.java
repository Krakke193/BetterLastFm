package com.example.andrey.betterlastfm.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by andrey on 13.03.15.
 */
public class ProfileDbHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 5;
    static final String DATABASE_NAME = "profile.db";

    public ProfileDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_PROFILE_TABLE = "CREATE TABLE " +
                ProfileContract.ProfileEntry.TABLE_NAME + " (" +
                ProfileContract.ProfileEntry.USER_ID + "TEXT UNIQUE NOT NULL, " +
                ProfileContract.ProfileEntry._ID + " INTEGER PRIMARY KEY," +
                ProfileContract.ProfileEntry.COLUMN_HEADER_ID + " INTEGER NOT NULL, " +
                ProfileContract.ProfileEntry.COLUMN_RECENT_TRACKS_ID + " INTEGER NOT NULL, " +
                ProfileContract.ProfileEntry.COLUMN_TOP_ARTISTS_ID + " INTEGER NOT NULL, " +

                " FOREIGN KEY (" + ProfileContract.ProfileEntry.COLUMN_HEADER_ID + ") REFERENCES " +
                ProfileContract.HeaderEntry.TABLE_NAME + " (" + ProfileContract.HeaderEntry._ID + "), " +

                " FOREIGN KEY (" + ProfileContract.ProfileEntry.COLUMN_RECENT_TRACKS_ID + ") REFERENCES " +
                ProfileContract.RecentTracksEntry.TABLE_NAME + " (" + ProfileContract.RecentTracksEntry._ID + "), " +

                " FOREIGN KEY (" + ProfileContract.ProfileEntry.COLUMN_TOP_ARTISTS_ID + ") REFERENCES " +
                ProfileContract.TopArtistsEntry.TABLE_NAME + " (" + ProfileContract.TopArtistsEntry._ID + ")" +

                " );";

        final String SQL_CREATE_HEADER_TABLE = "CREATE TABLE " +
                ProfileContract.HeaderEntry.TABLE_NAME + " (" +
                ProfileContract.HeaderEntry._ID + " INTEGER PRIMARY KEY," +
                ProfileContract.HeaderEntry.COLUMN_HEADER_ICON_URL + " TEXT NOT NULL, " +
                ProfileContract.HeaderEntry.COLUMN_HEADER_NAME + " TEXT NOT NULL, " +
                ProfileContract.HeaderEntry.COLUMN_HEADER_REAL_NAME + " TEXT NOT NULL, " +
                ProfileContract.HeaderEntry.COLUMN_HEADER_AGE + " TEXT NOT NULL, " +
                ProfileContract.HeaderEntry.COLUMN_HEADER_COUNTRY + " TEXT NOT NULL, " +
                ProfileContract.HeaderEntry.COLUMN_HEADER_PLAYCOUNT + " TEXT NOT NULL, " +
                ProfileContract.HeaderEntry.COLUMN_HEADER_REGISTRY_DATE + " TEXT NOT NULL " + " );";

        final String SQL_CREATE_RECENT_TRACKS_TABLE = "CREATE TABLE " +
                ProfileContract.RecentTracksEntry.TABLE_NAME + " (" +
                ProfileContract.RecentTracksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ProfileContract.RecentTracksEntry.COLUMN_TRACK_ARTIST + " TEXT NOT NULL, " +
                ProfileContract.RecentTracksEntry.COLUMN_TRACK_NAME + " TEXT NOT NULL, " +
                ProfileContract.RecentTracksEntry.COLUMN_TRACK_TIMESTAMP + " TEXT NOT NULL, " +
                ProfileContract.RecentTracksEntry.COLUMN_TRACK_ICON_URL + " TEXT NOT NULL, " +
                ProfileContract.RecentTracksEntry.COLUMN_SCROBBLEABLE_FLAG + " INTEGER NOT NULL" + " );";

        final String SQL_CREATE_TOP_ARTISTS_TABLE = "CREATE TABLE " +
                ProfileContract.TopArtistsEntry.TABLE_NAME + " (" +
                ProfileContract.TopArtistsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ProfileContract.TopArtistsEntry.COLUMN_ARTIST_NAME + " TEXT NOT NULL, " +
                ProfileContract.TopArtistsEntry.COLUMN_ARTIST_PLAYCOUNT + " INT NOT NULL, " +
                ProfileContract.TopArtistsEntry.COLUMN_ARTIST_ICON_URL + " TEXT NOT NULL " + " );";

        db.execSQL(SQL_CREATE_PROFILE_TABLE);
        db.execSQL(SQL_CREATE_HEADER_TABLE);
        db.execSQL(SQL_CREATE_RECENT_TRACKS_TABLE);
        db.execSQL(SQL_CREATE_TOP_ARTISTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ProfileContract.ProfileEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProfileContract.HeaderEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProfileContract.RecentTracksEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProfileContract.TopArtistsEntry.TABLE_NAME);
        onCreate(db);
    }

    public void onDelete(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + ProfileContract.ProfileEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProfileContract.HeaderEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProfileContract.RecentTracksEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProfileContract.TopArtistsEntry.TABLE_NAME);
    }
}
