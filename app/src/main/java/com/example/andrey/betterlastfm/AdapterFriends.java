package com.example.andrey.betterlastfm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by andrey on 11.03.15.
 */
public class AdapterFriends extends ArrayAdapter<Friend> {

    private Context mContext;

    public AdapterFriends(Context context, int resource) {
        super(context, resource);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Friend friend = getItem(position);

        View viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_friends, null);
        }

        ((TextView) convertView.findViewById(R.id.tv_friend_name)).setText(friend.friendName);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_friend_icon);

//        new TaskDownloadImage(imageView).execute(friend.friendImageURL);
        Picasso.with(mContext)
                .load(friend.friendImageURL)
                .resize(100,100)
                .centerCrop()
                .into(imageView);

        return convertView;
    }
}

class Friend {

    public String friendName;
    public String friendImageURL;

    public Friend (String friendName, String friendImageURL){
        this.friendName = friendName;
        this.friendImageURL = friendImageURL;
    }
}
