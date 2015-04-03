package com.example.andrey.betterlastfm;

import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.andrey.betterlastfm.data.ProfileContract;
import com.example.andrey.betterlastfm.data.ProfileDbHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProfileActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Void>{
    public final static String LOG_TAG = ProfileActivity.class.getSimpleName();

    public static String userName = "se0ko";

    private RecyclerView mRecyclerView;                           // Declaring RecyclerView
    private RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    private RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    private DrawerLayout mDrawer;                                  // Declaring DrawerLayout
    private int mIcons[] = {R.drawable.home, R.drawable. friends};
    private String mTitles[] = {"Home", "Friends"};
    private Toolbar mToolbar;

    private ActionBarDrawerToggle mDrawerToggle;                  // Declaring Action Bar Drawer Toggle

    public ArrayAdapter<RecentTrack> mListAdapter;
    public ArrayAdapter<TopArtist> mGridAdapter;
    public ListView listView;
    public GridView gridView;
    private Button mButton;
    private CardView mCardList;
    //private TaskFetchProfile taskFetchProfile;
    private ProfileDbHelper profileDbHelper;
    private SQLiteDatabase db;

    public static ImageView ivProfilePic;
    public static TextView tvProfileName;
    public static TextView tvProfileDetails;
    public static TextView tvProfileListens;
    public static Context profileContext;
    public static ProgressBar profileProgressBar;

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
        Cursor cursor = db.query(
                ProfileContract.RecentTracksEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        int recentTrackName = cursor.getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_NAME);
        int recentTrackURL = cursor.getColumnIndex(ProfileContract.RecentTracksEntry.COLUMN_TRACK_ICON_URL);

        if (cursor.moveToFirst()){
            for (int i=0; i<5; i++) {
                mListAdapter.add(new RecentTrack(
                        cursor.getString(recentTrackName),
                        cursor.getString(recentTrackURL)));
                cursor.moveToNext();
            }

            cursor.moveToFirst();
            do {
                Log.d(LOG_TAG, cursor.getString(recentTrackName));
            } while (cursor.moveToNext());


            cursor.close();
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

        if (cursor.moveToFirst()){
            for (int i=0; i<8; i++){
                mGridAdapter.add(new TopArtist(
                        cursor.getString(topArtistsName),
                        cursor.getString(topArtistsPlaycount),
                        cursor.getString(topArtistsIconURL)
                ));
                cursor.moveToNext();
            }
            cursor.close();
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);



        ivProfilePic = (ImageView) findViewById(R.id.iv_profile_pic);
        tvProfileName = (TextView) findViewById(R.id.tv_profile_name);
        tvProfileDetails = (TextView) findViewById(R.id.tv_profile_details);
        tvProfileListens = (TextView) findViewById(R.id.tv_profile_listens);
        profileProgressBar = (ProgressBar) findViewById(R.id.profile_progress_bar);
        profileContext = this.getApplicationContext();
        profileDbHelper = new ProfileDbHelper(this);
        db = profileDbHelper.getReadableDatabase();

        listView = (ListView) findViewById(R.id.list_recent_tracks);
        gridView = (GridView) findViewById(R.id.grid_top_artists);
        mButton = (Button) findViewById(R.id.button_expand_list);
        mCardList = (CardView) findViewById(R.id.list_card);

        listView.setScrollContainer(false);
        gridView.setScrollContainer(false);

        if (getIntent().hasExtra("user")){
            userName = getIntent().getStringExtra("user");
        }

        mListAdapter = new AdapterList(this,R.layout.list_item);
        listView.setAdapter(mListAdapter);

        mGridAdapter = new AdapterGrid(this,R.layout.grid_item);
        gridView.setAdapter(mGridAdapter);

        getLoaderManager().initLoader(0, null, this);

//        FragmentManager fragmentManager = this.getSupportFragmentManager();
//        RecentTracksFragment recentTracksFragment = (RecentTracksFragment) fragmentManager.findFragmentById(R.id.fragment_recent_tracks);

        if (userName.equals("se0ko")){
            fillHeader();
            fillRecentTracks();
            fillTopArtists();
        } else {
//            supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//            setSupportProgressBarIndeterminateVisibility(true);
            profileProgressBar.setVisibility(View.VISIBLE);
            try {
                //taskFetchProfile.execute(userName);
                getLoaderManager().getLoader(0).forceLoad();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

//            for (int i=0; i<5; i++){
//                mListAdapter.add(new RecentTrack("NO DATA", null));
//            }
//            for (int i=0; i<8; i++){
//                mGridAdapter.add(new TopArtist("NO DATA", "no data", null));
//            }

        }

        //setListViewHeightBasedOnChildren(listView);
        //setListViewHeightBasedOnChildren(gridView);

        //taskFetchProfile = new TaskFetchProfile(this, mListAdapter, mGridAdapter, userName);



        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RecentTracksActivity.class);
                ArrayList<String> passListDataArr = new ArrayList<String>();
                for (int i=0; i < listView.getAdapter().getCount(); i++){
                    passListDataArr.add(listView.getAdapter().getItem(i).toString());
                }
                intent.putStringArrayListExtra("passKey", passListDataArr);

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation(ProfileActivity.this, mCardList, "listCard");

                    startActivity(intent, options.toBundle());
                } else{
                    startActivity(intent);
                }


            }
        });
        /** All with navigation drawer here: */

        // Attaching the layout to the toolbar object
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        // Setting toolbar as the ActionBar with setSupportActionBar() call
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

