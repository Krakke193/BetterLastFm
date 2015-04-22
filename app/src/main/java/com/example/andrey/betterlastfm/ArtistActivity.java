package com.example.andrey.betterlastfm;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.andrey.betterlastfm.adapters.NavigationListAdapter;
import com.example.andrey.betterlastfm.loaders.ArtistLoader;
import com.example.andrey.betterlastfm.model.Artist;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.squareup.picasso.Picasso;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by Andrey on 21.04.2015.
 */
public class ArtistActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Artist>, ObservableScrollViewCallbacks {
    private final String LOG_TAG = ArtistActivity.class.getSimpleName();

    private ImageView mArtistPic;
    private TextView mArtistName;
    private TextView mArtistListeners;
    private TextView mArtistPlaycount;
    private AutoResizeTextView mArtistInfo;
    private LinearLayout mArtistTagHolder;
    private ProgressBar bar;
    private SharedPreferences mShrdPrefs;
    private ObservableScrollView mScrollView;
    private Toolbar mToolbarView;

    private int fullWidth;
    private int mParallaxImageHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mArtistPic = (ImageView) findViewById(R.id.artists_list_imageview);
        mToolbarView = (Toolbar) findViewById(R.id.toolbar);
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(0,getResources().getColor(R.color.lastfm_red)));

        mScrollView = (ObservableScrollView) findViewById(R.id.activity_scrollview);
        mScrollView.setScrollViewCallbacks(this);

        mParallaxImageHeight = getResources().getDimensionPixelSize(R.dimen.parallax_image_height);
        //mParallaxImageHeight = fullWidth;


        //toolbar.setVisibility(View.GONE);

        mShrdPrefs = getSharedPreferences("com.example.andrey.betterlastfm",MODE_PRIVATE);


        mArtistName = (TextView) findViewById(R.id.artist_page_name);
        mArtistListeners = (TextView) findViewById(R.id.artist_page_listeners);
        mArtistPlaycount = (TextView) findViewById(R.id.artist_page_playcount);
        mArtistTagHolder = (LinearLayout) findViewById(R.id.artist_page_tagholder);
        mArtistInfo = (AutoResizeTextView) findViewById(R.id.artist_page_artist_info);

        bar = (ProgressBar) findViewById(R.id.artist_progress_bar);





        ViewTreeObserver vto = mArtistPic.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                mArtistPic.getViewTreeObserver().removeOnPreDrawListener(this);
                fullWidth = mArtistPic.getMeasuredWidth();
                ViewGroup.LayoutParams params = mArtistPic.getLayoutParams();
                params.height = fullWidth;
                mParallaxImageHeight = fullWidth - 110;
                mArtistPic.setLayoutParams(params);
                return true;
            }
        });

        //bar.setVisibility(View.VISIBLE);
        getLoaderManager().initLoader(0, null, this).forceLoad();

        // Nav. drawer

        ListView navigationListView = (ListView) findViewById(R.id.navigation_listview);
        navigationListView.setAdapter(new NavigationListAdapter(this));
        navigationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0: {
                        startActivity(new Intent(getApplicationContext(),ProfileActivity.class).
                                putExtra("user", mShrdPrefs.getString("username", "ERROR")));
                        break;
                    }
                    case 1: {
                        startActivity(new Intent(getApplicationContext(),FriendsActivity.class).
                                putExtra("user", mShrdPrefs.getString("username", "ERROR")));
                        break;
                    }
                }
            }
        });

        DrawerLayout mDrawer = (DrawerLayout) findViewById(R.id.DrawerLayout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer , null, R.string.open_drawer,R.string.close_drawer){

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
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        onScrollChanged(mScrollView.getCurrentScrollY(), false, false);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        int baseColor = getResources().getColor(R.color.lastfm_red);
        float alpha = 1 - (float) Math.max(0, mParallaxImageHeight - scrollY) / mParallaxImageHeight;
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(alpha, baseColor));
        //ViewHelper.setTranslationY(mArtistPic, scrollY / 2);
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    @Override
    public Loader<Artist> onCreateLoader(int id, Bundle args) {
        return new ArtistLoader(this, getIntent().getStringExtra(Util.ARTIST_KEY));
    }

    @Override
    public void onLoadFinished(Loader<Artist> loader, Artist data) {
        Picasso.with(this).load(data.getArtistPic()).resize(fullWidth, fullWidth).centerCrop().into(mArtistPic);

        mToolbarView.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbarView.setTitle(data.getArtistName());
        mToolbarView.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mArtistName.setText(data.getArtistName());
        mArtistListeners.setText(getString(R.string.text_artist_listeners) + data.getArtistListeners());
        mArtistPlaycount.setText(getString(R.string.text_artist_playcounts) + data.getArtistPlaycount());

        for (String tag : data.getArtistTags()){
            View view = LayoutInflater.from(this).inflate(R.layout.item_tag, null);
            TextView temp = (TextView) view.findViewById(R.id.item_tag_textview);
            //TextView temp = new TextView(this);
            temp.setText(tag);
            temp.setBackgroundColor(Color.GRAY);

            mArtistTagHolder.addView(view);
        }

        mArtistInfo.setText(data.getArtistInfo());

        bar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Artist> loader) {

    }
}
