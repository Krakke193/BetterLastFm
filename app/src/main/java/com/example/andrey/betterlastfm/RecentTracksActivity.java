package com.example.andrey.betterlastfm;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.andrey.betterlastfm.data.ProfileContract;
import com.example.andrey.betterlastfm.data.ProfileDbHelper;

import java.util.ArrayList;


public class RecentTracksActivity extends ActionBarActivity {

    private static final String LOG_TAG = RecentTracksActivity.class.getSimpleName();

    private ListView mListView;
    private AdapterList mListAdapter;
    private Toolbar mToolbar;
    private SQLiteDatabase db;
    private ProfileDbHelper profileDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_tracks);

        Intent intent = getIntent();
        ArrayList<String> passDataArr = intent.getStringArrayListExtra("passKey");

        mListView = (ListView) this.findViewById(R.id.list_recent_tracks);
        mListView.setScrollContainer(false);
        mListAdapter = new AdapterList(this,R.layout.list_item);
        mListView.setAdapter(mListAdapter);

        profileDbHelper = new ProfileDbHelper(this);
        db = profileDbHelper.getReadableDatabase();

        if (ProfileActivity.userName.equals("se0ko")){


            Cursor cursor = db.query(
                    ProfileContract.RecentTracksEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            int recentTrackIconURL = cursor.getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ICON_URL);
            int recentTrackName = cursor.getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_NAME);
            cursor.moveToFirst();

            for (int i=0; i<10; i++){
                mListAdapter.add(new RecentTrack(
                        cursor.getString(recentTrackName),
                        cursor.getString(recentTrackIconURL)
                ));
                cursor.moveToNext();
            }

            cursor.close();
        } else {
            for (int i=0; i<5; i++){
                mListAdapter.add(new RecentTrack(
                        passDataArr.get(i),
                        null));
            }
        }



        // Attaching the layout to the toolbar object
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        // Setting toolbar as the ActionBar with setSupportActionBar() call
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
        db.close();
        Log.d(LOG_TAG, "Database closed!");
    }
}
