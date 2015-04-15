package com.example.andrey.betterlastfm;

import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
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

    public static String userName;

    private DrawerLayout mDrawer;
    private int mIcons[] = {R.drawable.home, R.drawable. friends};
    private String mTitles[] = {"Home", "Friends"};
    private Toolbar mToolbar;

    private ActionBarDrawerToggle mDrawerToggle;

    public ArrayAdapter<RecentTrack> mListAdapter;
    public ArrayAdapter<TopArtist> mGridAdapter;

    //public static ListView listView;
    //public static GridView gridView;

    private Button mButton;
    private CardView mCardList;
    private ProfileDbHelper profileDbHelper;
    private SQLiteDatabase db;
    private SharedPreferences mShrdPrefs;

    public static int fullWidth;

    public static LinearLayout tracksListLinearLayout;
    public static LinearLayout artistsListLinearLayout;

    public static ImageView relativeBarImageview;
    public static ImageView ivProfilePic;
    public static TextView tvProfileName;
    public static TextView tvProfileDetails;
    public static TextView tvProfileListens;
    public static Context profileContext;
    public static ProgressBar profileProgressBar;



    private View tempView;
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
                    cursor.moveToNext();

                    //listView.addHeaderView(header);

//                    mListAdapter.add(new RecentTrack(
//                            cursor.getString(recentTrackArtistIndex) + " - " + cursor.getString(recentTrackNameIndex),
//                            cursor.getString(recentTrackURLIndex)));
//                    cursor.moveToNext();
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

        int topArtistsIconURL = cursor.getColumnIndex(ProfileContract.TopArtistsEntry.COLUMN_ARTIST_ICON_URL);
        int topArtistsName = cursor.getColumnIndex(ProfileContract.TopArtistsEntry.COLUMN_ARTIST_NAME);
        int topArtistsPlaycount = cursor.getColumnIndex(ProfileContract.TopArtistsEntry.COLUMN_ARTIST_PLAYCOUNT);

