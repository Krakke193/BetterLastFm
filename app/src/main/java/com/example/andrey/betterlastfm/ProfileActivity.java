package com.example.andrey.betterlastfm;

import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andrey.betterlastfm.adapters.ArtistsAdapter;
import com.example.andrey.betterlastfm.adapters.NavigationListAdapter;
import com.example.andrey.betterlastfm.adapters.TracksAdapter;
import com.example.andrey.betterlastfm.data.ProfileContract;
import com.example.andrey.betterlastfm.data.ProfileDbHelper;
import com.example.andrey.betterlastfm.loaders.ScrobbleLoader;
import com.example.andrey.betterlastfm.model.RecentTrack;
import com.example.andrey.betterlastfm.data.RecentTracksProvider;

import com.example.andrey.betterlastfm.model.TopArtist;
import com.example.andrey.betterlastfm.loaders.ProfileLoader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProfileActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Void>{
    public final static String LOG_TAG = ProfileActivity.class.getSimpleName();

    public static String mUserName;

    private DrawerLayout mDrawer;
    private int mIcons[] = {R.drawable.home, R.drawable. friends};
    private String mTitles[] = {"Home", "Friends"};
    private Toolbar mToolbar;

    private ActionBarDrawerToggle mDrawerToggle;

    public ArrayAdapter<RecentTrack> mListAdapter;
    public ArrayAdapter<TopArtist> mGridAdapter;

    private ProgressBar bar;
    private ProgressDialog mProgressDialog;
    private Button mButton;
    private CardView mCardList;
    private ProfileDbHelper profileDbHelper;
    private SQLiteDatabase db;
    private SharedPreferences mShrdPrefs;

    public static int fullWidth;

    public static LinearLayout tracksListLinearLayout;
    public static LinearLayout artistsListLinearLayout;
    public static ImageView ivProfilePic;
    public static TextView tvProfileName;
    public static TextView tvProfileDetails;
    public static TextView tvProfileListens;
    public static Context profileContext;

    private ImageView tempRelativeBar;

    public static final String USERNAME_KEY = "username_key";

    public void fillHeader(){
        Cursor cursor = db.query(
                ProfileContract.HeaderEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
        int profileIconURL = cursor.getColumnIndex(ProfileContract.HeaderEntry.COLUMN_HEADER_ICON_URL);
        int profileName = cursor.getColumnIndex(ProfileContract.HeaderEntry.COLUMN_HEADER_NAME);
        int profileRealName = cursor.getColumnIndex(ProfileContract.HeaderEntry.COLUMN_HEADER_REAL_NAME);
        int profileAge = cursor.getColumnIndex(ProfileContract.HeaderEntry.COLUMN_HEADER_AGE);
        int profileCountry = cursor.getColumnIndex(ProfileContract.HeaderEntry.COLUMN_HEADER_COUNTRY);
        int profilePlaycount = cursor.getColumnIndex(ProfileContract.HeaderEntry.COLUMN_HEADER_PLAYCOUNT);
        int profileRegDate = cursor.getColumnIndex(ProfileContract.HeaderEntry.COLUMN_HEADER_REGISTRY_DATE);

        if (cursor.moveToFirst()){
            Picasso.with(this).load(cursor.getString(profileIconURL)).into(ivProfilePic);
            tvProfileName.setText(cursor.getString(profileName));
            tvProfileDetails.setText(
                    cursor.getString(profileRealName) + ", " + cursor.getString(profileAge) + ", " +
                            cursor.getString(profileCountry)
            );
            tvProfileListens.setText(
                    cursor.getString(profilePlaycount) + this.getResources().getString(R.string.profile_header_listens) +
                            cursor.getString(profileRegDate)
            );

            cursor.close();
        }
    }

    public void fillRecentTracks(){
        try {
            Cursor cursor = getContentResolver().query(RecentTracksProvider.TRACKS_CONTENT_URI,
                    null,
                    null,
                    null,
                    ProfileContract.RecentTracksEntry._ID + " " + "DESC");

            int recentTrackArtistIndex = cursor.getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ARTIST);
            int recentTrackNameIndex = cursor.getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_NAME);
            int recentTrackURLIndex = cursor.getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ICON_URL);


            if (cursor.moveToFirst()){
                mListAdapter.clear();
                tracksListLinearLayout.removeAllViews();
                for (int i=0; i<5; i++) {

                    View header = LayoutInflater.from(this).inflate(R.layout.item_recent_tracks_list, null);
                    ((TextView) header.findViewById(R.id.list_textview))
                            .setText(cursor.getString(recentTrackArtistIndex) +
                                            " - " +
                                            cursor.getString(recentTrackNameIndex)
                            );

                    ImageView imageView = (ImageView) header.findViewById(R.id.list_imageview);

                    if (!TextUtils.isEmpty(cursor.getString(recentTrackURLIndex)))
                        Picasso.with(this).load(cursor.getString(recentTrackURLIndex)).into(imageView);

                    tracksListLinearLayout.addView(header);
                    Log.d(LOG_TAG, "Added new childview");

                    ImageView divider1 = new ImageView(this);
                    LinearLayout.LayoutParams lp1 =
                            new LinearLayout.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, 1);
                    lp1.setMargins(5, 0, 5, 0);
                    divider1.setLayoutParams(lp1);
                    divider1.setBackgroundColor(Color.rgb(200, 200, 200));
                    tracksListLinearLayout.addView(divider1);

                    cursor.moveToNext();
                }
                cursor.close();
            }
        } catch (Exception e){
            getLoaderManager().getLoader(0).forceLoad();
            e.printStackTrace();
        }
    }

    public void fillTopArtists(){
        Cursor cursor = db.query(
                ProfileContract.TopArtistsEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        int topArtistsIconURL = cursor.getColumnIndex(ProfileContract
                .TopArtistsEntry.COLUMN_ARTIST_ICON_URL);
        int topArtistsName = cursor.getColumnIndex(ProfileContract
                .TopArtistsEntry.COLUMN_ARTIST_NAME);
        int topArtistsPlaycount = cursor.getColumnIndex(ProfileContract
                .TopArtistsEntry.COLUMN_ARTIST_PLAYCOUNT);

        if (cursor.moveToFirst()){
            mGridAdapter.clear();
            artistsListLinearLayout.removeAllViews();

            int currentFullWidth = mShrdPrefs.getInt("full_width", 50);

            Log.d(LOG_TAG, Integer.toString(currentFullWidth));

            int fullPlays = Integer.parseInt(cursor.getString(topArtistsPlaycount)
                    .replaceAll("plays",""));

            for (int i=0; i<9; i++){

                View view = LayoutInflater.from(this).inflate(R.layout.item_artists_list, null);

                ImageView imageView = (ImageView) view.findViewById(R.id.artists_list_imageview);
                ((TextView) view.findViewById(R.id.artists_list_name_textview))
                        .setText(cursor.getString(topArtistsName));
                ((TextView) view.findViewById(R.id.artists_list_plays_textview))
                        .setText(cursor.getString(topArtistsPlaycount));

                float percentage = (Integer.parseInt(cursor.getString(topArtistsPlaycount)
                        .replaceAll("plays","")) * 100 / fullPlays);

                Log.d(LOG_TAG, Float.toString(percentage));

                ImageView relativeBar = (ImageView) view.findViewById(R.id.artists_relativebar_imageview);

                ViewGroup.LayoutParams params = relativeBar.getLayoutParams();
                params.width = (int) ((currentFullWidth * percentage) / 100);
                relativeBar.setLayoutParams(params);

                if (!TextUtils.isEmpty(cursor.getString(topArtistsIconURL)))
                    Picasso.with(this).load(cursor.getString(topArtistsIconURL)).resize(150, 150)
                            .centerCrop().into(imageView);

                artistsListLinearLayout.addView(view);

                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mShrdPrefs = getSharedPreferences("com.example.andrey.betterlastfm",MODE_PRIVATE);
        mUserName = mShrdPrefs.getString("username", "ERROR");

        ivProfilePic = (ImageView) findViewById(R.id.iv_profile_pic);
        tvProfileName = (TextView) findViewById(R.id.tv_profile_name);
        tvProfileDetails = (TextView) findViewById(R.id.tv_profile_details);
        tvProfileListens = (TextView) findViewById(R.id.tv_profile_listens);

        profileContext = this.getApplicationContext();
        profileDbHelper = new ProfileDbHelper(this);
        db = profileDbHelper.getReadableDatabase();

        tracksListLinearLayout = (LinearLayout) findViewById(R.id.recent_tracks_linear_layout);
        artistsListLinearLayout = (LinearLayout) findViewById(R.id.artists_linear_layout);

        mButton = (Button) findViewById(R.id.button_expand_list);
        mCardList = (CardView) findViewById(R.id.list_card);

        bar = (ProgressBar) this.findViewById(R.id.progressBar);
//        mProgressDialog = new ProgressDialog(this);
//        mProgressDialog.setIndeterminate(false);
//        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        final LinearLayout tempLinearLayout = (LinearLayout) findViewById(R.id.temp_linear_layout);
        tempRelativeBar = (ImageView) findViewById(R.id.artists_relativebar_imageview);

        ViewTreeObserver vto = tempRelativeBar.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                if (!mShrdPrefs.contains("full_width")){
                    tempRelativeBar.getViewTreeObserver().removeOnPreDrawListener(this);
                    fullWidth = tempRelativeBar.getMeasuredWidth();
                    SharedPreferences.Editor shrdEditor = mShrdPrefs.edit();
                    shrdEditor.putInt("full_width", fullWidth);
                    shrdEditor.commit();

                    Log.d(LOG_TAG, "From this new method: " + Integer.toString(fullWidth));
                }

                tempLinearLayout.setVisibility(View.GONE);
                return true;
            }
        });

        if (getIntent().hasExtra("user")){
            mUserName = getIntent().getStringExtra("user");
        }

        mListAdapter = new TracksAdapter(this,R.layout.item_recent_tracks_list);
        mGridAdapter = new ArtistsAdapter(this,R.layout.item_top_artists_grid);

        getLoaderManager().initLoader(0, null, this);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RecentTracksActivity.class);
                intent.putExtra("username", mUserName);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation(ProfileActivity.this, mCardList, "listCard");

                    startActivity(intent, options.toBundle());
                } else{
                    startActivity(intent);
                }
            }
        });

        if (!mUserName.equals(mShrdPrefs.getString("username", "ERROR"))){
            try {
                bar.setVisibility(View.VISIBLE);
                getLoaderManager().getLoader(0).forceLoad();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }


        /** All with navigation drawer here: */

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ListView navigationListView = (ListView) findViewById(R.id.navigation_listview);
        navigationListView.setAdapter(new NavigationListAdapter(this));
        navigationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0: {
                        startActivity(new Intent(getApplicationContext(),ProfileActivity.class).
                                putExtra("user","se0ko"));
                        break;
                    }
                    case 1: {
                        startActivity(new Intent(getApplicationContext(),FriendsActivity.class).
                                putExtra("user", mUserName));
                        break;
                    }
                }
            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.DrawerLayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer , mToolbar, R.string.open_drawer,R.string.close_drawer){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
            }

        };
        mDrawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();


    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "ON RESUMED!");
        if (mUserName.equals(mShrdPrefs.getString("username", "ERROR"))){
            fillHeader();
            fillRecentTracks();
            fillTopArtists();
        }

        //tracksListLinearLayout.setDividerDrawable(getResources().getDrawable(R.drawable.divider));
        tracksListLinearLayout.setDividerPadding(10);
        tracksListLinearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        getLoaderManager().getLoader(0).stopLoading();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;

        } else if (id == R.id.action_refresh) {
            try {
                bar.setVisibility(View.VISIBLE);
                getLoaderManager().getLoader(0).forceLoad();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

        } else if (id == R.id.action_scrobble){
            ArrayList<String> trackNames = new ArrayList<>();
            ArrayList<String> trackArtists = new ArrayList<>();
            ArrayList<String> trackTimestamps = new ArrayList<>();

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
            int recentTrackTimestamp = cursor
                    .getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_TIMESTAMP);

            if (cursor.moveToFirst()){
                int i = 0;
                do{
                    trackNames.add(cursor.getString(recentTrackNameIndex));
                    trackArtists.add(cursor.getString(recentTrackArtistIndex));
                    trackTimestamps.add(cursor.getString(recentTrackTimestamp));
                } while (cursor.moveToNext());

                ScrobbleLoader scrobbleLoader = new ScrobbleLoader(this,
                        Util.API_KEY,
                        trackNames,
                        trackArtists,
                        trackTimestamps);
                scrobbleLoader.forceLoad();
            } else {
                Toast.makeText(this, "Nothing to scrobble!", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.action_friends){
            startActivity(new Intent(this,FriendsActivity.class).putExtra("user", mUserName));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Void> onCreateLoader(int id, Bundle args) {

        //mProgressDialog.show();
        return new ProfileLoader(this, mUserName);
    }

    @Override
    public void onLoadFinished(Loader<Void> loader, Void data) {
        bar.setVisibility(View.GONE);
        //mProgressDialog.dismiss();
        Log.d(LOG_TAG, "Loader finished?");
    }

    @Override
    public void onLoaderReset(Loader<Void> loader) {
        Log.d(LOG_TAG, "Loader reseted?");
    }


}
