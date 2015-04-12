package com.example.andrey.betterlastfm;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.andrey.betterlastfm.adapters.TracksAdapter;
import com.example.andrey.betterlastfm.data.ProfileContract;
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
        ArrayList<String> passDataArr = intent.getStringArrayListExtra("passKey");

        ListView mListView = (ListView) this.findViewById(R.id.list_recent_tracks);
        mListView.setScrollContainer(false);
        TracksAdapter mListAdapter = new TracksAdapter(this,R.layout.item_recent_tracks_list);
        mListView.setAdapter(mListAdapter);

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

//                cursor.moveToNext();
            } while (cursor.moveToNext());
//            for (int j=0; j<5; j++){
//                mListAdapter.add(new RecentTrack(null, null));
//            }

//            for (int i=0; i<10; i++){
//                mListAdapter.add(new RecentTrack(
//                        cursor.getString(recentTrackName),
//                        cursor.getString(recentTrackIconURL)
//                ));
//                if (cursor.moveToNext()){
//
//                } else {
//                    for (int j=0; j<5; j++){
//                        mListAdapter.add(new RecentTrack(null, null));
//                    }
//
//                }
////                cursor.moveToNext();
//            }

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recent_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
