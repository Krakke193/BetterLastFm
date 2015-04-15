package com.example.andrey.betterlastfm.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andrey.betterlastfm.R;
import com.squareup.picasso.Picasso;
import com.example.andrey.betterlastfm.model.RecentTrack;

/**
 * Created by andrey on 01.03.15.
 */
public class TracksAdapter extends ArrayAdapter<RecentTrack> {
    private final String LOG_TAG = TracksAdapter.class.getSimpleName();
    private Context mContext;

    public TracksAdapter(Context context, int resource) {
        super(context,resource);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RecentTrack recentTrack = getItem(position);

        View viewHolder = null;
        if (convertView == null) {

            //View header = LayoutInflater.from(getContext()).inflate(R.layout.item_recent_tracks_list, null);


            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_recent_tracks_list, null);
        }

        ((TextView) convertView.findViewById(R.id.list_textview)).setText(recentTrack.trackInfo);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.list_imageview);

        if (!TextUtils.isEmpty(recentTrack.trackImageURL))
            Picasso.with(mContext).load(recentTrack.trackImageURL).into(imageView);

        return convertView;
    }
}