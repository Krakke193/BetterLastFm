package com.example.andrey.betterlastfm;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.andrey.betterlastfm.adapters.FriendsAdapter;
import com.example.andrey.betterlastfm.loaders.FriendsLoader;
import com.example.andrey.betterlastfm.model.Friend;

import java.util.ArrayList;


public class FriendsActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<ArrayList<Friend>> {

    private final String LOG_TAG = this.getClass().getSimpleName();
    private ProgressBar bar;
    private String mUserName;
    private FriendsAdapter mFrienListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bar = (ProgressBar) this.findViewById(R.id.progressBar);
        ListView friendList = (ListView) findViewById(R.id.friend_list);

        mFrienListAdapter = new FriendsAdapter(this, R.layout.item_friends_list);

        mUserName = getIntent().getStringExtra("user");

        friendList.setAdapter(mFrienListAdapter);

        bar.setVisibility(View.VISIBLE);
        getLoaderManager().initLoader(0, null, this);
        getLoaderManager().getLoader(0).forceLoad();

        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("user", mFrienListAdapter.getItem(position).getFriendName());
                startActivity(intent);
                Log.d(LOG_TAG, mFrienListAdapter.getItem(position).getFriendName());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        getLoaderManager().getLoader(0).stopLoading();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_friends, menu);
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
    public Loader<ArrayList<Friend>> onCreateLoader(int id, Bundle args) {
        return new FriendsLoader(this, mUserName);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Friend>> loader, ArrayList<Friend> data) {
        if (data == null){
            Toast.makeText(this, getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
            return;
        }
        mFrienListAdapter.clear();
        for (Friend friend : data) {
            mFrienListAdapter.add(friend);

            bar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Friend>> loader) {

    }

}