//        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view); // Assigning the RecyclerView Object to the xml View
//        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size
//        mAdapter = new NavigationAdapter(mTitles, mIcons,"se0ko","Da eto ya", R.drawable.male);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
//        // And passing the titles,icons,header view name, header view email,
//        // and header view profile picture
//
//        mRecyclerView.setAdapter(mAdapter);                              // Setting the adapter to RecyclerView
//        mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager
//        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager

        mDrawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
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

        }; // Drawer Toggle Object Made
        mDrawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();               // Finally we set the drawer toggle sync State

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
        } else if (id == R.id.action_refresh){
            try {
                //taskFetchProfile.execute(userName);
                getLoaderManager().getLoader(0).forceLoad();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        } else if (id == R.id.action_friends){
            startActivity(new Intent(this,FriendsActivity.class).putExtra("user",userName));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Void> onCreateLoader(int id, Bundle args) {
        FetchProfileLoader fetchProfileLoader = new FetchProfileLoader(this, mListAdapter, mGridAdapter, userName);
        //fetchProfileLoader.forceLoad();
        return fetchProfileLoader;
    }

    @Override
    public void onLoadFinished(Loader<Void> loader, Void data) {
        Log.d(LOG_TAG, "Loader finished?");
    }

    @Override
    public void onLoaderReset(Loader<Void> loader) {
        Log.d(LOG_TAG, "Loader reseted?");
    }

    //    public void setListViewHeightBasedOnChildren(ListView listView) {
//        android.widget.AdapterList listAdapter = listView.getAdapter();
//        if (listAdapter == null)
//            return;
//
//        float totalHeight = listAdapter.getCount() * this.getResources().getDimension(R.dimen.list_item);
//        ViewGroup.LayoutParams params = listView.getLayoutParams();
//        params.height = (int)totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
//
//
//        /** Method found on Internet. */
////        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
////        int totalHeight = 0;
////        int debugingCount = 0;
////        View view = null;
////        for (int i = 0; i < listAdapter.getCount(); i++) {
////            view = listAdapter.getView(i, null, listView);
////            if (i == 0)
////                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, RadioGroup.LayoutParams.WRAP_CONTENT));
////
////            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
////
////            //view.measure(View.MeasureSpec.makeMeasureSpec(desiredWidth, View.MeasureSpec.AT_MOST)
////            //       , View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
////
////            totalHeight += view.getMeasuredHeight();
////            Log.d("Profile Activity", Integer.toString(totalHeight));
////            debugingCount++;
////        }
////        Log.d("Profile Activity", Integer.toString(debugingCount));
////
////        ViewGroup.LayoutParams params = listView.getLayoutParams();
////        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
////        listView.setLayoutParams(params);
////        listView.requestLayout();
//    }
//
//    public void setListViewHeightBasedOnChildren(GridView gridView) {
//        android.widget.AdapterList listAdapter = gridView.getAdapter();
//        if (listAdapter == null)
//            return;
//
//        float totalHeight = (listAdapter.getCount() / 2) * (this.getResources().getDimension(R.dimen.iv_height_grid)
//                + this.getResources().getDimension(R.dimen.tv_height_grid));
//        ViewGroup.LayoutParams params = gridView.getLayoutParams();
//        params.height = (int)totalHeight;
//    }
}
