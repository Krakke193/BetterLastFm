package com.example.andrey.betterlastfm;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.andrey.betterlastfm.adapters.TracksAdapter;
import com.example.andrey.betterlastfm.data.ProfileContract;
import com.example.andrey.betterlastfm.data.ProfileDbHelper;
import com.example.andrey.betterlastfm.model.RecentTrack;
import com.example.andrey.betterlastfm.data.RecentTracksProvider;
import com.example.andrey.betterlastfm.loaders.RecentTracksLoader;

import java.util.ArrayList;


public class RecentTracksActivity extends ActionBarActivity {
    private static final String LOG_TAG = RecentTracksActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_tracks);

        Intent intent = getIntent();
        //ArrayList<String> passDataArr = intent.getStringArrayListExtra("passKey");

        ListView mListView = (ListView) this.findViewById(R.id.list_recent_tracks);
        mListView.setScrollContainer(false);
        final TracksAdapter mListAdapter = new TracksAdapter(this,R.layout.item_recent_tracks_list);
        mListView.setAdapter(mListAdapter);

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int iddqd = parent.getAdapter().getCount() - position;

                Uri uri = ContentUris.withAppendedId(RecentTracksProvider.TRACKS_CONTENT_URI, iddqd);
                int cnt = getContentResolver().delete(uri, "_id = " + Integer.toString(iddqd), null);
                Toast.makeText(getApplicationContext(), "Deleted: " + cnt + "row, on " + Integer.toString(iddqd) + " id", Toast.LENGTH_SHORT).show();

                mListAdapter.remove((RecentTrack) parent.getAdapter().getItem(position));

                return true;
            }
        });

        if (ProfileActivity.userName.equals("se0ko")){
            Cursor cursor = getContentResolver().query(RecentTracksProvider.TRACKS_CONTENT_URI,
                    null,
                    null,
                    null,
                    ProfileContract.RecentTracksEntry._ID + " " + "DESC");

            int recentTrackIconURL = cursor
                    .getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ICON_URL);
            int recentTrackArtist = cursor
                    .getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ARTIST);
            int recentTrackName = cursor
                    .getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_NAME);

            cursor.moveToFirst();

            do {
                mListAdapter.add(new RecentTrack(
                        cursor.getString(recentTrackArtist) + " - " + cursor.getString(recentTrackName),
                        cursor.getString(recentTrackIconURL)
                ));

            } while (cursor.moveToNext());

            cursor.close();

        } else {
            RecentTracksLoader rtl = new RecentTracksLoader(this, mListAdapter, ProfileActivity.userName);
            rtl.forceLoad();
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
