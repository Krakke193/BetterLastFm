package com.example.andrey.betterlastfm.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.w3c.dom.Text;

/**
 * Created by Andrey on 07.04.2015.
 */
public class RecentTracksProvider extends ContentProvider {
    private final String LOG_TAG = RecentTracksProvider.class.getSimpleName();

    private ProfileDbHelper mProfileDbHelper;
    private SQLiteDatabase db;

    public static final String AUTHORITY = "com.example.andrey.betterlastfm";
    public static final String TRACKS_PATH = "tracks";
    public static final Uri TRACKS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TRACKS_PATH);

    // Типы данных
    public static final String TRACKS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + TRACKS_PATH;
    public static final String TRACKS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + TRACKS_PATH;

    // общий Uri
    public static final int URI_TRACKS = 1;
    // Uri с указанным ID
    public static final int URI_TRACKS_ID = 2;

    // описание и создание UriMatcher
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, TRACKS_PATH, URI_TRACKS);
        uriMatcher.addURI(AUTHORITY, TRACKS_PATH + "/#", URI_TRACKS_ID);
    }

    @Override
    public boolean onCreate() {
        mProfileDbHelper = new ProfileDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        switch (uriMatcher.match(uri)){
            case URI_TRACKS:
                break;
            case URI_TRACKS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = ProfileContract.RecentTracksEntry._ID + " = " + id;
                } else {
                    selection = selection +
                            " AND " + ProfileContract.RecentTracksEntry._ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = mProfileDbHelper.getWritableDatabase();
        Cursor cursor = db.query(ProfileContract.RecentTracksEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        // просим ContentResolver уведомлять этот курсор
        // об изменениях данных в TRACKS_CONTENT_URI
        cursor.setNotificationUri(getContext().getContentResolver(),
                TRACKS_CONTENT_URI);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case URI_TRACKS:
                return TRACKS_CONTENT_TYPE;
            case URI_TRACKS_ID:
                return TRACKS_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) != URI_TRACKS)
            throw new IllegalArgumentException("Wrong URI: " + uri);

        db = mProfileDbHelper.getWritableDatabase();
        long rowID = db.insert(ProfileContract.RecentTracksEntry.TABLE_NAME,
                null,
                values);

        Uri resultUri = ContentUris.withAppendedId(TRACKS_CONTENT_URI, rowID);
        // уведомляем ContentResolver, что данные по адресу resultUri изменились
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_TRACKS:
                break;
            case URI_TRACKS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = ProfileContract.RecentTracksEntry._ID  + " = " + id;
                } else {
                    selection = selection +
                            " AND " + ProfileContract.RecentTracksEntry._ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = mProfileDbHelper.getWritableDatabase();

        if (!TextUtils.isEmpty(selection)){
            Log.d(LOG_TAG, selection);
            Log.d(LOG_TAG, uri.toString());
        }

        int cnt = db.delete(ProfileContract.RecentTracksEntry.TABLE_NAME, selection, null);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "update, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_TRACKS:
                Log.d(LOG_TAG, "URI_CONTACTS");

                break;
            case URI_TRACKS_ID:
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_CONTACTS_ID, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = ProfileContract.RecentTracksEntry._ID + " = " + id;
                } else {
                    selection = selection + " AND " + ProfileContract.RecentTracksEntry._ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = mProfileDbHelper.getWritableDatabase();
        int cnt = db.update(ProfileContract.RecentTracksEntry.TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }
}
