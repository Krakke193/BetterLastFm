package com.example.andrey.betterlastfm.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.andrey.betterlastfm.R;
import com.example.andrey.betterlastfm.model.Friend;
import com.squareup.picasso.Picasso;

/**
 * Created by andrey on 11.03.15.
 */
public class FriendsAdapter extends ArrayAdapter<Friend> {

    private Context mContext;

    public FriendsAdapter(Context context, int resource) {
        super(context, resource);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Friend friend = getItem(position);

        View viewHolder = null;
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_friends_list, null);

        ((TextView) convertView.findViewById(R.id.tv_friend_name)).setText(friend.getFriendName());
        ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_friend_icon);

        if (!TextUtils.isEmpty(friend.getFriendImageURL()))
            Picasso.with(mContext)
                    .load(friend.getFriendImageURL())
                    .resize(100,100)
                    .centerCrop()
                    .into(imageView);

        return convertView;
    }
}

