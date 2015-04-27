package com.example.andrey.betterlastfm;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.andrey.betterlastfm.adapters.TracksAdapter;
import com.example.andrey.betterlastfm.data.ProfileContract;
import com.example.andrey.betterlastfm.loaders.ScrobbleLoader;
import com.example.andrey.betterlastfm.model.RecentTrack;
import com.example.andrey.betterlastfm.data.RecentTracksProvider;
import com.example.andrey.betterlastfm.loaders.RecentTracksLoader;

import java.util.ArrayList;


public class RecentTracksActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<ArrayList<RecentTrack>>{
    private static final String LOG_TAG = RecentTracksActivity.class.getSimpleName();

    private String mUserName;
    private ProgressBar bar;

    public ArrayAdapter<RecentTrack> mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_tracks);

        Intent intent = getIntent();
        mUserName = intent.getStringExtra("username");
        if (mUserName.equals(null))
            Toast.makeText(this,"Ooops! something went wrong!", Toast.LENGTH_SHORT).show();


        ListView mListView = (ListView) this.findViewById(R.id.list_recent_tracks);
        mListView.setScrollContainer(false);
        mListAdapter = new TracksAdapter(this,R.layout.item_recent_tracks_list);
        mListView.setAdapter(mListAdapter);

        bar = (ProgressBar) this.findViewById(R.id.progress_bar_recent);

        getLoaderManager().initLoader(0, null, this);

        SharedPreferences mShrdPrefs = getSharedPreferences("com.example.andrey.betterlastfm",MODE_PRIVATE);
        String storedUserName = mShrdPrefs.getString("username", "ERROR");

        if (mUserName.equals(storedUserName))
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    int iddqd = parent.getAdapter().getCount() - position;

                    Uri uri = ContentUris.withAppendedId(RecentTracksProvider.TRACKS_CONTENT_URI, iddqd);
                    int cnt = getContentResolver().delete(uri, "_id = " + Integer.toString(iddqd), null);
                    Toast.makeText(getApplicationContext(), "Deleted: " + cnt + "row, on " +
                        Integer.toString(iddqd) + " id", Toast.LENGTH_SHORT).show();

                    mListAdapter.remove((RecentTrack) parent.getAdapter().getItem(position));

                    return true;
                }
            });

        if (mUserName.equals(storedUserName)){
            Cursor cursor = getContentResolver().query(RecentTracksProvider.TRACKS_CONTENT_URI,
                    null,
                    null,
                    null,
                    ProfileContract.RecentTracksEntry._ID + " " + "DESC");

            int recentTrackIconIndexURL = cursor
                    .getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ICON_URL);
            int recentTrackArtistIndex = cursor
                    .getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ARTIST);
            int recentTrackNameIndex = cursor
                    .getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_NAME);
            int recentTrackDateIndex = cursor
                    .getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_TIMESTAMP);

            cursor.moveToFirst();

            do {
                mListAdapter.add(new RecentTrack(
                        cursor.getString(recentTrackNameIndex),
                        cursor.getString(recentTrackArtistIndex),
                        "",
                        cursor.getString(recentTrackIconIndexURL),
                        cursor.getString(recentTrackDateIndex)
                ));

            } while (cursor.moveToNext());

            cursor.close();

        } else {
            getLoaderManager().getLoader(0).forceLoad();
        }

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recent_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_refresh_recent_tracks) {
            try {
                bar.setVisibility(View.VISIBLE);
                getLoaderManager().getLoader(0).forceLoad();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

        } else if (id == R.id.action_scrobble_recent_tracks) {

            ArrayList<RecentTrack> recentTracks = new ArrayList<>();

            Cursor cursor = getContentResolver().query(RecentTracksProvider.TRACKS_CONTENT_URI,
                    null,
                    ProfileContract.RecentTracksEntry.COLUMN_SCROBBLEABLE_FLAG + " =" + " 1",
                    null,
                    ProfileContract.RecentTracksEntry._ID + " DESC"
            );

            int recentTrackArtistIndex = cursor
                    .getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ARTIST);
            int recentTrackNameIndex = cursor
                    .getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_NAME);
            int recentTrackAlbumIndex = cursor
                    .getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ALBUM);
            int recentTrackTimestamp = cursor
                    .getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_TIMESTAMP);

            if (cursor.moveToFirst()) {
                do {

                    recentTracks.add(new RecentTrack(
                            cursor.getString(recentTrackNameIndex),
                            cursor.getString(recentTrackArtistIndex),
                            cursor.getString(recentTrackAlbumIndex),
                            "",
                            cursor.getString(recentTrackTimestamp)
                    ));
                } while (cursor.moveToNext());

                ScrobbleLoader scrobbleLoader = new ScrobbleLoader(this,
                        Util.API_KEY,
                        recentTracks);
                scrobbleLoader.forceLoad();
            } else {
                Toast.makeText(this, "Nothing to scrobble!", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        getLoaderManager().getLoader(0).stopLoading();
    }

    @Override
    public Loader<ArrayList<RecentTrack>> onCreateLoader(int id, Bundle args) {
        return new RecentTracksLoader(this, mListAdapter, mUserName);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<RecentTrack>> loader, ArrayList<RecentTrack> data) {
        Log.d(LOG_TAG, "Finished loading.");

        mListAdapter.clear();

        Log.d(LOG_TAG, Integer.toString(data.size()));
        for(int i=0; i<10; i++) {
            mListAdapter.add(data.get(i));
        }

        bar.setVisibility(View.GONE);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<RecentTrack>> loader) {

    }
}