//        Point size = new Point();
//        getWindowManager().getDefaultDisplay().getSize(size);
//        float screenWidth = size.x;
//        int desiredWidth = (int) (screenWidth / 3);
//
//        artistsGridLayout.setColumnCount(3);
//        artistsGridLayout.setRowCount(3);
//
//
//
//
//        //artistsGridLayout.setLayoutParams(params);
//
//        if (cursor.moveToFirst()){
//            mGridAdapter.clear();
//
//            for (int j=0; j<3; j++){
//                for (int i=0; i<3; i++){
//
//                    GridLayout.Spec row = GridLayout.spec(j);
//                    GridLayout.Spec col = GridLayout.spec(i);
//
//                    GridLayout.LayoutParams params = new GridLayout.LayoutParams(row, col);
//                    params.width = desiredWidth;
//
//
//                    View view = LayoutInflater.from(this).inflate(R.layout.item_top_artists_grid, null);
//                    ((TextView) view.findViewById(R.id.tv_artists_name_grid)).setText(cursor.getString(topArtistsName));
//                    ((TextView) view.findViewById(R.id.tv_artists_playcount_grid)).setText(cursor.getString(topArtistsPlaycount));
//
//
//                    ImageView imageView = (ImageView) view.findViewById(R.id.iv_grid);
//
//                    if (!TextUtils.isEmpty(cursor.getString(topArtistsIconURL)))
//                        Picasso.with(this).load(cursor.getString(topArtistsIconURL)).into(imageView);
//
//                    //view.setLayoutParams(params);
//                    artistsGridLayout.addView(view, params);
//                    Log.d(LOG_TAG, "Added new child gridview");
//
//                    cursor.moveToNext();
//                }
//            }




        if (cursor.moveToFirst()){
            mGridAdapter.clear();
            artistsListLinearLayout.removeAllViews();


            //tempRelativeBar.setScaleType(ImageView.ScaleType.FIT_XY);


            //tempRelativeBar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

            //int fullWidth = tempRelativeBar.getMeasuredWidth();
            Log.d(LOG_TAG, Integer.toString(fullWidth));
//
//
//            Paint myPaint = new Paint();
//            myPaint.setColor(Color.BLACK);
//            myPaint.setStrokeWidth(5);
//            myPaint.setStyle(Paint.Style.STROKE);
//
//            Bitmap tempBitmap = Bitmap.createBitmap(fullWidth, 50, Bitmap.Config.RGB_565);
//
//            Bitmap bitmap = Bitmap.createBitmap(fullWidth, 50, Bitmap.Config.RGB_565);
//            Canvas canvas = new Canvas(tempBitmap);
//
//            canvas.drawBitmap(bitmap, 0, 0, null);
//            canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), myPaint);





            int fullPlays = Integer.parseInt(cursor.getString(topArtistsPlaycount).replaceAll("plays",""));
            for (int i=0; i<9; i++){

                View view = LayoutInflater.from(this).inflate(R.layout.item_artists_list, null);

                ImageView imageView = (ImageView) view.findViewById(R.id.artists_list_imageview);
                ((TextView) view.findViewById(R.id.artists_list_name_textview))
                        .setText(cursor.getString(topArtistsName));
                ((TextView) view.findViewById(R.id.artists_list_plays_textview))
                        .setText(cursor.getString(topArtistsPlaycount));

                float percentage = (Integer.parseInt(cursor.getString(topArtistsPlaycount).replaceAll("plays","")) * 100 / fullPlays);
                Log.d(LOG_TAG, Float.toString(percentage));

                ImageView relativeBar = (ImageView) view.findViewById(R.id.artists_relativebar_imageview);


//                relativeBar.setScaleType(ImageView.ScaleType.FIT_XY);
//                relativeBar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//                Log.d(LOG_TAG, Integer.toString(relativeBar.getMeasuredWidth()));

                ViewGroup.LayoutParams params = relativeBar.getLayoutParams();
                //params.width = (int) ((percentage * fullWidth)) / 100;
                params.width = (int) ((fullWidth * percentage) / 100);
                relativeBar.setLayoutParams(params);

                //relativeBar.setMaxWidth((int) (percentage * fullWidth));


                if (!TextUtils.isEmpty(cursor.getString(topArtistsIconURL)))
                    Picasso.with(this).load(cursor.getString(topArtistsIconURL)).resize(150, 150).centerCrop().into(imageView);

                artistsListLinearLayout.addView(view);

//                mGridAdapter.add(new TopArtist(
//                        cursor.getString(topArtistsName),
//                        cursor.getString(topArtistsPlaycount),
//                        cursor.getString(topArtistsIconURL)
//                ));
                cursor.moveToNext();
            }
            //tempRelativeBar.setVisibility(View.GONE);
            cursor.close();
        }
   }

//
//                View view = LayoutInflater.from(this).inflate(R.layout.item_top_artists_grid, null);
//                ((TextView) view.findViewById(R.id.tv_artists_name_grid)).setText(cursor.getString(topArtistsName));
//                ((TextView) view.findViewById(R.id.tv_artists_playcount_grid)).setText(cursor.getString(topArtistsPlaycount));
//
//
//                ImageView imageView = (ImageView) view.findViewById(R.id.iv_grid);
//
//                if (!TextUtils.isEmpty(cursor.getString(topArtistsIconURL)))
//                    Picasso.with(this).load(cursor.getString(topArtistsIconURL)).into(imageView);
//
//                //view.setLayoutParams(params);
//                artistsGridLayout.addView(view, params);
//                Log.d(LOG_TAG, "Added new child gridview");
//
//
//



