package com.example.andrey.betterlastfm;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


public class FriendsActivity extends ActionBarActivity {

    private final String LOG_TAG = this.getClass().getSimpleName();
    private AdapterFriends mFrienListAdapter;
    private ListView friendList;
    private TaskFetchFriends taskFetchFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        friendList = (ListView) findViewById(R.id.friend_list);
        mFrienListAdapter = new AdapterFriends(this, R.layout.item_list_friends);
//        for (int i=0; i<10; i++){
//            mFrienListAdapter.add(new Friend("NO DATA", null));
//        }
        String userName = getIntent().getStringExtra("user");

        friendList.setAdapter(mFrienListAdapter);

        taskFetchFriends = new TaskFetchFriends(this, mFrienListAdapter);
        taskFetchFriends.execute(userName);

        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("user", mFrienListAdapter.getItem(position).friendName);
                startActivity(intent);
                Log.d(LOG_TAG, mFrienListAdapter.getItem(position).friendName);
            }
        });
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
        } else if (id == R.id.friends_action_refresh){
            taskFetchFriends.execute("se0ko");
        }

        return super.onOptionsItemSelected(item);
    }
}