//    @Override
//    public void onWindowFocusChanged(boolean hasFocus){
//        int width=ivProfilePic.getWidth();
//        int height=tempRelativeBar.getHeight();
//        Log.d(LOG_TAG, "focused changed **********************************" + Integer.toString(width));
//    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mShrdPrefs = getSharedPreferences("com.example.andrey.betterlastfm",MODE_PRIVATE);
        userName = mShrdPrefs.getString("username", "ERROR");

        ivProfilePic = (ImageView) findViewById(R.id.iv_profile_pic);
        tvProfileName = (TextView) findViewById(R.id.tv_profile_name);
        tvProfileDetails = (TextView) findViewById(R.id.tv_profile_details);
        tvProfileListens = (TextView) findViewById(R.id.tv_profile_listens);
        //profileProgressBar = (ProgressBar) findViewById(R.id.profile_progress_bar);

        profileContext = this.getApplicationContext();
        profileDbHelper = new ProfileDbHelper(this);
        db = profileDbHelper.getReadableDatabase();


        tracksListLinearLayout = (LinearLayout) findViewById(R.id.recent_tracks_linear_layout);
        artistsListLinearLayout = (LinearLayout) findViewById(R.id.artists_linear_layout);
        //artistsGridLayout = (GridLayout) findViewById(R.id.top_artists_grid_layout);

        //listView = (ListView) findViewById(R.id.list_recent_tracks);
        //gridView = (GridView) findViewById(R.id.grid_top_artists);
        mButton = (Button) findViewById(R.id.button_expand_list);
        mCardList = (CardView) findViewById(R.id.list_card);

        //tempView = LayoutInflater.from(this).inflate(R.layout.item_artists_list, null);

        final LinearLayout tempLinearLayout = (LinearLayout) findViewById(R.id.temp_linear_layout);
        tempRelativeBar = (ImageView) findViewById(R.id.artists_relativebar_imageview);

        ViewTreeObserver vto = tempRelativeBar.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                tempRelativeBar.getViewTreeObserver().removeOnPreDrawListener(this);
                fullWidth = tempRelativeBar.getMeasuredWidth();
                Log.d(LOG_TAG, "From this new method: " + Integer.toString(fullWidth));

                tempLinearLayout.setVisibility(View.GONE);
                return true;
            }
        });


        //listView.setScrollContainer(false);
        //gridView.setScrollContainer(false);

        if (getIntent().hasExtra("user")){
            userName = getIntent().getStringExtra("user");
        }

        mListAdapter = new TracksAdapter(this,R.layout.item_recent_tracks_list);
        //listView.setAdapter(mListAdapter);

        mGridAdapter = new ArtistsAdapter(this,R.layout.item_top_artists_grid);
        //gridView.setAdapter(mGridAdapter);

        getLoaderManager().initLoader(0, null, this);

        //setListViewHeightBasedOnChildren(gridView);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                Intent intent = new Intent(getApplicationContext(), RecentTracksActivity.class);
//                ArrayList<String> passListDataArr = new ArrayList<String>();
//                for (int i=0; i < listView.getAdapter().getCount(); i++){
//                    //passListDataArr.add(listView.getAdapter().getItem(i).toString());
//                    passListDataArr.add((String) listView.getItemAtPosition(i));
//                }
//                intent.putStringArrayListExtra("passKey", passListDataArr);

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation(ProfileActivity.this, mCardList, "listCard");

                    startActivity(intent, options.toBundle());
                } else{
                    startActivity(intent);
                }
            }
        });

        if (!userName.equals(mShrdPrefs.getString("username", "ERROR"))){
            //profileProgressBar.setVisibility(View.VISIBLE);
            try {
                getLoaderManager().getLoader(0).forceLoad();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            //Util.setListViewHeightBasedOnChildren(this, ProfileActivity.listView);
            //Util.setListViewHeightBasedOnChildren(this, ProfileActivity.gridView);
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
                                putExtra("user",userName));
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
        if (userName.equals(mShrdPrefs.getString("username", "ERROR"))){
            fillHeader();
            fillRecentTracks();
            fillTopArtists();
        }
        //Util.setListViewHeightBasedOnChildren(this, listView);
        //Util.setListViewHeightBasedOnChildren(this, gridView);
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
                //profileProgressBar.setVisibility(View.VISIBLE);
                //taskFetchProfile.execute(userName);
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
            startActivity(new Intent(this,FriendsActivity.class).putExtra("user",userName));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Void> onCreateLoader(int id, Bundle args) {
        return new ProfileLoader(this, mListAdapter, mGridAdapter, userName);
    }

    @Override
    public void onLoadFinished(Loader<Void> loader, Void data) {
        Log.d(LOG_TAG, "Loader finished?");
    }

    @Override
    public void onLoaderReset(Loader<Void> loader) {
        Log.d(LOG_TAG, "Loader reseted?");
    }


}
